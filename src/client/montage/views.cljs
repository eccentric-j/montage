(ns montage.views
  (:require
   [montage.store :as store]
   [montage.features.slideshow.views :as slideshow]
   [montage.features.playback.views :as playback]))

(defn montage
  []
  [:div
   {:class "montage bg-black m-auto relative"
    :style {:width "177.77vh"
            :height "100vh"}}
   [:audio#music-player]
   (let [photos (get-in (store/get-state) [:photos :photos])]
     (when (not-empty photos)
       [:div.frame
        [slideshow/slideshow]
        [playback/controls]]))])
