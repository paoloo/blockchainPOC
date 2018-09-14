(defproject bcpoc "0.5.0-HOTASHELL"
  :description "BCPOC: a proof-of-concept blockchain-based wallet API"
  :url "https://github.com/paoloo/blockchainPOC"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [com.chain/chain-sdk-java "1.2.1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-jetty-adapter "0.3.8"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler bcpoc.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}}
  :main ^{:skip-aot true} bcpoc.handler)
