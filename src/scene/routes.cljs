(ns scene.routes
  (:require [bidi.bidi :as bidi]
            [clojure.spec.alpha :as s]
            [macchiato.util.response :as r]
            [scene.db :as db]
            [scene.web3.event :as event]
            [scene.web3.event :as web3event]))

;; -- utils

(defn parse-error [{problems :cljs.spec.alpha/problems}]
  {:error (map (fn [{:keys [pred path]}]
                    {:path path :is-not pred})
                  problems)})


;; -- routes handlers

(defn health [req res raise]
  (-> {:ok true}
      r/ok
      res))

(defn not-found [req res raise]
  (let [method (-> req :request-method name str clojure.string/upper-case)
        url    (:uri req)]
    (-> {:msg (str "\"" method "\" on \"" url "\" was not found")}
        r/not-found
        res)))

(defn events [req res raise]
  (let [{abi               :body
         {:keys [address]} :params} req]
    (if-let [validation-error (s/explain-data ::event/event-abi abi)]
      (-> validation-error
          parse-error
          r/bad-request
          res)
        (let [decoder (web3event/create-decoder abi)
              getter  (if address
                        (partial db/get-logs decoder address)
                        (partial db/get-logs decoder))]
          (-> abi
              web3event/abi->signature
              getter
              r/json
              res)))))


;; -- routes

(def routes
  ["/"
   [["" {:get health}]
    ["events" {:post events}]]])


(defn router [req res raise]
  (if-let [{:keys [handler route-params]} (bidi/match-route* routes (:uri req) req)]
    (handler (assoc req :route-params route-params) res raise)
    (not-found req res raise)))
