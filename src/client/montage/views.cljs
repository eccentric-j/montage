(ns montage.views
  (:require
   [montage.store :as store]
   [montage.features.slideshow.views :as slideshow]))

(defn montage
  []
  [:div
   {:class "montage bg-black m-auto relative"
    :style {:width "100vw"
            :height "56.25vw"}}
   (let [photos (get-in (store/get-state) [:photos :photos])]
     (println "INCOMING PHOTOS" photos)
     (when (not-empty photos)
       [slideshow/slideshow]))])
