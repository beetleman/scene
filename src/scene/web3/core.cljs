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
  "start log getter which gets logs for ranges provided by `ranges-ch`"
  [ranges-ch]
  (let [getter-ch (chan config/chunk-size)
        mult-ch   (mult getter-ch)]
    (assoc (log/create-log-getter web3 ranges-ch getter-ch)
           :mult-ch
           mult-ch)))

(defstate log-ranges-getter
  :start (log/create-block-ranges-getter web3 0 config/chunk-size)
  :stop (log/stop @log-ranges-getter))


(defstate log-getter
  :start (start-log-getter (log/data @log-ranges-getter))
  :stop (log/stop @log-getter))


(defstate log-getter-saver
  :start (log/create-log-handler (get-chan @log-getter)
                                 db/save-logs
                                 false)
  :stop ((:stop @log-getter-saver)))
