(ns scene.web3.log
  (:require [clojure.core.async
             :as a
             :refer [put! >! <! chan sliding-buffer pipe]]
            [taoensso.timbre :refer-macros [info error]]
            [scene.utils :as utils])
  (:require-macros [cljs.core.async.macros :refer [go-loop go]]))


(defn watch-callback-fn [ch last-block]
  (fn [error data]
    (let [r (utils/callback->clj error data)]
      (put! ch r)
      (when-not error
        (->> r :data :blockNumber (reset! last-block))))))


(defn create-watcher
  "create log watcher from `from-block` and put all arived dat into `ch`"
  [web3 ch from-block]
  (-> (.filter (.. web3 -eth)
               #js{:fromBlock from-block}
               (fn [err] (when err (error err))))
      (.watch (utils/callback-chan-fn ch))))


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

(defn create-log-getter
  "create log getter geting block for givent `ranges` vector
  and put them on `logs-ch` chan as vectors of logs"
  [web3 to-block-ch logs-ch chunk-size]
  (let [result-ch (chan)
        running   (atom true)]
    (go
      (print (<! (current-block-number web3)))
      (let [ranges (create-block-ranges 0
                                        (-> to-block-ch
                                            <!
                                            :data)
                                        chunk-size)]
        (doseq [range ranges
                :when @running]
          (-> (.filter (.. web3 -eth)
                       (clj->js range))
              (.get (utils/callback-chan-fn result-ch)))
          (info "getting logs for range" range)
          (>! logs-ch (<! result-ch)))))
    {:stop #(reset! running false)}))


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
