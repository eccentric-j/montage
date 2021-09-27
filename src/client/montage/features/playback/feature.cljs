(ns montage.features.playback.feature
  (:require
   [framework.fsm :as fsm]
   [framework.features :refer [register]]
   [framework.reactor :refer [of-type compose-fx]]
   [framework.stream :as stream]
   [montage.features.photos.feature :as photos]))

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

(def playback-machine
  (fsm/create :playback
              states
              {:state :paused
               :context {}
               :states states}))

(defn play-fx
  [actions {:keys [state]}]
  (-> actions
      (of-type :playback/start-playback)
      (.flatMapLatest
       (fn []
         (let [duration (get-in @state [:settings :photo-duration])]
           (println "duration" duration (:settings @state))
           (if duration
             (-> (stream/interval duration (photos/next))
                 (.takeUntil (-> actions
                                 (of-type :playback/pause))))
             (stream/of nil)))))))

(register :playback {:reducer (:reducer playback-machine)
                     :fx (compose-fx
                          [(:fx playback-machine)
                           play-fx])})
