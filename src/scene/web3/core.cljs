(ns scene.web3.core
  (:require [mount.core :refer [defstate]]
            [taoensso.timbre :refer-macros [info]]
            [clojure.core.async
             :as a
             :refer [put! >! <! chan sliding-buffer close! alts! timeout mult tap]]
            [scene.config :as config]
            [scene.db :as db]
            [scene.web3.log :as log])
    (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def Web3 (js/require "web3"))

(def httpProvider (Web3.providers.HttpProvider. config/rpc-url))
(def web3 (Web3. httpProvider))

(defn get-chan
  "get chan from state with mutl chan"
  ([s]
   (get-chan s (chan)))
  ([s ch]
   (tap (:mult-ch s) ch)
   ch))

(defn start-log-getter
  "start log getter which gets logs from 0 to `to-block`"
  [to-block-ch]
  (let [getter-ch (chan config/chunk-size)
        mult-ch   (mult getter-ch)]
    (assoc (log/create-log-getter web3 to-block-ch getter-ch config/chunk-size)
           :mult-ch
           mult-ch)))

(defn start-log-watcher
  "start watching ethereum log from latest block"
  []
  (let [watch-ch      (chan)
        mult-ch       (mult watch-ch)
        last-block-ch (chan (sliding-buffer 1))
        watcher       (log/create-watcher web3 watch-ch "latest")]
    (tap mult-ch last-block-ch)
    {:mult-ch              mult-ch
     :last-block-number-ch (log/last-block-number last-block-ch)
     :stop                 #(.stopWatching watcher)}))

(defstate log-watcher
  :start (start-log-watcher)
  :stop ((:stop @log-watcher)))


(defstate log-getter
  :start (start-log-getter (:last-block-number-ch @log-watcher))
  :stop ((:stop @log-getter)))


(defstate log-watcher-saver
  :start (log/create-log-handler (get-chan @log-watcher)
                                 db/save-log)
  :stop ((:stop @log-watcher-saver)))

(defstate log-getter-saver
  :start (log/create-log-handler (get-chan @log-getter)
                                 db/save-logs
                                 false)
  :stop ((:stop @log-watcher-saver)))
