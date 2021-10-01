(ns montage.features.slideshow.views
  (:require
   [reagent.core :as r]
   [montage.store :as store]))

(defn get-classname
  [state]
  (case state
    :prepare-out    "opacity-100"
    :transition-out "opacity-0"
    :prepare-in     "opacity-0"
    :transition-in  "opacity-100"
    ""))

(defn slideshow
  []
  (let [{:keys [state context]} (get-in (store/get-state) [:slideshow])
        playback-state (get-in (store/get-state) [:playback :state])
        {:keys [target]} context
        {:keys [photos]} (get-in (store/get-state) [:photos])
        current-photo (nth photos target)]
    [:div
     {:key target
      :class (r/class-names
              "absolute left-0 top-0 right-0 bottom-0 bg-contain bg-center bg-no-repeat transition-opacity ease-linear duration-500"
              (get-classname state)
              (when (= playback-state :playing) "zoom-in"))
      :style {:background-image (str "url(/img/" current-photo ")")}}]))
