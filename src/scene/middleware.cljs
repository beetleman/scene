(ns scene.middleware
  (:require [clojure.core.async.impl.channels :refer [ManyToManyChannel]]
            [macchiato.http :as http]
            [macchiato.middleware.defaults :as defaults]
            [macchiato.middleware.restful-format :as rf]
            [scene.utils :as utils])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(extend-type ManyToManyChannel
  http/IHTTPResponseWriter
  (-write-response [ch node-server-response raise]
    (.write node-server-response "[")
    (go-loop [part (<! ch)
              separator ""]
      (cond
        (nil? part)
        (do
          (.write node-server-response "]")
          (.end node-server-response))

        (contains? part :error)
        (do
          (.end node-server-response)
          (raise (get part :error)))

        :else
        (do
          (.write node-server-response separator)
          (.write node-server-response (utils/js->json part))
          (recur (<! ch) ","))))))

(defn wrap-defaults [handler]
  (-> handler
      (defaults/wrap-defaults defaults/api-defaults)
      (rf/wrap-restful-format {:keywordize? true})))
