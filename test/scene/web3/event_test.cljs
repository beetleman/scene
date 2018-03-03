(ns scene.web3.event-test
  (:require [cljs.test :as t :include-macros true]
            [clojure.spec.test.alpha :as stest]
            [goog.object :as gobj]
            [scene.data :as data]
            [scene.stest :refer [deftest-spec] :include-macros true]
            [scene.utils :refer [clj->json js->json]]
            [scene.web3.event :as sut]))


(t/deftest test-create-decoder
  (let [decode (sut/create-decoder data/event)]
    (t/is (= (-> data/log decode (gobj/get "args") js->json)
             (clj->json {:_from  "0x00a329c0648769a73afac7f9381e08fb43dbea72"
                         :_to    "0xef59cb8748e54ea2a7aaa0699430271000000000"
                         :_value "754"})))))


(deftest-spec spec-abi->signature `sut/abi->signature)

(t/deftest test-abi->signature
  (t/testing "if generate signature from given abi"
    (t/is (= (sut/abi->signature data/event)
             data/signature))))


(deftest-spec spec-Ox `sut/Ox)

(t/deftest test-Ox
  (t/testing "adding 0x when needed"
    (t/is (= (sut/Ox "0")
             "0x0"))
    (t/is (= (sut/Ox "0xf")
             "0xf"))))
