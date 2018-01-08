(ns scene.routes
  (:require [bidi.bidi :as bidi]
            [hiccups.runtime]
            [macchiato.util.response :as r]
            [taoensso.timbre :refer-macros [info]]
            [clojure.core.async :refer [<!]]
            [scene.db :as db]
            [scene.web3.core :as web3]
            [scene.web3.event :as web3event])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn health [req res raise]
  (r/ok {:ok true}))

(defn not-found [req res raise]
  (r/not-found {:msg (str "`" (:uri req) "` was not found")}))

(defn info-proxy [x]
  (info x)
  x)

(defn events [req res raise]
  (let [abi               (:body req)
        {:keys [address]} (:params req)
        decoder           (web3event/create-decoder abi)
        getter            (if address
                            (partial db/get-logs decoder address)
                            (partial db/get-logs decoder))]
    (go
      (let [e (-> abi
                  web3event/abi->signature
                  getter
                  <!)]
           (if (:data e)
             (r/ok e)
             (r/not-found e))))))

(def routes
  ["/"
   [["" {:get health}]
    ["events" {:get events}]]])


(defn router [req res raise]
  (if-let [{:keys [handler route-params]} (bidi/match-route* routes (:uri req) req)]
    (handler (assoc req :route-params route-params) res raise)
    (not-found req res raise)))
