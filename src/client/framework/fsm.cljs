(ns framework.fsm
  (:require
   [framework.stream :as stream]
   [framework.reactor :refer [init of-type]]))

;; Create a fsm atom or instance that can be referenced directly
;; Create an epic that will update an fsm and emit a transition and
;; any effect actions. That way fsms can still be created for
;; components and individual use-cases as well as fit into the
;; bigger achitecture

(defn transition
  [machine states action]
  (if-let [reducer (get-in states [(:state machine) (:type action)])]
    (reducer machine action)
    nil))

(defn create
  [name states initial]
  (let [bus (stream/Bus.)]
    {:reducer (fn [prev action]
                (if (= action init)
                  initial
                  (if-let [next (transition prev states action)]
                    (do
                      (.push bus {:type :fsm/transition
                                  :payload {:name name
                                            :prev prev
                                            :next next}})
                      (when (:effect next)
                        (.push bus (:effect next)))
                      next)
                    prev)))

     :fx (fn [actions _deps]
           (-> bus
               ; (.doAction println)
               (.takeUntil
                (-> actions
                    (of-type :store/end)
                    (.doAction #(.end bus))))))}))
