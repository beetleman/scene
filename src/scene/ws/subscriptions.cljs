(ns scene.ws.subscriptions
  (:require [mount.core :refer [defstate]]
            [scene.protocols :as protocols]))

(defn remove [subs id]
  (swap! subs dissoc id))

(defn add [subs id abi conn]
  (swap! subs assoc id data))

(defn create-subscription-registry []
  (let [registry (atom {:connection {}
                        :sub        #{}})]
    (reify
      protocols/IDataProvider
      (data [_] registry))))


(defn create-subscription-handler [logs-chan registry]
;; connecto to subs
  (reify
    protocols/IStoppable
    (stop [_] nil)))
