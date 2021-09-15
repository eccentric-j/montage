(ns montage.main
  (:require
   [montage.store :as store]))

(defn stop
  []
  (store/destroy)
  (println "Stopping montage"))

(defn start
  []
  (println "Starting montage")
  (store/create))

(defn -main
  []
  (println "Initializing montage")
  (store/create)
  (store/dispatch
   {:type :store/initialize
    :payload (js/Date.now)}))
