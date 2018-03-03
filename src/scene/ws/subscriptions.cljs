(ns scene.ws.subscriptions
  (:require [mount.core :refer [defstate]]
            [scene.web3.event :refer [abi->signature]]
            [scene.protocols :as protocols]))

(defn- unsubscribe* [registry id]
  )

(defn unsubscribe [registry id]
  (swap! registry unsubscribe*))

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
                                                 (conj conns conn)
                                                 [conn])))))))

(defn subscribe [subs id conn abi]
  )


(def empty-subscription-registry
  {:sub-id->conns     {}
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
