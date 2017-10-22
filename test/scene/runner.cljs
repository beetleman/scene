(ns scene.runner
  (:require [doo.runner :refer-macros [doo-all-tests]]
            [scene.core-test]
            [scene.web3.log-test]
            [scene.utils-test]
            [scene.web3.event-test]))

(doo-all-tests #"(scene)\..*-test")
