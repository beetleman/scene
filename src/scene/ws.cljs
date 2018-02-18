(ns scene.ws
  (:require [scene.utils :as utils]
            [scene.spec :refer [json-conformer]]
            [clojure.spec.alpha :as s]))

(defn error [msg]
  {:mgs msg :type :error})

(defn msg [msg]
  {:msg msg :type :msg})

(defmulti on-message :type)

(defmethod on-message "echo" [{:keys [payload]}]
  (str "got message: " payload))

(defmethod on-message :default [_]
  (error "unknow message or wrong message"))


(s/def ::type #{"echo"})
(s/def ::payload (complement nil?))
(s/def ::parsed-message (s/keys :req-un [::type
                                         ::payload]))
(s/def ::message (s/and string?
                        json-conformer
                        ::parsed-message))

(defn handler [websocket message]
  (->> message
       (s/conform ::message)
       on-message
       utils/clj->json
       (.send websocket)))
