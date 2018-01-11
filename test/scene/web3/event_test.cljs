(ns scene.web3.event-test
  (:require [scene.web3.event :as sut]
            [cljs.test :as t :include-macros true]
            [scene.web3.fixtures :as fixtures]
            [scene.stest :refer [deftest-spec] :include-macros true]
            [clojure.spec.test.alpha :as stest]
            [scene.utils :refer [clj->json]]))


(t/deftest test-create-decoder
  (let [decode (sut/create-decoder fixtures/event)]
    (t/is (= (-> fixtures/log decode :args clj->json)
             (clj->json {:_from  "0x00a329c0648769a73afac7f9381e08fb43dbea72"
                         :_to    "0xef59cb8748e54ea2a7aaa0699430271000000000"
                         :_value "754"})))))


(deftest-spec spec-abi->signature `sut/abi->signature)

(t/deftest test-abi->signature
  (t/testing "if generate signature from given abi"
    (t/is (= (sut/abi->signature fixtures/event)
             (-> fixtures/log
                 :topics
                 first)))))


(deftest-spec spec-Ox `sut/Ox)

(t/deftest test-Ox
  (t/testing "adding 0x when needed"
    (t/is (= (sut/Ox "0")
             "0x0"))
    (t/is (= (sut/Ox "0xf")
             "0xf"))))
