(ns scene.runner
  (:require [doo.runner :refer-macros [doo-all-tests]]
            [scene.core-test]
            [scene.web3-test]))

(doo-all-tests #"(scene)\..*-test")
