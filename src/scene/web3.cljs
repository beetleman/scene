(ns scene.web3
  (:require [mount.core :refer [defstate]]
            [clojure.core.async
             :as a
             :refer [put! >! <! chan sliding-buffer close! alts! timeout]]
            [scene.config :as config]
            [scene.utils :as utils])
    (:require-macros [cljs.core.async.macros :refer [go-loop go]]))

(def Web3 (js/require "web3"))

(def httpProvider (Web3.providers.HttpProvider. config/rpc-url))
(def web3 (Web3. httpProvider))

(def state (atom {:last-block 0}))

;; for debuging
(set! (.-web3 js/global) web3)


(defn log-watch-callback-fn [ch last-block]
  (fn [error data]
    (let [r (utils/callback->clj error data)]
      (put! ch r)
      (when-not error
        (->> r :data :blockNumber (reset! last-block))))))


(defn create-log-watcher
  "create log watcher from `from-block` and put all arived dat into `ch`"
  [ch from-block]
  (-> (.filter (.. web3 -eth)
               #js{:fromBlock from-block})
      (.watch (utils/callback-chan-fn ch))))


(defn start-log-watcher
  "start watching ethereum log from latest block"
  []
  (let [ch (chan)
        watcher (create-log-watcher ch "latest")]
    {:chan ch :stop #(.stopWatching watcher)}))


(defn last-block-number
  "return atom with last block number from `ch`"
  [ch]
  (let [last-block (atom 0)]
    (go-loop []
      (let [block-number (-> ch <! :blockNumber)])
      (recur))
    last-block))


(defstate log-watcher
  :start (start-log-watcher)
  :stop ((:stop @log-watcher)))


(defstate log-loger
  :start (let [running (atom true)]
           (go-loop []
             (->> @log-watcher
                  :chan
                  (<!)
                  :data
                  :blockNumber
                  (.log js/console "block"))
             (when @running (recur)))
          {:stop #(reset! running false)})
  :stop ((:stop @log-loger)))
