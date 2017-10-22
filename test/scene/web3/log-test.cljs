(ns scene.web3.log-test
  (:require [scene.web3.log :as sut]
            [clojure.core.async :refer [>! chan]]
            [cljs.test :as t :include-macros true]
            [scene.web3.fixtures :as fixtures])
    (:require-macros [cljs.core.async.macros :refer [go-loop go]]))

(t/deftest test-last-block-number
  (let [ch (chan 1)
        last-block (sut/last-block-number ch)]
    (t/async done
             (go
               (t/is (= @last-block 0))
               (>! ch (update fixtures/log :blockNumber dec))
               (>! ch fixtures/log)
               (>! ch (update fixtures/log :blockNumber (comp dec dec)))
               (>! ch (update fixtures/log :blockNumber (comp dec dec dec)))
               (t/is (= @last-block (:blockNumber fixtures/log)))
               (done)))))

(t/deftest test-create-block-ranges
  (t/is (= (sut/create-block-ranges 0 10 2)
           '({:fromBlock 0 :toBlock 2}
             {:fromBlock 3 :toBlock 5}
             {:fromBlock 6 :toBlock 8}
             {:fromBlock 9 :toBlock 9}))))
