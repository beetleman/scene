(ns scene.db-test
  (:require [scene.db :as sut]
            [scene.web3.fixtures :as fixtures]
            [cljs.test :as t :include-macros true]))

;; checking only load here

(t/deftest log-address-key
  (t/is (= (sut/log->address-key fixtures/log)
           "scene:topic:0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef:address:0xc2ce67affc8bf3e6bdb87be40dc104addb5f66a4")))

(t/deftest log-topic-key
  (t/is (= (sut/log->topic-key fixtures/log)
           "scene:topic:0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef")))
