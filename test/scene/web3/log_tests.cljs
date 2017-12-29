(ns scene.web3.log-test
  (:require [scene.web3.log :as sut]
            [clojure.core.async :refer [>! <! chan alts! timeout]]
            [cljs.test :as t :include-macros true]
            [scene.web3.fixtures :as fixtures])
  (:require-macros [cljs.core.async.macros :refer [go-loop go]]))


(t/deftest test-last-block-number
  (let [ch (chan 1)
        log {:data fixtures/log}
        last-block-ch (sut/last-block-number ch)]
    (t/async done
             (go
               (let [[last-block _] (alts! [last-block-ch (timeout 10)])]
                 (t/is (nil? last-block)))
               (>! ch log)
               (t/is (= (<! last-block-ch) (get-in log [:data :blockNumber])))
               (done)))))

(t/deftest test-create-block-ranges
  (t/is (= (sut/create-block-ranges 0 10 2)
           '({:fromBlock 0 :toBlock 1}
             {:fromBlock 2 :toBlock 3}
             {:fromBlock 4 :toBlock 5}
             {:fromBlock 6 :toBlock 7}
             {:fromBlock 8 :toBlock 9}
             {:fromBlock 10 :toBlock 10}))))
