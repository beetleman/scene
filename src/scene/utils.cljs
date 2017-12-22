(ns scene.utils
  (:require [clojure.core.async :refer [<! chan put! onto-chan]]
            [taoensso.timbre :refer-macros [error info]]
            [cognitect.transit :as t])
  (:require-macros [cljs.core.async.macros :refer [go-loop go]]))


(def clj->transit (partial t/write (t/writer :json)))
(def transit->clj (partial t/read (t/reader :json)))

(defn callback->clj
  "convert data from node style callback function to clojure"
  [error data]
  {:data (js->clj data :keywordize-keys true)
   :error (js->clj error :keywordize-keys true)})


(defn callback-chan-fn
  "return node style callback function"
  [ch]
  (fn [error data]
    (put! ch (callback->clj error data))))

(defn promise->chan
  "return channel with result from promise"
  [promise]
  (let [ch (chan 1)]
    (-> promise
        (.then (fn [data]
                 (put! ch {:data (js->clj data :keywordize-keys true)})))
        (.catch (fn [err]
                  (error err)
                  (put! ch {:error (js->clj err :keywordize-keys true)}))))
    ch))

(defn callback-chan-seq-fn
  "return node style callback function for lists of items"
  [ch]
  (fn [err data]
    (if err
      (do
        (error (js->clj err))
        (put! ch (callback->clj err nil)))
      (onto-chan ch (map #(callback->clj nil %) data) false))))


(defn clj->json
  "convert clojure object to json"
  [ds]
  (.stringify js/JSON (clj->js ds)))


(defn json->clj
  "convert json to clojure object"
  [s]
  (js->clj (.parse js/JSON s) :keywordize-keys true))

(defn logger
  "logger for `->` and `->>` macros"
  [x]
  (js/console.log x)
  x)
