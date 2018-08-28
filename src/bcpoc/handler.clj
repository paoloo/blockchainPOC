(ns bcpoc.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :as middleware]
            [bcpoc.setup :as bcsetup]
            [bcpoc.chain :as bchain])
  (:use  [ring.adapter.jetty]))

(def masterdetails (atom {}))

(defn output-json
  [status-code information]
  {:status status-code
   :headers {"Content-Type" "application/json"}
   :body information })

(defn json-ok
  [information]
  (output-json 200 information))

(defn create-wallet
  [x]
  (if (get-in x [:body :username])
    (json-ok {:message (bchain/create-wallet! (get-in x [:body :username]))})
    (output-json 400 {:error "A parameter is missing on the request." })))

(defn wallet-balance
  [x]
  (if (get-in x [:body :wallet])
    (json-ok {:message (bchain/balance (get-in x [:body :wallet]))})
    (output-json 400 {:error "A parameter is missing on the request." })))

(defn issue->wallet
  [x]
  (if (and (get-in x [:body :wallet]) (get-in x [:body :amount]) )
    (json-ok {:message (str (bchain/issue-asset! (get-in x [:body :amount]) (get-in x [:body :wallet])))})
    (output-json 400 {:error "A parameter is missing on the request." })))

(defn wallet-transfer
  [x]
  (if (and (get-in x [:body :origin]) (get-in x [:body :originxpub]) (get-in x [:body :destination]) (get-in x [:body :amount]))
    (json-ok {:message (bchain/transfer! (get-in x [:body :origin]) (get-in x [:body :originxpub]) (get-in x [:body :destination]) (Integer/parseInt (get-in x [:body :amount])) )})
    (output-json 400 {:error "A parameter is missing on the request." })))

(defn wallet-transactions
  [x]
  (if (get-in x [:body :wallet])
    (json-ok {:message (bchain/list-transactions (get-in x [:body :wallet]))})
    (output-json 400 {:error "A parameter is missing on the request." })))


(defroutes app-routes
  (GET "/"          []  "hello, i am working perfectly! Have a great day.")
  (POST "/balance"  req (wallet-balance req))
  (POST "/account"  req (create-wallet req))
  (POST "/issue"    req (issue->wallet req))
  (POST "/transfer" req (wallet-transfer req))
  (POST "/list"     req (wallet-transactions req))
  (route/not-found "Not Found"))

(def app
    (do
    (println "Initializing blockchain...")
    (let [envvars (System/getenv)
          datafile (clojure.string/trim (clojure.string/trim-newline (slurp "/chain/client-token")))
          sbc (bcsetup/setup-blockchain
               (get envvars "BCURI" "http://localhost:1999")
               (get envvars "BCKEY" datafile)
               (get envvars "BCBANK" "PAOLOBANK")
               (get envvars "BCCOIN" "PAOLOCOIN"))
          obc (bchain/init-blockchain
               (get envvars "BCURI" "http://localhost:1999")
               (get envvars "BCKEY" datafile)
               (:i-id sbc)
               (:i-key sbc)
               (:i-asset sbc))]
         (reset! masterdetails obc))
    (println "Starting API...")
    (->
      (wrap-defaults app-routes (assoc-in site-defaults [:security :anti-forgery] false))
      (middleware/wrap-json-body {:keywords? true})
      middleware/wrap-json-response)))

(defn -main []
  (let [port (Integer/parseInt (get (System/getenv) "PORT" "5000"))]
    (run-jetty app {:port port})))
