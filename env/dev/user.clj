(ns user
  (:require [figwheel-sidecar.repl-api :as ra]))

(defn cljs []
  (ra/cljs-repl))

(defn start-fw []
  (ra/start-figwheel!))

(defn stop-fw []
  (ra/stop-figwheel!))

(defn restart-fw []
  (stop-fw)
  (start-fw))

(defn quick-dev []
  (restart-fw)
  (cljs))
