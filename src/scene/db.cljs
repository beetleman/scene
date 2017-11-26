(ns scene.db
  (:require [mount.core :refer [defstate]]
            [clojure.core.async
             :as a
             :refer [put! >! <! chan sliding-buffer close! alts! timeout]]
            [scene.utils :as utils]
            [scene.config :as config])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def redis (js/require "redis"))

(defstate conn
  :start (.createClient redis config/redis-url)
  :stop (.quit @conn))


(defn redis-key
  "produce key with prefix"
  [s]
  (str config/key-prefix ":" s))

(defn topic-key [t]
  (redis-key (str "topic:" t)))

(defn address-key [a t]
  (str (topic-key t) ":address:" a))

(defn get-topic [log]
  (->> log
       :topics
       first))

(defn get-address [log]
  (:address log))

(defn log->address-key
  "produce redis key for given `log` map, based on contract address"
  [log]
  (address-key (get-address log) (get-topic log)))

(defn log->topic-key
  "produce redis jey for given `log` map, based on topic"
  [log]
  (topic-key (get-topic log)))

(defn save-log
  "save `log` in db, if `key` not provided function will generate keys and save data"
  ([log]
   (go
     (doseq [kf [log->address-key log->topic-key]]
       (-> (kf log)
           (save-log log)
           <!))))
  ([key log]
   (let [ch (chan)]
     (.hmset @conn
             key
             (:blockNumber log)
             (utils/clj->json log)
             (utils/callback-chan-fn ch))
     ch)))


(defn parse-event [[address raw-event]]
  {:blockNumber address
   :event       (utils/json->clj raw-event)})

(defn get-log
  "get log by `key` (any key) and return"
  [key]
  (let [ch (chan)]
    (.hgetall @conn key (utils/callback-chan-fn ch))
    ch))
