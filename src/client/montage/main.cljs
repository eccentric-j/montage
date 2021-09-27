(ns montage.main
  (:require
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [montage.store :as store]
   [montage.views :as views]
   [cljs.pprint :refer [pprint]]))

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
    :payload (js/Date.now)})
  (rdom/render [views/montage] (js/document.getElementById "app-root")))

(comment
  (get-in (store/get-state) [:photos :current])
  (store/dispatch
   {:type :playback/pause
    :payload nil})
  (start)
  (stop)
  (store/dispatch
   {:type :photos/next
    :payload nil})
  (store/dispatch
   {:type :playback/play
    :payload nil}))
