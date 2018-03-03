(ns scene.ws.subscriptions-test
  (:require [scene.ws.subscriptions :as sut]
            [scene.data :as data]
            [cljs.test :as t :include-macros true]))

(def conn-id (str (random-uuid)))
(def sub-id {:signature data/signature
             :address data/address})
(def connection-stub :connection-stub)

(t/deftest test-create-sub-id
  (t/testing "abi but without address"
    (t/is (= (#'sut/create-sub-id :abi data/event)
             {:signature data/signature
              :address   nil})))
  (t/testing "abi with with address"
    (t/is (= (#'sut/create-sub-id :abi data/event :address data/address)
             {:signature data/signature
              :address   data/address})))
  (t/testing "without abi but with address"
    (t/is (= (#'sut/create-sub-id :address data/address)
             {:signature nil
              :address   data/address}))))

(t/deftest test-subscribe
  (t/testing "subscribe* with address"
    (t/is
     (= (#'sut/subscribe* sut/empty-subscription-registry
                        conn-id
                        connection-stub
                        data/event
                        data/address)
        {:sub-id->conns    {sub-id [connection-stub]}
         :conn-id->subs-id {conn-id (set [sub-id])}})))

  (t/testing "subscribe* dont duplicate"
    (t/is (= (-> sut/empty-subscription-registry
                 (#'sut/subscribe*
                  conn-id
                  connection-stub
                  data/event
                  data/address)
                 (#'sut/subscribe*
                  conn-id
                  connection-stub
                  data/event
             data/address))
             {:sub-id->conns    {sub-id [connection-stub]}
              :conn-id->subs-id {conn-id (set [sub-id])}})))

  (t/testing "subscribe* adding same event but with different address"
    (let [address  "0x00000000000000000000000000000000000000000000000000000000000002f2"
          sub-id-2 {:signature data/signature
                    :address   address}]
      (t/is (= (-> sut/empty-subscription-registry
                   (#'sut/subscribe*
                    conn-id
                    connection-stub
                    data/event
                    data/address)
                   (#'sut/subscribe*
                    conn-id
                    connection-stub
                    data/event
                    address))
               {:sub-id->conns    {sub-id   [connection-stub]
                                   sub-id-2 [connection-stub]}
                :conn-id->subs-id {conn-id (set [sub-id sub-id-2])}})))))
