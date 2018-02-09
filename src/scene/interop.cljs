(ns scene.interop
  (:require [goog.object :as gobj]))

(defn get-topic [log n]
  (gobj/get log "topics" n))

(defn get-address [log]
  (gobj/get log "address"))

(defn get-block-number [log]
  (gobj/get log "blockNumber"))

(defn get-log-index [log]
  (gobj/get log "logIndex"))

(defn get-id [log]
  (gobj/get log "_id"))

(defn js-merge [obj data]
  (.assign js/Object
           #js {}
           obj
           data))
