(ns scene.web3.core
  (:require [mount.core :refer [defstate]]
            [taoensso.timbre :refer-macros [info]]
            [clojure.core.async
             :as a
             :refer [put! >! <! chan sliding-buffer close! alts! timeout]]
            [scene.config :as config]
            [scene.db :as db]
            [scene.web3.log :as log])
    (:require-macros [cljs.core.async.macros :refer [go-loop]]))

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

(defstate log-watcher-saver
  :start (let [running  (atom true)
               info-log (fn [log]
                          (info (str "current block: "
                                     (:blockNumber log))))]
           (go-loop []
             (let [log (-> @log-watcher :chan (<!) :data)]
               (info-log log)
               (doseq [key-fn [db/log-address-key db/log-topic-key]]
                 (<! (db/save-log log key-fn)))
               (when @running (recur))))
           {:stop #(reset! running false)})
  :stop ((:stop @log-watcher-saver)))
