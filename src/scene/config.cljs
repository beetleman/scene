(ns scene.config
  (:require [macchiato.env :as config]
            [mount.core :refer [defstate]]))

(defstate env :start (config/env))

(def redis-url (get @env :redis-url "redis://localhost:6379/0"))
(def rpc-url (get @env :rpc-url "http://localhost:8545"))
