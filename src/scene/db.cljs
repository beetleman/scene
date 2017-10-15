(ns scene.db
  (:require [mount.core :refer [defstate]]
            [scene.config :as config]))

(def redis (js/require "redis"))

(defstate conn
  :start (.createClient redis config/redis-url)
  :stop (.quit @conn))
