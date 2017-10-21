(ns scene.web3.core
  (:require [mount.core :refer [defstate]]
            [clojure.core.async
             :as a
             :refer [put! >! <! chan sliding-buffer close! alts! timeout]]
            [scene.config :as config]
            [scene.web3.log :as log])
    (:require-macros [cljs.core.async.macros :refer [go-loop go]]))

(def Web3 (js/require "web3"))

(def httpProvider (Web3.providers.HttpProvider. config/rpc-url))
(def web3 (Web3. httpProvider))

;; for debuging
(set! (.-web3 js/global) web3)

(defn start-log-getter
  "start log getter which gets logs from 0 to `to-block`"
  [to-block])

(defn start-log-watcher
  "start watching ethereum log from latest block"
  []
  (let [ch (chan)
        watcher (log/create-watcher web3 ch "latest")]
    {:chan ch :stop #(.stopWatching watcher)}))

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
