(ns montage.features.slideshow
  (:require
   [framework.fsm :as fsm]
   [framework.features :refer [register]]
   [framework.reactor :refer [of-type compose-fx]]
   [frmaework.stream :as stream]))

(defn transition-delay
  [machine]
  (/ (get-in machine [:context :transition-delay]) 2))

(def states
  {:idle
   {:slideshow/transition
    (fn [machine action]
      {:state   :prepare
       :context {:transition-delay (get-in action [:payload :transition-delay])
                 :from (get-in action [:payload :from])
                 :to (get-in action [:payload :to])}
       :effect  {:type :slideshow/wait
                 :payload {:delay 0
                           :action {:type :slideshow/begin
                                    :payload nil}}}})}

   :prepare
   {:slideshow/begin
    (fn [machine _action]
      {:state   :transition-out
       :context (:context machine)
       :effect {:type :slideshow/wait
                :payload {:delay (transition-delay machine)
                          :action {:type :slideshow/transition-out-complete
                                   :payload nil}}}})}

   :transition-out
   {:slideshow/transitioned-out-complete
    (fn [machine _action]
      {:state :transition-in
       :context (:context machine)
       :effect {:type :slideshow/wait
                :payload {:delay (transition-delay machine)
                          :action {:type :slideshow/transition-in-complete
                                   :payload nil}}}})}

   :transition-in
   {:slideshow/transition-in-complete
    (fn [machine _action]
      {:state   :idle
       :context (merge (:context machine)
                       {:from nil
                        :to   nil})
       :effect  nil})}})

(def slideshow
  (fsm/create :create/initial
              states
              {:state   :idle
               :context {}
               :effect  nil}))

(def reducer (:reducer slideshow))

(defn wait-fx
  [actions _deps]
  (-> actions
      (of-type :slideshow/wait)
      (.flatMap (fn [action]
                  (stream/later (get-in action [:payload :delay])
                                (get-in action [:payload :action]))))))

(def fx (compose-fx
         (:fx slideshow)
         wait-fx))

(register :slideshow {:reducer reducer
                      :fx      fx})
