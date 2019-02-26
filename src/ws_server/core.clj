(ns ws-server.core
  (:gen-class)
  (:require [cheshire.core :as json]
            [clj-time.core :as t]
            [org.httpkit.server :refer :all]
            [org.httpkit.timer :as timer]))

(def sleep 1000)
(defonce server (atom nil))
(defonce channels (atom #{}))

(defn connect! [channel]
  (println "channel open")
  (swap! channels conj channel))

(defn disconnect! [channel status]
  (println "channel closed:" status)
  (swap! channels #(remove #{channel} %)))

(defn disconnect-all! []
  (doseq [ch @channels]
    (disconnect! ch "shutdown")))

(defn send-all [msg]
  (doseq [ch @channels]
    (send! ch msg)))

(defn echo [ch msg]
  (send! ch msg))

(defn stream-data []
  (loop [id 0]
    (let [msg {:id id :time (str (t/now))}] 
      (println msg)
      (send-all (json/generate-string msg))
      (Thread/sleep sleep)
      (recur (inc id)))))

(defn handler [req]
  (with-channel req ch
    (connect! ch)
    (on-close ch (partial disconnect! ch))
    (on-receive ch #(send! ch (str "server received: " %)))))

(defn stop-server []
  (println "stopping server")
  (when-not (nil? @server)
    (disconnect-all!)
    (@server :timeout 100)
    (reset! server nil)))

(defn start-server [port]
  (println "starting server on port" port)
  (reset! server (run-server handler {:port port}))
  (stream-data))

(defn shutdown-hook []
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-server)))

(defn -main [& args]
  (shutdown-hook)
  (if (nil? args)
    (start-server 9090)
    (start-server (Integer/parseInt (first args)))))

(comment
  (start-server 9090)
  (stop-server)
  )
