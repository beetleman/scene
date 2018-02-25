(ns scene.ws.core
  (:require [scene.ws.subscriptions :as subs]
            [scene.ws.middleware :refer [wrap-defaults]]
            [scene.protocols :as protocols]
            [scene.web3.core :refer [log-getter]]
            [scene.ws.handler :as handler]
            [mount.core :refer [defstate]]))

(defstate subscription-registry
  :start (subs/create-subscription-registry))

(defstate subscription-handler
  :start (subs/create-subscription-handler @subscription-registry
                                           (protocols/data @log-getter))
  :stop (protocols/stop @subscription-handler))


(defn websocket->connection-request
  [websocket]
  {:ws websocket})

(defn register-hooks [{:keys [ws id registry] :as connection-request}]
  (.on ws "close"
       #(subs/remove registry id))
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
