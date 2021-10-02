(ns montage.features.music.feature
  (:refer-clojure :exclude [next])
  (:require
   [kitchen-async.promise :as p]
   [cljs.reader :refer [read-string]]
   [framework.fsm :as fsm]
   [framework.features :refer [register]]
   [framework.reactor :as fr]
   [montage.features.slideshow.feature :as slideshow]
   [montage.features.settings.feature :as settings]
   [montage.features.playback.feature :as playback]
   [framework.stream :as stream]))

(def reducer
  (fr/assign-reducers
   {:initial {:collection []
              :last       0
              :current    0
              :next       nil
              :prev       nil}
    :reducers
    {:music/set
     (fn [state {music :payload}]
       (assoc state
              :collection music
              :last (dec (count music))))

     :music/next
     (fn [{:keys [current last] :as state} _action]
       (let [next (inc current)
             next (if (>= next last) 0 next)]
         (assoc state :current next)))}}))

(defn load-music-on-initialize-fx
  [actions _deps]
  (-> actions
      (fr/of-type :store/initialize)
      (.flatMap (fn []
                  (stream/from-promise
                   (p/-> (js/fetch "/music.edn")
                         (.text)
                         (read-string)
                         (fr/create-action :music/set)))))))

(defn select-el
  []
  (js/document.querySelector "#music-player"))

(defn play-music-fx
  [actions _deps]
  (-> actions
      (fr/of-type :playback/play)
      (.map select-el)
      (.doAction (fn [el]
                   (.play el)))
      (.filter (constantly false))))

(defn pause-music-fx
  [actions _deps]
  (-> actions
      (fr/of-type :playback/pause)
      (.map select-el)
      (.doAction (fn [el]
                   (.pause el)))
      (.filter (constantly false))))

(defn continue-music-fx
  [actions {:keys [state]}]
  (-> actions
      (fr/of-type :music/loaded)
      (.filter #(= (playback/select-state @state) :playing))
      (.map select-el)
      (.doAction (fn [el]
                   (.play el)))
      (.filter (constantly false))))

(register :music
          {:reducer reducer
           :fx (fr/compose-fx
                [load-music-on-initialize-fx
                 play-music-fx
                 pause-music-fx
                 continue-music-fx])})

(defn select-songs
  [state]
  (get-in state [:music :collection]))

(defn select-song
  [state]
  (get-in state [:music :current]))

(defn next
  []
  {:type :music/next
   :payload nil})

(defn loaded
  []
  {:type :music/loaded
   :payload nil})
