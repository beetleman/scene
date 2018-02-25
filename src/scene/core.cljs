(ns scene.core
  (:require [macchiato.server :as http]
            [mount.core :as mount]
            [scene.config :refer [env]]
            [scene.middleware :refer [wrap-defaults]]
            [scene.routes :refer [router]]
            [scene.ws.core :as ws]
            [scene.web3.core]
            [taoensso.timbre :refer-macros [info]]))

(defn ws-handler [{:keys [websocket]}]
  (ws/on-connect websocket))

(defn server []
  (mount/start)
  (let [host   (or (:host @env) "0.0.0.0")
        port   (or (some-> @env :port js/parseInt) 3000)
        server (http/start
                {:handler    (wrap-defaults router)
                 :host       host
                 :port       port
                 :on-success #(info "scene started on" host ":" port)})]
    (http/start-ws server ws-handler)))
