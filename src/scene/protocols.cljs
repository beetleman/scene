(ns scene.protocols)

(defprotocol IStoppable
  (stop [this] "stop it"))

(defprotocol IDataProvider
  (data [this] "provide chan with data"))
