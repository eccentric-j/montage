(ns montage.store
  (:require
   [reagent.core :as r]
   [framework.stream :refer [log Bus]]
   [framework.reactor :refer [init reduce-state of-type compose-fx combine-reducers]]
   [framework.features :refer [features]]))

(defn log-actions-fx
  [actions _deps]
  (-> actions
      (log)
      (.filter false)))

(def store-reducer
  (combine-reducers
   (->> @features
        (map (fn [[name feature]]
               [name (:reducer feature)]))
        (filter second)
        (into {}))))

(def store-fx
  (compose-fx
   (->> @features
        (keep (fn [[_name feature]]
                (:fx feature)))
        (into [log-actions-fx]))))

(defonce state (r/atom (store-reducer {} init)))
(defonce actions (Bus.))
(defonce unsubscribe (atom (constantly nil)))

(defn dispatch
  [action]
  (reduce-state state store-reducer action)
  (.push actions action))

(defn subscribe
  []
  (-> (store-fx actions {:state state})
      (.takeUntil (-> actions
                      (of-type :store/end)))
      (.onValue dispatch)))

(defn create
  []
  (reset! unsubscribe (subscribe)))

(defn destroy
  []
  (.push actions
         {:type :store/end
          :payload (js/Date.now)})
  (@unsubscribe))

(defn get-state
  ([]
   @state)
  ([keys]
   (get-in @state keys))
  ([keys default]
   (get-in @state keys default)))

(set! (.-store js/window)
      #js {:getState (fn []
                       (clj->js (get-state)))})
