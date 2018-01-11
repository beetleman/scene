(ns scene.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.spec.gen.alpha :as gen]
            [scene.utils :refer [int->hex]]))


(defn hex-generator-fn [min-len]
  (fn []
    (gen/fmap #(str "0x" (int->hex % (- min-len 2)))
              (s/gen pos-int?))))

(def signature-gen (hex-generator-fn 66))
(def address-gen (hex-generator-fn 42))
