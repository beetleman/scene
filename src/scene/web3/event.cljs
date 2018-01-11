(ns scene.web3.event
  (:require [clojure.spec.alpha :as s]
            [scene.spec :refer [signature-gen]]))

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

(s/fdef Ox
        :args (s/cat :s string?)
        :ret string?)


(defn abi->signature
  "create event signature for given `event-abi` from given ABI"
  [event-abi]
  (-> (.transformToFullName utils (clj->js event-abi))
      sha3
      Ox))

(def signature-regex #"^0x[a-fA-F0-9]{64}$")
(s/def ::signature (s/with-gen (s/and string?
                                      #(re-matches signature-regex %))
                     signature-gen))
(def valid-solidity-name-regex #"^[a-zA-Z_][a-zA-Z0-9_]*$")
(s/def ::valid-solidity-name (s/and string?
                                    #(re-matches valid-solidity-name-regex %)
                                    #(> (count %) 0)))
(s/def ::anonymous boolean?)
(s/def ::name ::valid-solidity-name)
(s/def ::type ::valid-solidity-name)
(s/def ::indexed boolean?)
(s/def ::input (s/keys :req-un [::name
                                ::indexed
                                ::type]))
(s/def ::inputs (s/coll-of ::input))
(s/def ::event-abi (s/keys :req-un [::anonymous
                                    ::inputs
                                    ::name
                                    ::type]))
(s/fdef abi->signature
        :args (s/cat :event-abi ::event-abi)
        :ret ::signature)
