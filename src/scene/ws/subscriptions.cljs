(ns scene.ws.subscriptions
  (:require [scene.interop :as interop]
            [scene.protocols :as protocols]
            [scene.utils :as utils]
            [scene.web3.event :refer [abi->signature create-decoder]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn- create-sender [abi ws]
  (let [decoder (create-decoder abi)]
    (fn [log]
      (->>
       (decoder log)
       utils/js->json
       (.send ws)))))

(defn- create-sub-id [& {:keys [abi address]}]
  {:signature (if abi
                (abi->signature abi)
                abi)
   :address   address})

(defn- cleanup-fn [k]
  (fn [m]
    (if (empty? (get m k))
      (dissoc m k)
      m)))

(defn- unsubscribe-by-sub-id [registry id sub-id]
  (-> registry
      (update-in [:sub-id->conns sub-id] dissoc id)
      (update :sub-id->conns (cleanup-fn sub-id))
      (update-in [:conn-id->subs-id id] #(disj % sub-id))
      (update :conn-id->subs-id (cleanup-fn id))))

(defn- unsubscribe* [registry id abi address]
  (let [sub-id  (create-sub-id :abi abi :address address)]
    (unsubscribe-by-sub-id registry id sub-id)))

(defn unsubscribe [registry id abi address]
  (swap! registry unsubscribe* id abi address))

(defn- unsubscribe-all* [registry id]
  (reduce (fn [reg sub-id]
            (unsubscribe-by-sub-id reg id sub-id))
          registry
          (get-in registry [:conn-id->subs-id id] #{})))

(defn unsubscribe-all [registry id]
  (swap! registry unsubscribe-all* id))

(defn- subscribe* [registry id ws abi address]
  (let [sub-id (create-sub-id :abi abi :address address)]
    (if (contains? (get-in registry [:conn-id->subs-id id] #{})
                   sub-id)
      registry
      (-> registry
          (update-in [:conn-id->subs-id id] (fn [subs]
                                              (if subs
                                                (conj subs sub-id)
                                                (set [sub-id]))))
          (update-in [:sub-id->conns sub-id] (fn [conns]
                                               (if conns
                                                 (assoc conns id (create-sender abi ws))
                                                 {id (create-sender abi ws)})))))))

(defn subscribe [registry id conn abi address]
  (swap! registry subscribe* id conn abi address))

;; {:sub-id->conns    {{:signature "0x<signature>"
;;                      :address   "0x<contract address>"} {"<connection id>" <JS: connection object>}}
;;  :conn-id->subs-id {"<connection id>" #{{:signature "0x<signature>"
;;                                          :address   "0x<contract address>"}}}}
(def empty-subscription-registry
  {:sub-id->conns    {}
   :conn-id->subs-id {}})

(defn create-subscription-registry []
  (let [registry (atom empty-subscription-registry)]
    (reify
      protocols/IDataProvider
      (data [_] registry))))

(defn- send-log [registry log]
  (let [address   (interop/get-address log)
        signature (interop/get-topic log 0)
        conns     (mapcat (fn [sub-id]
                            (vals (get-in @registry [:sub-id->conns sub-id])))
                          [{:signature signature
                            :address   address}
                           {:signature signature
                            :address nil}])]
    (doseq [send conns] (send log))))

(defn send-logs [registry logs]
  (go
    (.map logs #(send-log registry %))))
