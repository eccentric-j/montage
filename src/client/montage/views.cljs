(ns montage.views
  (:require
   [montage.store :as store]
   [montage.features.photos.feature :as photos]
   [montage.features.settings.views :as settings]
   [montage.features.slideshow.views :as slideshow]
   [montage.features.playback.views :as playback]
   [montage.features.reorder.views :as reorder]
   [montage.features.music.views :as music]))

(defn montage
  []
  [:div
   {:class "montage bg-black m-auto relative overflow-hidden"
    :style {:width "100vw"
            :height "56.25vw"}}
   [music/music-player]
   (let [photos (photos/select-photos (store/get-state))]
     (when (not-empty photos)
       [:div.frame
        [slideshow/slideshow]
        [playback/controls]
        [settings/panels
         {:config settings/config-panel
          :reorder reorder/reorder-panel}]]))])
