(ns scene.config
  (:require [macchiato.env :as config]
            [mount.core :refer [defstate]]))

(defstate env :start (config/env))

(defn parse-number [s]
  (.parseInt js/Number s))

(def redis-url (get @env :redis-url "redis://localhost:6379/0"))
(def couchdb-url (get @env :couchdb-url "http://localhost:5984"))
(def rpc-url (get @env :rpc-url "http://localhost:8545"))
(def chunk-size (parse-number (get @env :chunk-size 100000)))
(def key-prefix (get @env :key-prefix "scene"))
