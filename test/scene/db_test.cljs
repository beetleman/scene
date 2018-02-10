(ns scene.db-test
  (:require [cljs.test :as t :include-macros true]
            [clojure.core.async :refer [<!]]
            [scene.db :as sut]
            [scene.data :as data]
            [scene.fixtures :refer [withDb]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(t/use-fixtures :each withDb)

(t/deftest test_save-log_and_get-latest-logs
  (t/async done
           (go
             (<! (sut/save-logs #js [data/log-js]))
             (t/is (= data/log
                      (-> (sut/get-latest-log)
                          <!
                          (js->clj :keywordize-keys true)
                          (dissoc :_id :signature))))
             (done))))
