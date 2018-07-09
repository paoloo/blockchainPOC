(ns bcpoc.setup
  (:import (com.chain.signing HsmSigner)
          (com.chain.http Client)
          (com.chain.api MockHsm
                         MockHsm$Key
                         Asset
                         Asset$Builder
                         Account
                         Account$Builder)
          (java.sql Timestamp)))

(defn setup-blockchain
  [chain-url access-token issuer-alias asset-alias]
  (def chain (Client. chain-url access-token))
  (def base-key (MockHsm$Key/create chain (str issuer-alias " KEY")))
  (HsmSigner/addKey base-key (MockHsm/getSignerClient chain))
  (->
    (Asset$Builder.)
    (.setAlias asset-alias)
    (.addRootXpub (.xpub base-key))
    (.setQuorum 1)
    (.create chain))
  (def acc  (->
    (Account$Builder.)
    (.setAlias issuer-alias)
    (.addRootXpub (.xpub base-key))
    (.setQuorum 1)
    (.create chain)))
  {:i-key (.xpub base-key) :i-alias issuer-alias :i-id (.id acc) :i-asset asset-alias})


