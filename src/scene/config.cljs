(ns scene.config
  (:require [macchiato.env :as config]
            [mount.core :refer [defstate]]))

(defstate env :start (config/env))

(defn parse-number [s]
  (.parseInt js/Number s))

(def mongo-url (get @env :mongo-url "mongodb://localhost:27017/scene"))
(def db-name (get @env :db-name "scene"))
(def rpc-url (get @env :rpc-url "http://localhost:8545"))
(def chunk-size (parse-number (get @env :chunk-size 10000)))
(def key-prefix (get @env :key-prefix "scene"))
