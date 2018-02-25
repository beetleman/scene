(ns scene.ws.middleware)

(defn ws-id [connection-request]
  (assoc connection-request :id (str (random-uuid))))

(defn attach-registry [connection-request registry]
  (assoc connection-request :registry registry))

(defn wrap-defaults [ws-request registry]
  (-> ws-request
      ws-id
      (attach-registry registry)))
