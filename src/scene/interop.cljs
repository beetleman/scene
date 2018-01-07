(ns scene.interop)

(defn get-topic [log n]
  (aget log "topics" n))

(defn get-address [log]
  (aget log "address"))

(defn get-block-number [log]
  (aget log "blockNumber"))

(defn get-log-index [log]
  (aget log "logIndex"))

(defn get-id [log]
  (aget log "_id"))

(defn js-merge [obj data]
  (.assign js/Object
           #js {}
           obj
           data))
