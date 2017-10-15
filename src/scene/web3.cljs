(ns scene.web3
  (:require [mount.core :refer [defstate]]
            [scene.config :as config]))

(def Web3 (js/require "web3"))

(def httpProvider (Web3.providers.HttpProvider. config/rpc-url))
(def web3 (Web3. httpProvider))


(defn start-log-watcher [web3]
  (.subscribe web3.eth "logs"
              (clj->js {:fromBlock 0})
              console.log))


(defn stop-log-watcher [log-watcher]
  (.unsubscribe log-watcher))


(defstate log-watcher
  :start (start-log-watcher web3)
  :stop (stop-log-watcher @log-watcher))
