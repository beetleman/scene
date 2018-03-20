(ns scene.ws.handler
  (:require [clojure.spec.alpha :as s]
            [clojure.core.async :as async]
            [scene.spec :refer [json-conformer]]
            [scene.utils :as utils]
            [scene.web3.event :as event]
            [scene.ws.msg :as msg]
            [scene.db :as db]
            [scene.ws.subscriptions :as subscriptions])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn send [ws msg]
  (->> msg
      utils/clj->json
      (.send ws)))

(defmulti on-message (fn [_ msg] (:type msg)))

(defmethod on-message "echo" [_ {:keys [payload]}]
  (msg/generic :echo payload))

(defmethod on-message "subscribe" [{:keys [id registry ws]}
                                   {{:keys [abi address]} :payload :as payload}]
  (subscriptions/subscribe registry id ws abi address)
  (go
    (->>
     (db/get-logs-by-abi abi address)
     (async/into [])
     async/<!
     (msg/logs-snapshot abi)
     (send ws)))
  (msg/subscribed payload))

(defmethod on-message "unsubscribe" [{:keys [id registry ws]}
                                     {{:keys [abi address]} :payload :as payload}]
  (subscriptions/unsubscribe registry id abi address)
  (msg/unsubscribed payload))

(defmethod on-message :default [_ _]
  (msg/error "unknow message or wrong message"))


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
         (send ws))))
