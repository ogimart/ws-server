(defproject ws-server "0.1.0-alpha"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [cheshire "5.8.1"]
                 [clj-time "0.15.0"] 
                 [http-kit "2.3.0"]]
  :repl-options {:init-ns ws-server.core}
  :main ws-server.core
  :profiles {:uberjar {:aot :all}})
