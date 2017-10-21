(ns scene.utils
  (:require [clojure.core.async :refer [<! chan put!]])
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
