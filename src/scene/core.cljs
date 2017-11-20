(ns scene.core
  (:require
    [scene.config :refer [env]]
    [scene.middleware :refer [wrap-defaults]]
    [scene.routes :refer [router]]
    [macchiato.server :as http]
    [macchiato.middleware.session.memory :as mem]
    [mount.core :as mount :refer [defstate]]
    [taoensso.timbre :refer-macros [log trace debug info warn error fatal]]))

(defn server []
  (mount/start)
  (let [host (or (:host @env) "0.0.0.0")
        port (or (some-> @env :port js/parseInt) 3000)]
    (http/start
     {:handler    (wrap-defaults router)
      :host       host
      :port       port
      :on-success #(info "scene started on" host ":" port)})))
