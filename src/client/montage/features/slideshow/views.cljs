(ns montage.features.slideshow.views
  (:require
   [montage.store :as store]))

(defn slideshow
  []
  (let [slideshow-machine (get-in (store/get-state) [:slideshow])
        {:keys [current next photos]} (get-in (store/get-state) [:photos])
        current-photo (nth photos current)]
    [:div
     [:div
      {:class "absolute left-0 top-0 right-0 bottom-0 bg-contain bg-center bg-no-repeat"
       :style {:background-image (str "url(/img/" current-photo ")")}}]]))
