(ns scene.utils
  (:require [clojure.core.async :refer [chan put!]]
            [taoensso.timbre :refer-macros [error info]]))

(defn callback->clj
  "convert data from node style callback function to clojure"
  [error data]
  {:data data
   :error error})


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


(defn clj->json
  "convert clojure object to json"
  [ds]
  (.stringify js/JSON (clj->js ds)))


(defn logger
  "logger for `->` and `->>` macros"
  [x]
  (info x)
  x)


(defn logger-fn [desc]
  (fn [data]
    (info desc)
    data))


(defn int->hex
  ([n]
   (int->hex n 0))
  ([n pad-to]
   (-> (.toString n 16)
       (.padStart pad-to "0"))))
