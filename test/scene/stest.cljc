(ns scene.stest
  (:require [clojure.spec.alpha :as s]
            [clojure.test :as t :include-macros true]
            [clojure.pprint :as pprint]
            [clojure.spec.test.alpha :as stest]))


;; Utility functions to intergrate clojure.spec.test/check with clojure.test
(defn summarize-results' [spec-check]
  (pprint/write
   (map (fn [{ret :clojure.test.check/ret
              sym :sym}]
          (assoc (select-keys ret [:seed :fail :num-tests])
                 :sym sym
                 :shrunk (dissoc (:shrunk ret) :result-data)))
        spec-check)
   :stream nil))

(defn spec-ok? [check-results]
  (let [checks-passed? (every? nil? (map :failure check-results))]
    (t/is checks-passed?
          (summarize-results' check-results))))

(defmacro deftest-spec
  ([name obj]
   `(deftest-spec ~name ~obj 25))
  ([name obj num-tests]
  `(t/deftest ~name
     (t/testing "generative test"
       (spec-ok? (stest/check ~obj
                              {:clojure.test.check/opts
                               {:num-tests ~num-tests}}))))))
