(ns scene.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.spec.gen.alpha :as gen]
            [scene.utils :refer [int->hex]]
            [clojure.core.async :as a]))


(defn hex-generator-fn [min-len]
  (fn []
    (gen/fmap #(str "0x" (int->hex % (- min-len 2)))
              (s/gen pos-int?))))

(def signature-gen (hex-generator-fn 66))
(def address-gen (hex-generator-fn 42))

(defn- name<M>-spec*
  ([name]
   (name<M>-spec* name 8 257))
  ([name from to]
   (name<M>-spec* name from to #(zero? (mod % 8)) #{name}))
  ([name from to filter-fn initial]
   (into initial (map #(str name %) (filter filter-fn (range from to))))))

(def uint<M>-spec (name<M>-spec* "uint"))
(def int<M>-spec (name<M>-spec* "int"))
(def bytes<M>-spec (name<M>-spec* "bytes" 1 33 (constantly true) #{}))
(def solidity-types-spec (clojure.set/union #{"address" "bool"}
                                            int<M>-spec
                                            bytes<M>-spec
                                            uint<M>-spec))

(s/def ::address-type #{"address"})
(s/def ::bool-type #{"bool"})
(s/def ::solidity-types solidity-types-spec)
