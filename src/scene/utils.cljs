(ns scene.utils
  (:require [clojure.core.async
             :refer [<! chan]])
    (:require-macros [cljs.core.async.macros :refer [go-loop go]]))


(defn callback-log-fn [desc]
  (fn [error value]
    (if error
      (.error js/console desc value)
      (.log js/console desc value))))

(defn callback-log-chan [ch desc]
  (go-loop [msg (str "start '" desc "'channel logger")]
    (.log js/console msg)
    (recur (<! ch))))
