(ns scene.web3-test
  (:require [scene.web3 :as sut]
            [clojure.core.async :refer [>! chan]]
            [cljs.test :as t :include-macros true])
    (:require-macros [cljs.core.async.macros :refer [go-loop go]]))


(def log {:address             "0xc2ce67affc8bf3e6bdb87be40dc104addb5f66a4"
          :transactionHash     "0x08c9bef894e1e5a4ea129043954f69b9ffd8ccf0a155ea43b861fb7bc7c69683"
          :blockHash           "0x9c512510eedf6699f87293577a74409b0209ebc6be1da3cb75d519d33949c8a1"
          :transactionLogIndex "0x0"
          :type                mined
          :transactionIndex    0
          :topics              ["0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"
                                "0x00000000000000000000000000a329c0648769a73afac7f9381e08fb43dbea72"
                                "0x000000000000000000000000ef59cb8748e54ea2a7aaa0699430271000000000"]
          :blockNumber         139530
          :logIndex            0
          :data                "0x00000000000000000000000000000000000000000000000000000000000002f2"})


(t/deftest test-last-block-number
  (let [ch (chan 1)
        last-block (sut/last-block-number ch)]
    (t/async done
             (go
               (t/is (= @last-block 0))
               (>! ch (update log :blockNumber dec))
               (>! ch log)
               (>! ch (update log :blockNumber (comp dec dec)))
               (>! ch (update log :blockNumber (comp dec dec dec)))
               (t/is (= @last-block (:blockNumber log)))
               (done)))))


(t/deftest test-create-block-ranges
  (t/is (= (sut/create-block-ranges 0 10 2)
           '({:fromBlock 0 :toBlock 2}
             {:fromBlock 3 :toBlock 5}
             {:fromBlock 6 :toBlock 8}
             {:fromBlock 9 :toBlock 9}))))
