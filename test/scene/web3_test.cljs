(ns scene.web3-test
  (:require [scene.web3 :as sut]
            [cljs.test :as t :include-macros true]))


(t/deftest test-false-true
  (t/is (= true false)))
