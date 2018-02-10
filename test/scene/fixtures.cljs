(ns scene.fixtures
  (:require [cljs.test :as t :include-macros true]
            [mount.core :as mount]
            [promesa.core :as p]
            [scene.db :as db]
            [macchiato.util.response :as r]))


(def withDb
  {:before
   (fn []
     (t/async done
              (do (mount/start #'db/conn
                               #'db/db
                               #'db/logs-collection)
                  (p/then [p/all [@db/db @db/logs-collection]]
                          done))))
   :after
   (fn[]
     (t/async done
              (p/chain @db/db
                       #(.dropDatabase %)
                       (fn [_]
                         (mount/stop #'db/conn
                                     #'db/db
                                     #'db/logs-collection)
                         log
                         (done)))))})
