(ns scene.core
  (:require [macchiato.server :as http]
            [mount.core :as mount]
            [scene.config :refer [env]]
            [scene.middleware :refer [wrap-defaults]]
            [scene.routes :refer [router]]
            [scene.web3.core]
            [taoensso.timbre :refer-macros [info]]))

(defn server []
  (mount/start)
  (let [host (or (:host @env) "0.0.0.0")
        port (or (some-> @env :port js/parseInt) 3000)]
    (http/start
     {:handler    (wrap-defaults router)
      :host       host
      :port       port
      :on-success #(info "scene started on" host ":" port)})))
