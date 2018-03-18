(ns scene.ws.handler
  (:require [scene.utils :as utils]
            [scene.spec :refer [json-conformer]]
            [scene.web3.event :as event]
            [scene.ws.subscriptions :as subscriptions]
            [clojure.spec.alpha :as s]))

(defn msg [type payload]
  {:type type :payload payload})

(defn error [payload]
  (msg :error payload))

(defn subscribed [abi]
  (msg :subscribed abi))

(defn unsubscribed [abi]
  (msg :unsubscribed abi))

(defmulti on-message (fn [_ msg] (:type msg)))

(defmethod on-message "echo" [_ {:keys [payload]}]
  (msg :echo payload))

(defmethod on-message "subscribe" [{:keys [id registry ws]}
                                   {{:keys [abi address]} :payload :as payload}]
  (subscriptions/subscribe registry id ws abi address)
  (subscribed payload))

(defmethod on-message "unsubscribe" [{:keys [id registry ws]}
                                     {{:keys [abi address]} :payload :as payload}]
  (subscriptions/unsubscribe registry id abi address)
  (unsubscribed payload))


(defmethod on-message :default [_ _]
  (error "unknow message or wrong message"))


(s/def :payload/address string?)
(s/def :payload/abi ::event/event-abi)

(s/def :echo/payload string?)
(s/def :subscribe/payload (s/keys :req-un [:payload/abi]
                                  :opt-un [:payload/address]))
(s/def :unsubscribe/payload (s/keys :req-un [:payload/abi]
                                    :opt-un [:payload/address]))

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
