(ns scene.db
  (:require [mount.core :refer [defstate]]
            [promesa.core :as p]
            [scene.utils :as utils]
            [scene.config :as config]))

(def MongoClient (.-MongoClient (js/require "mongodb")))

(defstate conn
  :start (.connect MongoClient
                   config/mongo-url)
  :stop (p/map #(.close %)
               @conn))


(defstate db
  :start (-> @conn
             (p/then #(.db % config/db-name))))

(defstate logs-collection
  :start (p/then @db #(.collection % "logs")))

(defn redis-key
  "produce key with prefix"
  [s]
  (str config/key-prefix ":" s))

(defn topic-key [t]
  (redis-key (str "topic:" t)))

(defn address-key [a t]
  (str (topic-key t) ":address:" a))

(defn get-topic [log]
  (->> log
       :topics
       first))

(defn get-address [log]
  (:address log))

(defn log->address-key
  "produce redis key for given `log` map, based on contract address"
  [log]
  (address-key (get-address log) (get-topic log)))

(defn log->topic-key
  "produce redis jey for given `log` map, based on topic"
  [log]
  (topic-key (get-topic log)))

(defn create-id
  "create `_id` for log"
  [log]
  (clojure.string/join ":" ((juxt :blockNumber :logIndex) log)))

(defn log->db-json
  "conver log to json accepted by couchdb, adds id to document"
  [log]
  (assoc log :_id (create-id log) :signature (-> log :topics first)))

(defn logs->db-json
  [logs]
  (map log->db-json logs))

(defn initializeOrderedBulkOp [collection]
  (.initializeOrderedBulkOp collection))

(defn- save-in-collection
  [{:keys [_id] :as to-save} collection]
  (.replaceOne collection
           #js {:_id _id}
           (clj->js to-save)
           #js {:upset true}))

(defn- save-in-batch
  [{:keys [_id] :as to-save} batch]
  (-> batch
      (.find #js {:_id _id})
      (.upsert)
      (.replaceOne (clj->js to-save))))

(defn save-log
  [log]
  (let [to-save (log->db-json log)
        save (partial save-in-collection to-save)]
    (-> @logs-collection
        (p/then save)
        utils/promise->chan)))


(defn save-logs
  [logs]
  (let [to-save (logs->db-json logs)]
    (-> @logs-collection
        (p/chain initializeOrderedBulkOp
                 (fn [batch]
                   (doseq [x to-save]
                     (save-in-batch x batch))
                   batch))
        (p/then #(.execute %))
        utils/promise->chan)))


(defn parse-events [raw-events decoder]
  (map #(-> %
            utils/transit->clj
            decoder)
       raw-events))
                                        ;TODO: cursor -> channel -> stream
(defn get-log
  ([decoder signature]
   (-> @logs-collection
       (p/chain #(.find % #js {:signature signature})
                #(.toArray %))
       utils/promise->chan))
  ([decoder address signature]
   (-> @logs-collection
       (p/chain #(.find % #js {:signature signature
                               :address   address})
                #(.toArray %))
       (utils/promise->chan))))
