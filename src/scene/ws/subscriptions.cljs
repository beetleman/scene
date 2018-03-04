(ns scene.ws.subscriptions
  (:require [mount.core :refer [defstate]]
            [scene.web3.event :refer [abi->signature]]
            [scene.protocols :as protocols]))

(defn- cleanup-fn [k]
  (fn [m]
    (if (empty? (get m k))
      (dissoc m k)
      m)))

(defn- unsubscribe* [registry id abi address]
  (let [sub-id  (create-sub-id :abi abi :address address)
        subs-id (get-in registry [:conn-id->subs-id id] #{})]
    (-> registry
        (update-in [:sub-id->conns sub-id] dissoc id)
        (update :sub-id->conns (cleanup-fn sub-id))
        (update-in [:conn-id->subs-id id] #(disj % sub-id))
        (update :conn-id->subs-id (cleanup-fn id)))))

(defn unsubscribe [registry id abi address]
  (swap! registry unsubscribe* id abi address))

#_{:subs {[signature address]   [cons]
          {:signature :address} [cons]}
   :cons {"id" [subs-keys]}}

(defn- create-sub-id [& {:keys [abi address]}]
  {:signature (if abi
                (abi->signature abi)
                abi)
   :address   address})

(defn- subscribe* [registry id conn abi address]
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
                                                 (assoc conns id conn)
                                                 {id conn})))))))

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

(defn create-subscription-handler [logs-chan registry]
;; connecto to subs
  (reify
    protocols/IStoppable
    (stop [_] nil)))
