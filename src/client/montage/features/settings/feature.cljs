(ns montage.features.settings.feature
  (:require
   [framework.features :refer [register]]
   [framework.reactor :refer [assign-reducers]]))

(def reducers
  {:settings/set-transition
   (fn [state action]
     (assoc state :slide-transition (:payload action)))

   :settings/set-duration
   (fn [state action]
     (assoc state :duration (:payload action)))})

(def reducer (assign-reducers {:initial {:slide-transition 1000
                                         :photo-duration   7000}
                               :reducers reducers}))

(defn set-transition
  "
  Update transition for the length of time animating a fade out of the
  current photo and a fade in of the next photo
  "
  [transition-ms]
  {:type    :settings/set-transition
   :payload transition-ms})

(defn set-duration
  "
  Update duration for the length of time a photo is presented before
  transitioning to the next
  "
  [duration-ms]
  {:type :settings/set-duration
   :payload duration-ms})

(register :settings {:reducer reducer})
