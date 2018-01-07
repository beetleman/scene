(ns scene.web3.log
  (:require [clojure.core.async
             :as a
             :refer [put! >! <! chan sliding-buffer pipe]]
            [taoensso.timbre :refer-macros [info error]]
            [scene.utils :as utils])
  (:require-macros [cljs.core.async.macros :refer [go-loop go]]))

(defprotocol Stoppable
  (stop [this] "stop it"))

(defprotocol DataProvider
  (data [this] "provide chan with data"))

(defrecord DataGetter [data-ch poison-ch]
  Stoppable
  (stop [_] (put! poison-ch :stop))
  DataProvider
  (data [_] data-ch))

(defn current-block-number
  "return chan with last block number"
  [web3]
  (let [ch (chan 1)]
    (.getBlockNumber (.-eth web3)
                     (utils/callback-chan-fn ch))
    ch))

(defn create-block-ranges
  "create block ranges for `web3.eth.get`"
  [from to step]
  (map #(hash-map :fromBlock (first %) :toBlock (last %))
       (partition-all step (range from (inc to)))))

(defn create-block-ranges-getter
  "create 'Stoppable' 'DataProvider' with block ranges
  from `from` to 'latest' with max `step`"
  [web3 from step]
  (let [ch           (chan 1)
        block-number #(go (-> web3
                              current-block-number
                              <!
                              :data))
        poison-ch    (chan 1)]
    (go-loop [from from
              [_ c] [nil nil]]
      ;; stream ranges to chan
      (when-let [to (and (not= c poison-ch)
                         (<! (block-number)))]
        (when (<= from to)
          (<! (a/onto-chan ch
                           (create-block-ranges from
                                                to
                                                step)
                           false)))
        (recur (inc to) (a/alts! [(a/timeout 100) poison-ch]))))
    (->DataGetter ch poison-ch)))

(defn create-log-getter
  "create log getter geting block for givent `ranges` vector
  and put them on `logs-ch` chan as vectors of logs"
  [web3 ranges-ch logs-ch]
  (let [result-ch (chan)
        poison-ch (chan 1)]
    (go-loop [[v c] (a/alts! [ranges-ch poison-ch])]
      (when-let [range (and (not= c poison-ch) v)]
        (-> (.filter (.. web3 -eth)
                     (clj->js range))
            (.get (utils/callback-chan-fn result-ch)))
        (info "getting logs for range" range)
        (>! logs-ch (<! result-ch))
        (recur (a/alts! [ranges-ch poison-ch]))))
    (->DataGetter logs-ch poison-ch)))


(defn create-log-handler
  "read all logs from given `ch` channel and run `handler` function on them"
  ([ch handler]
   (create-log-handler ch handler true))
  ([ch handler logging?]
   (let [running (atom true)
         logging (if logging?
                   (fn [log]
                     (info (str "handling log fron block number: " (aget log "blockNumber"))))
                   (constantly nil))]
     (go-loop []
       (let [log (-> ch
                     <!
                     :data)]
         (<! (handler log))
         (logging log)
         (when @running (recur))))
     {:stop #(reset! running false)})))
