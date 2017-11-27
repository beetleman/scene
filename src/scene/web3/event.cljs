(ns scene.web3.event)

(def utils (js/require "web3/lib/utils/utils.js"))
(def sha3 (js/require "web3/lib/utils/sha3.js"))
(def Event (js/require "web3/lib/web3/event.js"))


(defn create-decoder
  "create log decoder for given `event-abi` from contract ABI"
  [event-abi]
  (let [event (Event. nil (clj->js event-abi) nil)]
    (fn [log]
      (-> (.decode event (clj->js log))
          (js->clj  :keywordize-keys true)))))

(defn Ox
  "add '0x' to string without it"
  [s]
  (if (clojure.string/starts-with? s "0x")
    s
    (str "0x" s)))


(defn abi->signature
  "create event signature for given `event-abi` from given ABI"
  [event-abi]
  (-> (.transformToFullName utils (clj->js event-abi))
      sha3
      Ox))
