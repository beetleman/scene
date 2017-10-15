(ns scene.web3
  (:require [mount.core :refer [defstate]]
            [scene.config :as config]))

(def Web3 (js/require "web3"))

(defstate httpProvider
  :start (Web3.providers.HttpProvider. config/rpc-url))

(defstate web3
  :start (Web3. httpProvider))
