(ns scene.config
  (:require [macchiato.env :as config]
            [clojure.spec.alpha :as s]
            [mount.core :refer [defstate]]))

(def chunk-rex #"[1-9][0-9]*")
(s/def ::chunk-size (s/and string?
                           (complement empty?)
                           #(re-matches chunk-rex %)))

(def db-name-rex #"[^$][^.]*")
(s/def ::db-name (s/and string?
                        (complement empty?)
                        #(re-matches db-name-rex %)))

(def url-rex #"[^\s]+")
(s/def ::url (s/and string?
                    (complement empty?)
                    #(re-matches url-rex %)))

(s/def ::rpc-url ::url)

(s/def ::mongo-url ::url)

(s/def ::config (s/keys :opt-un [::mongo-url
                                 ::db-name
                                 ::rpc-url
                                 ::chunk-size]))


(defstate env :start (let [raw (config/env)
                           ret (s/conform ::config raw)]
                       (when (s/invalid? ret)
                         (js/console.error "invalid config file or environment variables")
                         (js/console.error (s/explain-str ::config raw))
                         (js/process.exit 1))
                       ret))


(defn parse-number [s]
  (.parseInt js/Number s))

(def mongo-url (get @env :mongo-url "mongodb://localhost:27017/scene"))
(def db-name (get @env :db-name "scene"))
(def rpc-url (get @env :rpc-url "http://localhost:8545"))
(def chunk-size (parse-number (get @env :chunk-size 10000)))
