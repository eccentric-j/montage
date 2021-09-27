(ns montage.features.slideshow.feature
  (:require
   [framework.fsm :as fsm]
   [framework.features :refer [register]]
   [framework.reactor :refer [of-type compose-fx]]
   [framework.stream :as stream]))

(defn slide-transition
  [machine]
  (/ (get-in machine [:context :slide-transition]) 2))

(defn transition
  [{:keys [from to delay]}]
  {:type :slideshow/transition
   :payload {:from from
             :to   to
             :slide-transition delay }})

(def states
  {:idle
   {:slideshow/transition
    (fn [_machine action]
      {:state   :prepare
       :context {:slide-transition (get-in action [:payload :slide-transition])
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
                :payload {:delay (slide-transition machine)
                          :action {:type :slideshow/transition-out-complete
                                   :payload nil}}}})}

   :transition-out
   {:slideshow/transition-out-complete
    (fn [machine _action]
      {:state :transition-in
       :context (:context machine)
       :effect {:type :slideshow/wait
                :payload {:delay (slide-transition machine)
                          :action {:type :slideshow/transition-in-complete
                                   :payload nil}}}})}

   :transition-in
   {:slideshow/transition-in-complete
    (fn [machine _action]
      {:state   :idle
       :context (merge (:context machine)
                       {:from nil
                        :to   nil})
       :effect  {:type :slideshow/transitioned
                 :payload {:from (get-in machine [:context :from])
                           :to   (get-in machine [:context :to])}}})}})

(def slideshow
  (fsm/create :slideshow
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

(def fx (compose-fx [(:fx slideshow)
                     wait-fx]))

(register :slideshow {:reducer reducer
                      :fx      fx})
