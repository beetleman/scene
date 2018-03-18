(ns scene.db
  (:require [clojure.core.async :refer [<! chan]]
            [mount.core :refer [defstate]]
            [promesa.core :as p]
            [scene.config :as config]
            [scene.interop :as interop]
            [scene.utils :as utils]
            [scene.web3.event :as web3event])
  (:require-macros [cljs.core.async.macros :refer [go]]))

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
                  (fn [coll]
                    (.createIndex coll #js {:_id 1})
                    (.createIndex coll #js {:blockNumber 1})
                    (.createIndex coll #js {:signature 1})
                    (.createIndex coll #js {:signature 1 :address 1})
                    coll)
                  (utils/logger-fn "`logs` collection ready")))


(defn create-id
  "create `_id` for log"
  [log]
  (clojure.string/join ":"
                       ((juxt interop/get-block-number
                              interop/get-log-index) log)))

(defn log->db-json
  "convert log to json accepted by mongodb, adds id to document"
  [log]
  (interop/js-merge log
                    #js {"_id"       (create-id log)
                         "signature" (interop/get-topic log 0)}))

(defn logs->db-json
  "convert array with logs to json accepted by mongodb, adds id to document"
  [logs]
  (.map logs log->db-json))


(defn- initializeOrderedBulkOp [collection]
  (.initializeOrderedBulkOp collection))

(defn- save-in-collection
  [to-save collection]
  (.replaceOne collection
               #js {:_id (interop/get-id to-save)}
               to-save
               #js {:upset true}))

(defn- save-in-batch ;TODO: attach spec
  [to-save batch]
  (-> batch
      (.find #js {:_id (interop/get-id to-save)})
      (.upsert)
      (.replaceOne to-save)))

(defn save-logs ;TODO: attach spec
  [logs]
  (let [to-save (logs->db-json logs)]
    (-> @logs-collection
        (p/chain initializeOrderedBulkOp
                 (fn [batch]
                   (.forEach to-save #(save-in-batch % batch))
                   batch))
        (p/then #(.execute %))
        utils/promise->chan)))

(defn- get-logs* ;TODO: attach spec
  ([decoder filter]
   (get-logs* decoder filter 1000))
  ([decoder filter limit]
   (let [ch (chan 2 (map decoder))]
     (p/chain @logs-collection
              #(.find % (clj->js filter))
              #(.sort % #js { :blockNumber -1 })
              #(.limit % limit)
              #(utils/cursor->chan % ch))
     ch)))


(def get-latest-log
  "return lates saved log in db"
  (partial get-logs* identity #js{} 1))

(defn get-latest-block-number
  "return bock number for latest saved log in db"
  []
  (go (if-let [log (<! (get-latest-log))]
        (or (.-blockNumber log) 0)
        0)))

(defn get-logs-by-abi
  "return newest 1000 block for given `abi` and `address`, `address` ignored if equal `nil`"
  [abi address]
  (let [signature (web3event/abi->signature abi)
        decoder   (web3event/create-decoder abi)
        filter    (if (nil? address)
                    {:signature signature}
                    {:signature signature
                     :address   address})]
    (println signature)
    (get-logs* decoder filter)))
