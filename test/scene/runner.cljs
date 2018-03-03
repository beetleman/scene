(ns scene.runner
  (:require [doo.runner :refer-macros [doo-all-tests]]
            [scene.core-test]
            [scene.db-test]
            [scene.utils-test]
            [scene.web3.event-test]
            [scene.web3.log-test]
            [scene.ws.subscriptions-test]
            [taoensso.timbre :as timbre]))

(timbre/set-level! :fatal)
(doo-all-tests #"(scene)\..*-test")
