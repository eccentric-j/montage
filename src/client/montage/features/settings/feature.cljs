(ns montage.features.settings.feature
  (:require
   [framework.fsm :as fsm]
   [framework.features :refer [register]]
   [framework.reactor :as fr]))

(def reducers
  {:settings/set-transition
   (fn [state action]
     (assoc state :slide-transition (:payload action)))

   :settings/set-duration
   (fn [state action]
     (assoc state :photo-duration (:payload action)))})

(def reducer (fr/assign-reducers {:initial {:slide-transition 1000
                                            :photo-duration   7000}
                                  :reducers reducers}))

(def states
  {:closed {:settings/toggle
            (fn [_machine action]
              {:state   :open
               :context {:target (:payload action)}
               :effect  {:type :settings/opened
                         :payload (:payload action)}})}

   :open {:settings/toggle
          (fn [{:keys [context]} action]
            (println "settings/toggle" {:context context :action (:payload action)})
            (if (= (:target context) (:payload action))
              {:state :closed
               :context {:target nil}
               :effect {:type :settings/closed
                        :payload (:payload action)}}
              {:state :closed
               :context {:target nil}
               :effect {:type :slideshow/wait
                        :payload {:delay 500
                                  :action {:type :settings/open
                                           :payload (:payload action)}}}}))}})

(def panel-machine
  (fsm/create :settings/panel
              states
              {:state :closed
               :context {:target nil}
               :states states}))

(defn select-config
  [state]
  (get-in state [:settings :config]))

(defn select-panel
  [state]
  (get-in state [:settings :panel]))

(register :settings {:reducer (fr/combine-reducers {:config reducer
                                                    :panel  (:reducer panel-machine)})
                     :fx (fr/compose-fx [(:fx panel-machine)])})

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

(defn toggle
  [panel]
  {:type :settings/toggle
   :payload panel})
