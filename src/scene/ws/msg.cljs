(ns scene.ws.msg)

(defn generic [type payload]
  {:type type :payload payload})

(defn error [payload]
  (generic :error payload))

(defn subscribed [abi]
  (generic :subscribed abi))

(defn unsubscribed [abi]
  (generic :unsubscribed abi))

(defn logs [abi logs]
  (generic :logs {:abi abi :logs logs}))
