(ns montage.features.reorder.feature
  (:require
   [framework.fsm :as fsm]
   [framework.features :refer [register]]
   [framework.reactor :refer [of-type compose-fx]]
   [framework.stream :as stream]))

(def states
  {:paused {:playback/play (fn [_machine _action]
                             {:state   :playing
                              :context {}
                              :effect  {:type :playback/start-playback
                                        :payload nil}})}

   :playing {:playback/pause (fn [_machine _action]
                               {:state :paused
                                :context {}
                                :effect {:type :playback/pause-playback
                                         :payload nil}})}})

(def reorder-machine
  (fsm/create :reorder
              states
              {:state :closed
               :context {}
               :states states}))
