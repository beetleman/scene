(ns scene.utils-test
  (:require [scene.utils :as sut]
            [clojure.core.async :as a]
            [cljs.test :as t :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(t/deftest callback-cljs
  (t/is (= (sut/callback->clj nil #js{"number" 121212})
           {:data {:number 121212} :error nil})))


(t/deftest callback-chan-fn
  (let [ch (a/chan)
        f  (sut/callback-chan-fn ch)]
    (f nil #js {"number" 1})
    (f #js {"problem" "big"} nil)
    (t/async done
             (go
               (t/is (= (a/<! ch)
                        {:data {:number 1} :error nil}))
               (t/is (= (a/<! ch)
                         {:data nil :error {:problem "big"}}))
               (done)))))

(t/deftest callback-chan-seq-fn
  (let [ch (a/chan)
        f  (sut/callback-chan-seq-fn ch)]
    (f #js {"problem" "big"} nil)
    (f nil #js [0 1])
    (t/async done
             (go
               (t/is (= (a/<! ch)
                        {:data nil :error {:problem "big"}}))
               (t/is (= (a/<! ch)
                        {:data 0 :error nil}))
               (t/is (= (a/<! ch)
                        {:data 1 :error nil}))
               (done)))))
