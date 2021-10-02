(ns montage.features.slideshow.views
  (:require
   [clojure.string :as s]
   [reagent.core :as r]
   [montage.store :as store]
   [montage.features.photos.feature :as photos]
   [montage.features.playback.feature :as playback]))

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
        photos (photos/select-photos (store/get-state))
        current-photo (nth photos target)]
    (if (s/includes? current-photo "mov")
      [:video
       {:key target
        :class (r/class-names
                "absolute left-0 top-0 right-0 bottom-0 bg-contain m-auto bg-center bg-no-repeat transition-opacity ease-linear duration-500"
                (get-classname state))
        :src (str "/img/" current-photo)
        :autoPlay "autoplay"
        :on-play #(store/dispatch (playback/pause-photos))
        :on-ended #(do (store/dispatch (photos/next))
                       (store/dispatch (playback/resume-photos)))}]
      [:div
       {:key target
        :class (r/class-names
                "absolute left-0 top-0 right-0 bottom-0 bg-contain bg-center bg-no-repeat transition-opacity ease-linear duration-500"
                (get-classname state)
                (when (= playback-state :playing) "zoom-in"))
        :style {:background-image (str "url(/img/" current-photo ")")}}])))
