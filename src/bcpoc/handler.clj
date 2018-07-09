(ns bcpoc.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [bcpoc.setup :as bcsetup]
            [bcpoc.chain :as bchain])
  (:use  [ring.adapter.jetty]))

(def masterdetails (atom {}))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (route/not-found "Not Found"))

(def app
    (do
    (println "Initializing blockchain...")
    (let [envvars (System/getenv)
         sbc (bcsetup/setup-blockchain
               (get envvars "BCURI" "http://localhost:1999")
               (get envvars "BCKEY" "")
               (get envvars "BCBANK" "PAOLOBANK")
               (get envvars "BCCOIN" "PAOLOCOIN"))
         obc (bchain/init-blockchain
               (get envvars "BCURI" "http://localhost:1999")
               (get envvars "BCKEY" "")
               (:i-id sbc)
               (:i-key sbc)
               (:i-asset sbc))]
         (println obc))
    (println "Starting API...")
    (->
      (wrap-defaults app-routes site-defaults))))

(defn -main []
  (let [port (Integer/parseInt (get (System/getenv) "PORT" "5000"))]
    (run-jetty app {:port port})))
