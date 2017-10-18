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


(defn start-log-watcher []
  (let [ch (chan)
        f (.filter (.. web3 -eth)
                   #js{:fromBlock "latest"})]
    (.watch f #(put! ch (js->clj {:error %1 :data %2})))
    {:chan ch :stop #(.stopWatching f)}))


(defstate log-watcher
  :start (start-log-watcher web3)
  :stop ((:stop @log-watcher)))


(defstate log-loger
  :start (let [running (atom true)]
           (go-loop []
             (->> @log-watcher
                  :chan
                  (<!)
                  :data
                  clj->js
                  (.log js/console))
             (when @running (recur)))
          {:stop #(reset! running false)})
  :stop ((:stop @log-loger)))
