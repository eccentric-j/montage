(ns montage.features.reorder.feature
  (:require
   [framework.fsm :as fsm]
   [framework.features :refer [register]]
   [framework.reactor :as fr]
   [framework.stream :as stream]))

(def states
  {:idle {:reorder/mousedown
          (fn [_machine action]
            {:state   :dragging
             :context {:source (:payload action) :y 0 :target nil}
             :effect  nil})}

   :dragging {:reorder/move (fn [{:keys [context]} action]
                              {:state :dragging
                               :context (merge context
                                               {:y (get-in action [:payload :y])})
                               :effect nil})
              :reorder/set-target
              (fn [{:keys [context]} action]
                {:state :dragging
                 :context (merge context
                                 {:target (:payload action)})
                 :effect nil})

              :reorder/mouseup
              (fn [{:keys [context]} _action]
                {:state :idle
                 :context (merge context
                                 {:source nil
                                  :y 0})
                 :effect  {:type :reorder/update
                           :payload {:source (:source context)
                                     :target (:target context)}}})}})

(def reorder-machine
  (fsm/create :reorder
              states
              {:state :idle
               :context {:source nil
                         :y      0
                         :target nil}
               :states states}))

(defn get-rect
  [el]
  (let [rect (.getBoundingClientRect el)]
    {:y (.-y rect)
     :top (.-top rect)}))

(defn calc-mouse-y
  [el pageY]
  (let [rect (get-rect el)]
    (+ (- pageY
        (:top rect))
       (.-offsetTop el)
       (.-scrollTop el))))

(defn start-dragging-fx
  [actions {:keys [state]}]
  (-> actions
      (fr/of-type :reorder/mousedown)
      (fr/pluck [:payload])
      (.flatMap (fn [photo]
                  (let [el (.querySelector js/document ".photos-list")]
                    (-> (stream/merge-all
                         [(-> (stream/from-event js/window "pointermove")
                              (.map (fn [e]
                                      {:type :reorder/move
                                       :payload {:y (calc-mouse-y el (.. e -pageY))
                                                 :source photo}})))
                          (-> (stream/from-event js/window "pointerup")
                              (.map (fn [_e]
                                      {:type :reorder/mouseup
                                       :payload nil})))])
                        (.takeUntil (-> actions
                                        (fr/of-type :reorder/mouseup)
                                        (.take 1)))))))))

(defn el->top
  [target-el parent-el]
  (let [rect (get-rect target-el)]
    (+ (:top rect)
       (.-scrollTop parent-el))))

(defn el->bottom
  [target-el parent-el]
  (let [top (el->top target-el parent-el)]
    (+ top (.-offsetHeight target-el))))

(defn attr
  [el attr-key]
  (.getAttribute el attr-key))

(defn select-target-fx
  [actions {:keys [_state]}]
  (-> actions
      (fr/of-type :reorder/move)
      (fr/pluck [:payload])
      (.flatMapLatest
       (fn [{:keys [y source]}]
         (let [photos (js->clj (js/Array.from (.querySelectorAll js/document ".photos-list .photo")))
               parent-el (.querySelector js/document ".photos-list")]
           (-> photos
               (stream/from-seq)
               (.filter (fn [target-el]
                          (let [photo-url (attr target-el "data-photo")]
                            (and (>= y (el->top target-el parent-el))
                                 (<= y (el->bottom target-el parent-el))
                                 (not= photo-url source)))))
               (.take 1)
               (.map (fn [el]
                       (let [photo-url (attr el "data-photo")]
                         {:type :reorder/set-target
                          :meta {:y y :source source}
                          :payload photo-url})))))))))

(register :reorder {:reducer (:reducer reorder-machine)
                    :fx (fr/compose-fx
                         [(:fx reorder-machine)
                          start-dragging-fx
                          select-target-fx])})


(defn mousedown
  [photo]
  {:type :reorder/mousedown
   :payload photo})

(defn select-machine
  [state]
  (get-in state [:reorder]))
