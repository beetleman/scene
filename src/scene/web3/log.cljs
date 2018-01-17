(ns scene.web3.log
  (:require [clojure.core.async :as a :refer [<! >! chan put!]]
            [clojure.spec.alpha :as s]
            [scene.utils :as utils]
            [taoensso.timbre :refer-macros [info]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defprotocol IStoppable
  (stop [this] "stop it"))

(defprotocol IDataProvider
  (data [this] "provide chan with data"))

(defrecord DataGetter [data-ch poison-ch]
  IStoppable
  (stop [_] (put! poison-ch :stop))
  IDataProvider
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

(s/def ::fromBlock pos-int?)
(s/def ::toBlock pos-int?)
(s/fdef create-block-ranges
        :args (s/and (s/cat :from ::fromBlock
                            :to ::toBlock
                            :step pos-int?)
                     (fn [{from :from to :to}] (<= from to)))
        :ret (s/coll-of (s/keys :req-un [::fromBlock ::toBlock]))
        :fn #(>= (- (-> % :args :to inc) (-> % :args :from))
                 (-> % :ret count)))


(defn create-block-ranges-getter
  "create 'IStoppable' 'IDataProvider' with block ranges
  from `from-ch` to 'latest' with max `step`"
  [web3 from-ch step]
  (let [ch           (chan 1)
        block-number #(go (-> web3
                              current-block-number
                              <!
                              :data))
        poison-ch    (chan 1)]
    (go-loop [from (<! from-ch)
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
        (recur (inc to) (a/alts! [(a/timeout 500) poison-ch]))))
    (->DataGetter ch poison-ch)))

(defn create-log-getter
  "create log getter geting block for givent `ranges` vector
  and put then to `DataGetter` using `buffer`"
  [web3 ranges-ch buffer]
  (let [result-ch (chan 1)
        logs-ch   (chan buffer)
        poison-ch (chan 1)]
    (go-loop [[v c] (a/alts! [ranges-ch poison-ch])]
      (when-let [range (and (not= c poison-ch) v)]
        (.get (.filter (.. web3 -eth)
                       (clj->js range))
              (utils/callback-chan-fn result-ch))
        (info "getting logs for range" range)
        (>! logs-ch (<! result-ch))
        (recur (a/alts! [ranges-ch poison-ch]))))
    (->DataGetter logs-ch poison-ch)))


(defn create-data-handler
  "pass all data from IDataProvider `d` to `handler` function"
  [provider handler]
  (let [poison-ch (chan 1)]
    (go-loop [[{d :data} c] (a/alts! [poison-ch (data provider)])]
      (when-not (= c poison-ch)
        (<! (handler d))
        (recur (a/alts! [poison-ch (data provider)]))))
    (reify IStoppable
           (stop [_] (put! poison-ch :stop)))))
