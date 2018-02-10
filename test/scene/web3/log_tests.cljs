(ns scene.web3.log-test
  (:require [scene.web3.log :as sut]
            [clojure.core.async :refer [>! <! chan alts! timeout]]
            [cljs.test :as t :include-macros true]
            [clojure.spec.test.alpha :as stest]
            [scene.stest :refer [deftest-spec] :include-macros true]
            [clojure.spec.alpha :as s])
  (:require-macros [cljs.core.async.macros :refer [go-loop go]]))


(deftest-spec spec-create-block-range `sut/create-block-range)

(t/deftest test-create-block-ranges
  (t/testing "produce right data for given args"
    (t/is (= (sut/create-block-ranges 0 10 2)
             '({:fromBlock 0 :toBlock 1}
               {:fromBlock 2 :toBlock 3}
               {:fromBlock 4 :toBlock 5}
               {:fromBlock 6 :toBlock 7}
               {:fromBlock 8 :toBlock 9}
               {:fromBlock 10 :toBlock 10})))))
