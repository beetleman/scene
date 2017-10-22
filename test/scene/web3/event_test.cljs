(ns scene.web3.event-test
  (:require [scene.web3.event :as sut]
            [cljs.test :as t :include-macros true]
            [scene.web3.fixtures :as fixtures]
            [scene.utils :refer [clj->json]]))


(t/deftest create-decoder
  (let [decode (sut/create-decoder fixtures/event)]
    (t/is (= (-> fixtures/log decode clj->json)
             (clj->json {:_from  "0x00a329c0648769a73afac7f9381e08fb43dbea72"
                         :_to    "0xef59cb8748e54ea2a7aaa0699430271000000000"
                         :_value "754"})))))



(t/deftest abi->signature
  (t/is (= (sut/abi->signature fixtures/event)
           (-> fixtures/log
               :topics
               first))))


(t/deftest Ox
  (t/is (= (sut/Ox "0")
           "0x0")))
