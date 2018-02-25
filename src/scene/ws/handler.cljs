(ns scene.ws.handler
  (:require [scene.utils :as utils]
            [scene.spec :refer [json-conformer]]
            [scene.web3.event :as event]
            [clojure.spec.alpha :as s]))

(defn error [msg]
  {:mgs msg :type :error})

(defn msg [msg]
  {:msg msg :type :msg})

(defmulti on-message (fn [_ msg] (:type msg)))

(defmethod on-message "echo" [_ {:keys [payload]}]
  (str "got message: " payload))

(defmethod on-message "subscribe" [{:keys [id]}
                                   {:keys [payload]}]
  (str "id: " id))

(defmethod on-message "unsubscribe" [_ {:keys [payload]}]
  (str "got message: " payload))

(defmethod on-message :default [_ _]
  (error "unknow message or wrong message"))


;; TODO: multimethod spec

(s/def :echo/payload string?)
(s/def :subscribe/payload ::event/event-abi)
(s/def :unsubscribe/payload ::event/event-abi)

(defmulti parsed-message :type)
(defmethod parsed-message "echo" [_]
  (s/keys :req-un [::type :echo/payload]))

(defmethod parsed-message "subscribe" [_]
  (s/keys :req-un [::type :subscribe/payload]))

(defmethod parsed-message "unsubscribe" [_]
  (s/keys :req-un [::type :unsubscribe/payload]))


(s/def ::parsed-message (s/multi-spec parsed-message ::type))
(s/def ::message (s/and string?
                        json-conformer
                        ::parsed-message))


(defn create-handler [{:keys [ws] :as connection-request}]
  (fn [message]
    (->> message
         (s/conform ::message)
         (on-message connection-request)
         utils/clj->json
         (.send ws))))
