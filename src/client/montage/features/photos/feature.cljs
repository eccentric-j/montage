(ns montage.features.photos.feature
  (:refer-clojure :exclude [next])
  (:require
   [kitchen-async.promise :as p]
   [cljs.reader :refer [read-string]]
   [framework.fsm :as fsm]
   [framework.features :refer [register]]
   [framework.reactor :as fr]
   [montage.features.slideshow.feature :as slideshow]
   [montage.features.settings.feature :as settings]
   [framework.stream :as stream]))


(def reducer
  (fr/assign-reducers
   {:initial {:collection  []
              :last    0
              :current 0
              :next    nil
              :prev    nil}
    :reducers
    {:photos/set
     (fn [state {photos :payload}]
       (assoc state
              :collection photos
              :last (dec (count photos))))

     :photos/update-current
     (fn [state {current :payload}]
       (assoc state
              :current current
              :next    nil))


     :photos/next
     (fn [{:keys [current last] :as state} _action]
       (let [next (inc current)
             next (if (>= next last) 0 next)]
         (assoc state :prev current :next next)))

     :photos/prev
     (fn [{:keys [current last] :as state} _action]
       (let [prev (dec current)
             prev (if (< prev 0) last prev)]
         (assoc state :prev current :next prev)))}}))

(defn next
  []
  {:type    :photos/next
   :payload nil})

(defn prev
  []
  {:type    :photos/prev
   :payload nil})

(defn next-photo-fx
  [actions {:keys [state]}]
  (-> actions
      (fr/of-type :photos/next)
      (.map (fn [_]
              (let [state @state
                    {:keys [photos]} state
                    {:keys [current next]} photos
                    config (settings/select-config state)
                    {:keys [slide-transition]} config]
                {:from current
                 :to   next
                 :delay slide-transition})))
      (.map slideshow/transition)))

(defn prev-photo-fx
  [actions {:keys [state]}]
  (-> actions
      (fr/of-type :photos/prev)
      (.map (fn [_]
              (let [state @state
                    config (settings/select-config state)
                    {:keys [photos]} @state
                    {:keys [current next]} photos
                    {:keys [slide-transition]} config]
                {:from current
                 :to   next
                 :delay slide-transition})))
      (.map slideshow/transition)))

(defn load-photos-on-initialize-fx
  [actions]
  (-> actions
      (fr/of-type :store/initialize)
      (.flatMap (fn []
                  (stream/from-promise
                   (p/-> (js/fetch "/photos.edn")
                         (.text)
                         (read-string)
                         (fr/create-action :photos/set)))))))

(defn update-current-after-transition-fx
  [actions]
  (-> actions
      (fr/of-type :slideshow/transitioned)
      (fr/pluck [:payload :to])
      (fr/stream-action :photos/update-current)))

(def fx (fr/compose-fx
         [next-photo-fx
          prev-photo-fx
          load-photos-on-initialize-fx
          update-current-after-transition-fx]))

(register :photos {:reducer reducer
                   :fx      fx})

(defn select-photos
  [state]
  (get-in state [:photos :collection]))

(defn select-current
  [state]
  (get-in state [:photos :current]))
