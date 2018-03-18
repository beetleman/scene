(ns scene.ws.core
  (:require [scene.ws.subscriptions :as subs]
            [scene.ws.middleware :refer [wrap-defaults]]
            [scene.protocols :as protocols]
            [scene.web3.core :refer [log-getter]]
            [scene.web3.log :refer [create-data-handler]]
            [scene.ws.handler :as handler]
            [mount.core :refer [defstate]]))

(defstate subscription-registry
  :start (subs/create-subscription-registry))

(defstate subscription-handler
  :start (create-data-handler @log-getter
                              #(subs/send-logs (protocols/data @subscription-registry)
                                               %1))
  :stop (protocols/stop @subscription-handler))


(defn websocket->connection-request
  [websocket]
  {:ws websocket})

(defn register-hooks [{:keys [ws id registry] :as connection-request}]
  (.on ws "close"
       #(subs/unsubscribe-all registry id))
  (.on ws "message"
       (handler/create-handler connection-request))
  connection-request)

(defn on-connect
  "setup connection"
  [websocket]
  (-> websocket
      websocket->connection-request
      (wrap-defaults (protocols/data @subscription-registry))
      register-hooks))
