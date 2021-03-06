(ns scene.utils-test
  (:require [cljs.test :as t :include-macros true]
            [clojure.core.async :as a]
            [scene.utils :as sut])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(t/deftest callback-cljs
  (let [data #js{:number 121212}]
       (t/is (= (sut/callback->clj nil data)
                {:data data :error nil}))))


(t/deftest callback-chan-fn
  (let [ch    (a/chan)
        data  #js{"number" 1}
        error #js{"problem" "big"}
        f     (sut/callback-chan-fn ch)]
    (f nil data)
    (f error nil)
    (t/async done
             (go
               (t/is (= (a/<! ch)
                        {:data data :error nil}))
               (t/is (= (a/<! ch)
                        {:data nil :error error}))
               (done)))))


(t/deftest clj->json
  (t/is (= (sut/clj->json "json")
           "\"json\""))
  (t/is (= (sut/clj->json {:numbers [1 2 3]})
           "{\"numbers\":[1,2,3]}")))

(t/deftest json->clj
  (t/is (= (sut/json->clj "\"json\"" )
           "json"))
  (t/is (= (sut/json->clj "{\"numbers\":[1,2,3]}")
           {:numbers [1 2 3]})))
