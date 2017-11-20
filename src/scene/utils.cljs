(ns scene.utils
  (:require [clojure.core.async :refer [<! chan put! onto-chan]])
  (:require-macros [cljs.core.async.macros :refer [go-loop go]]))


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


(defn callback-chan-seq-fn
  "return node style callback function for lists of items"
  [ch]
  (fn [error data]
    (if error
      (put! ch (callback->clj error nil))
      (onto-chan ch (map #(callback->clj nil %) data) false))))

(defn clj->json
  "convert clojure object to json"
  [ds]
  (.stringify js/JSON (clj->js ds)))

(defn json->clj
  "convert json to clojure object"
  [s]
  (js->clj (.parse js/JSON s) :keywordize-keys true))
