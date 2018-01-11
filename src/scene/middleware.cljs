(ns scene.middleware
  (:require [clojure.core.async :refer [<!]]
            [clojure.core.async.impl.protocols :refer [Channel]]
            [macchiato.middleware.defaults :as defaults]
            [macchiato.middleware.restful-format :as rf])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn channel?
  "check if something is channel"
  [x]
  (satisfies? Channel x))

(defn auto-res [handler]
  (fn [req res raise]
    (let [r (handler req res raise)]
      (cond (iterable? r) (res r)
            (channel? r) (go (-> r
                                 <!
                                 res))
            :else r))))

(defn wrap-defaults [handler]
  (-> handler
      auto-res
      (rf/wrap-restful-format {:keywordize? true})
      (defaults/wrap-defaults defaults/site-defaults)))
