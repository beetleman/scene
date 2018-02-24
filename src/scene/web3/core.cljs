(ns scene.web3.core
  (:require [mount.core :refer [defstate]]
            [scene.config :as config]
            [scene.db :as db]
            [scene.protocols :as protocols]
            [scene.web3.log :as log]))

(def Web3 (js/require "web3"))

(def httpProvider (Web3.providers.HttpProvider. config/rpc-url))
(def web3 (Web3. httpProvider))

(defn start-log-getter
  "start log getter which gets logs for ranges provided by `ranges-ch`"
  [ranges-ch]
  (log/create-log-getter web3 ranges-ch config/chunk-size))


(defstate log-ranges-getter
  :start (log/create-block-ranges-getter web3
                                         (db/get-latest-block-number)
                                         config/chunk-size)
  :stop (protocols/stop @log-ranges-getter))


(defstate log-getter
  :start (start-log-getter (protocols/data @log-ranges-getter))
  :stop (protocols/stop @log-getter))


(defstate log-getter-saver
  :start (log/create-data-handler @log-getter
                                  db/save-logs)
  :stop (protocols/stop @log-getter-saver))
