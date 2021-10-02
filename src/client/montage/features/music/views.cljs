(ns montage.features.music.views
  (:require
   [montage.features.music.feature :as music]
   [montage.store :as store]))

(defn music-player
  []
  (let [state (store/get-state)
        song (music/select-song state)
        songs (music/select-songs state)]
    (when (and song (not-empty songs))
      [:audio#music-player
       {:src (str "/music/" (nth songs song))
        :on-duration-change #(store/dispatch (music/loaded))
        :on-ended #(store/dispatch (music/next))}])))
