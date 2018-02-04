(ns scene.middleware
  (:require [clojure.core.async.impl.channels :refer [ManyToManyChannel]]
            [clojure.core.async :refer [<!]]
            [macchiato.http :as http]
            [macchiato.middleware.defaults :as defaults]
            [macchiato.middleware.restful-format :as rf]
            [scene.utils :as utils])
  (:require-macros [cljs.core.async.macros :refer [go-loop go]]))


;; overuse of multi arity to detect if first element is procesed
                                        ;TODO: abstract away core of thi fuction
(defn- from-chan-to-respone
  ([ch node-server-response raise]
   (.write node-server-response "["
           #(from-chan-to-respone ch node-server-response raise "")))
  ([ch node-server-response raise separator]
   (go (let [part (<! ch)]
         (cond
           (nil? part)
           (.write node-server-response
                   "]"
                   #(.end node-server-response))

           (contains? part :error)
           (do
             (.end node-server-response)
             (raise (get part :error)))

           :else
           (.write node-server-response
                   (str separator
                        (utils/js->json part))
                   #(from-chan-to-respone ch
                                          node-server-response
                                          raise
                                          ",")))))))

(extend-type ManyToManyChannel
  http/IHTTPResponseWriter
  (-write-response [ch node-server-response raise]
    (from-chan-to-respone ch node-server-response raise)))

(defn wrap-defaults [handler]
  (-> handler
      (defaults/wrap-defaults defaults/api-defaults)
      (rf/wrap-restful-format {:keywordize? true})))
