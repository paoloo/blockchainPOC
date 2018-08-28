(ns bcpoc.chain
  (import (com.chain.signing HsmSigner)
          (com.chain.http Client)
          (com.chain.api MockHsm
                         MockHsm$Key
                         Asset
                         Asset$Builder
                         Account
                         Account$Builder
                         Transaction
                         Transaction$Builder
                         Transaction$Action$Issue
                         Balance
                         Balance$QueryBuilder
                         Transaction$QueryBuilder
                         Transaction$Action$ControlWithAccount
                         Transaction$Action$SpendFromAccount)))

(def config (atom {:chain ""
                   :issuer-id ""
                   :issuer-key ""
                   :asset-alias ""}))

(defn init-blockchain
  [chain-url access-token issuer-id issuer-key asset-alias]
  (swap! config assoc :chain (Client. chain-url access-token)
         :issuer-id issuer-id
         :issuer-key issuer-key
         :asset-alias asset-alias)
  (HsmSigner/addKey (:issuer-key @config) (MockHsm/getSignerClient (:chain @config)))
  "blockchain started!")

(defn create-wallet!
  [account-alias]
  (let [wallet-key (MockHsm$Key/create (:chain @config) account-alias)
        wallet-acc (->
                     (Account$Builder.)
                     (.setAlias account-alias)
                     (.addRootXpub (.xpub wallet-key))
                     (.setQuorum 1)
                     (.create (:chain @config)))]
    {:id (.id wallet-acc) :xpub (.xpub wallet-key)}))

(defn issue-asset!
  ([amount account-id asset-alias]
  (let [unsigned-transaction
        (-> (Transaction$Builder.)
            (.addAction (-> (Transaction$Action$Issue.)
                            (.setAssetAlias asset-alias)
                            (.setAmount amount)))
            (.addAction (-> (Transaction$Action$ControlWithAccount.)
                            (.setAccountId account-id)
                            (.setAssetAlias asset-alias)
                            (.setAmount amount)))
            (.build (:chain @config)))
        signed-transaction (HsmSigner/sign unsigned-transaction)]
    (Transaction/submit (:chain @config) signed-transaction)))
  ([amount account-id]
   (issue-asset! amount account-id (:asset-alias @config))))

(defn balance
  ([account-id asset-alias]
  (let [balances (-> (Balance$QueryBuilder.)
                     (.setFilter "account_id=$1 AND asset_alias=$2")
                     (.addFilterParameter account-id)
                     (.addFilterParameter asset-alias)
                     (.execute (:chain @config)))
        pbalance (atom 0)]
    (while (.hasNext balances)
      (swap! pbalance #(+ %1 (.amount (.next balances))))) @pbalance))
  ([account-id]
   (balance account-id (:asset-alias @config))))

(defn transfer!
  ([origin-acc-id origin-acc-xpub destination-acc-id amount asset-alias]
  (if (>= (balance origin-acc-id asset-alias) amount)
    (let [unsigned-transaction
          (-> (Transaction$Builder.)
              (.addAction (-> (Transaction$Action$SpendFromAccount.)
                              (.setAccountId origin-acc-id)
                              (.setAssetAlias asset-alias)
                              (.setAmount amount)))
              (.addAction (-> (Transaction$Action$ControlWithAccount.)
                              (.setAccountId destination-acc-id)
                              (.setAssetAlias asset-alias)
                              (.setAmount amount)))
              (.build (:chain @config)))
          _ (HsmSigner/addKey origin-acc-xpub (MockHsm/getSignerClient (:chain @config)))
          signed-transaction (HsmSigner/sign unsigned-transaction)]
      (Transaction/submit (:chain @config) signed-transaction)
      {:status 0 :description "Transaction successful."})
    {:status -1 :description "Not enough funds."}))
  ([origin-acc-id origin-acc-xpub destination-acc-id amount]
    (transfer! origin-acc-id origin-acc-xpub destination-acc-id amount (:asset-alias @config))))

(defn list-transactions
  [account-id]
  (let [transactions (-> (Transaction$QueryBuilder.)
                         (.setFilter "inputs(account_id=$1) OR outputs(account_id=$1)")
                         (.addFilterParameter account-id)
                         (.execute (:chain @config)))
        tdata (atom [])]
    (while (.hasNext transactions)
      (let [current (.next transactions)
            c-outputs (into [] (.outputs current))
            placeholder (atom {})]
            (reduce + (map
                 (fn [k] (do
                   (if (= (.purpose k) "receive")
                     (swap! placeholder assoc
                      :amount (.amount k)
                      :asset (.assetAlias k)
                      :to (.accountAlias k)
                      :type (if (= (.accountId k) account-id) "credit" "debit")
                   )) 0))
                 c-outputs))
          (swap! tdata conj @placeholder)
      )) @tdata))
