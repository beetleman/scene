(ns scene.web3.log
  (:require [clojure.core.async
             :as a
             :refer [put! >! <! chan sliding-buffer close! alts! timeout]]
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
               #js{:fromBlock from-block})
      (.watch (utils/callback-chan-fn ch))))


(defn last-block-number
  "return atom with last block number from `blocks-ch`"
  [blocks-ch]
  (let [block-number (atom 0)]
    (go-loop [current-block-number 0]
      (when (> current-block-number @block-number)
        (reset! block-number current-block-number))
      (recur (:blockNumber (<! blocks-ch))))
    block-number))


(defn create-block-ranges
  "create block ranges for `web3.eth.get`"
  [from to step]
  (map #(hash-map :fromBlock (first %) :toBlock (last %))
       (partition-all (inc step) (range from to))))

(defn create-log-getter
  "create log getter geting block givent in `ranges-ch` chan
  and put them on `logs-ch` chan"
  [web3 ranges-ch logs-ch]
  (go-loop [range (<! ranges-ch)
            result-ch (chan)]
    (-> (.filter (.. web3 -eth) (clj->js range))
        (.get (utils/callback-chan-fn result-ch)))
    (doseq [log (:data (<! result-ch))]  ;TODO: error handlig[ignore all errors for now]
      (>! logs-ch {:data log :erro nil}))
    (recur (<! ranges-ch) result-ch)))


;; TODO: REMOVE IT
(def state (atom nil))
(defn try-log-getter [web3]
  (let [ranges (chan)
        from 1000
        to 2000
        logs (chan 1000)
        all (set (range from to))
        get-number #(-> % :data :blockNumber)]
    (a/onto-chan ranges
                 (create-block-ranges from to 100)
                 false)
    (create-log-getter web3 ranges logs)
    (go-loop [ seen #{}]
      (reset! state (clojure.set/difference all seen))
      (recur  (conj seen (get-number (<! logs)))))))
