(ns scene.app
  (:require
    [doo.runner :refer-macros [doo-tests]]
    [scene.core-test]))

(doo-tests 'scene.core-test)


