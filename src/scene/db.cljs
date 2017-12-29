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
             (p/chain #(.db % config/db-name)
                      (utils/logger-fn "`db` connection ready"))))

(defstate logs-collection
  :start (p/chain @db
                  #(.collection % "logs")
                  (utils/logger-fn "`logs` collection ready")))


(defn get-signature [log]
  (aget log "topics" 0))

(defn get-address [log]
  (aget log "address"))

(defn get-id [log]
  (aget log "_id"))


(defn create-id
  "create `_id` for log"
  [log]
  (clojure.string/join ":" ((juxt :blockNumber :logIndex) log)))


(defn log->db-json
  "convert log to json accepted by mongodb, adds id to document"
  [log]
  (.assign js/Object
           # js {}
           log
           #js {"_id"       (create-id log)
                "signature" (get-signature log)}))

(defn logs->db-json
  "convert array with logs to json accepted by mongodb, adds id to document"
  [logs]
  (.map logs log->db-json))


(defn- initializeOrderedBulkOp [collection]
  (.initializeOrderedBulkOp collection))

(defn- save-in-collection
  [to-save collection]
  (.replaceOne collection
               #js {:_id (get-id to-save)}
               to-save
               #js {:upset true}))

(defn- save-in-batch
  [to-save batch]
  (-> batch
      (.find #js {:_id (get-id to-save)})
      (.upsert)
      (.replaceOne to-save)))

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
                   (.forEach to-save #(save-in-batch % batch))
                   batch))
        (p/then #(.execute %))
        utils/promise->chan)))


(defn parse-events
  [raw-events decoder]
  (map decoder raw-events))
                                        ;TODO: cursor -> channel -> stream

(defn- get-logs*
  [decoder filter]
  (-> @logs-collection
      (p/chain #(.find % (clj->js filter))
               #(.limit % 1000)
               #(.toArray %)
               #(parse-events % decoder))
       utils/promise->chan))

(defn get-logs
  ([decoder signature]
   (get-logs* decoder {:signature signature}))
  ([decoder address signature]
   (get-logs* decoder {:signature signature
                       :address    address})))
