(ns montage.features.settings.views
  (:require
   [reagent.core :as r]
   [montage.store :as store]
   [montage.features.settings.feature :as settings]))

(defn panel
  [{:keys [active class]} & children]
  (into
   [:div.panel
    {:class (r/class-names
             "fixed right-0 top-0 bottom-16 bg-black bg-opacity-60 p-4 transform transition-transform duration-500"
             class
             (if active "active translate-x-0" "translate-x-full"))}]
   children))

(defn config-panel
  [{:keys [active]}]
  (let [state (store/get-state)
        {:keys [photo-duration slide-transition]} (settings/select-config state)]
    [panel
     {:class "w-80"
      :active active}
     [:form
      {:class "flex flex-col gap-4"}
      [:div
       {:class "text-center"}
       [:label
        {:for "id-photo-duration"
         :class "block mb-2"}
        "Photo Duration"]
       [:input
        {:id "id-photo-duration"
         :class "w-full"
         :type :range
         :value photo-duration
         :on-change (fn [e]
                      (store/dispatch (settings/set-duration (js/Number (.. e -currentTarget -value)))))
         :min 1000
         :max 10000
         :step 250}]
       [:span
        {:class "block text-center my-2"}
        (str (.toFixed (/ photo-duration 1000) 2) " seconds")]]

      [:div
       {:class "text-center"}
       [:label
        {:for "id-slide-transition"
         :class "block mb-2"}
        "Transition Duration"]
       [:input
        {:id "id-slide-transition"
         :class "w-full"
         :type :range
         :value slide-transition
         :on-change (fn [e]
                      (store/dispatch (settings/set-transition (js/Number (.. e -currentTarget -value)))))
         :min 1000
         :max 4000
         :step 250}]
       [:span
        {:class "block text-center my-2"}
        (str (.toFixed (/ slide-transition 1000) 2) " seconds")]]]]))

(defn panels
  [panel-fns]
  (let [state (store/get-state)
        {:keys [state context]} (settings/select-panel state)
        {:keys [target]} context]
    (into
     [:div
      ]
     (->> panel-fns
          (map (fn [[key panel-f]]
                 [panel-f {:active (= key target)}]))))))
