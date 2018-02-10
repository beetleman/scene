(ns scene.runner
  (:require [doo.runner :refer-macros [doo-all-tests]]
            [scene.core-test]
            [scene.web3.log-test]
            [scene.utils-test]
            [scene.db-test]
            [taoensso.timbre :as timbre]
            [scene.web3.event-test]))

(timbre/set-level! :fatal)
(doo-all-tests #"(scene)\..*-test")
