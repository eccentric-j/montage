(ns montage.features.playback.views
  (:refer-clojure :exclude [next])
  (:require
   [reagent.core :as r]
   [montage.store :as store]
   [montage.features.photos.feature :as photos]
   [montage.features.playback.feature :as playback]
   [montage.features.settings.feature :as settings]))

(defn prev
  [{:keys [class on-click]}]
  [:button
   {:on-click on-click}
   [:svg
    {:class class
     :xmlns "http://www.w3.org/2000/svg"
     :fill "currentColor", :viewBox "0 0 20 20"
     :on-click on-click}
    [:path
     {:d
      "M8.445 14.832A1 1 0 0010 14v-2.798l5.445 3.63A1 1 0 0017 14V6a1 1 0 00-1.555-.832L10 8.798V6a1 1 0 00-1.555-.832l-6 4a1 1 0 000 1.664l6 4z"}]]])

(defn next
  [{:keys [class on-click]}]
  [:button
   {:on-click on-click}
   [:svg
    {:class class
     :xmlns "http://www.w3.org/2000/svg"
     :fill "currentColor", :viewBox "0 0 20 20"
     :on-click on-click}
    [:path
     {:d
      "M4.555 5.168A1 1 0 003 6v8a1 1 0 001.555.832L10 11.202V14a1 1 0 001.555.832l6-4a1 1 0 000-1.664l-6-4A1 1 0 0010 6v2.798l-5.445-3.63z"}]]])

(defn play
  [{:keys [class on-click]}]
  [:button
   {:on-click on-click}
   [:svg
    {:class class
     :xmlns "http://www.w3.org/2000/svg"
     :fill "currentColor"
     :viewBox "0 0 20 20"
     :on-click on-click}
    [:path
     {:clip-rule "evenodd",
      :d
      "M10 18a8 8 0 100-16 8 8 0 000 16zM9.555 7.168A1 1 0 008 8v4a1 1 0 001.555.832l3-2a1 1 0 000-1.664l-3-2z",
      :fill-rule "evenodd"}]]])


(defn pause
  [{:keys [class on-click]}]
  [:button
   {:on-click on-click}
   [:svg
    {:class class
     :xmlns "http://www.w3.org/2000/svg"
     :fill "currentColor", :viewBox "0 0 20 20"}
    [:path
     {:clip-rule "evenodd",
      :d
      "M18 10a8 8 0 11-16 0 8 8 0 0116 0zM7 8a1 1 0 012 0v4a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v4a1 1 0 102 0V8a1 1 0 00-1-1z",
      :fill-rule "evenodd"}]]])

(defn settings
  [{:keys [class on-click]}]
  [:button
   {:on-click on-click}
   [:svg
    {:class class
     :xmlns "http://www.w3.org/2000/svg"
     :fill "currentColor", :viewBox "0 0 20 20"}
    [:path
     {:clip-rule "evenodd",
      :d
      "M11.49 3.17c-.38-1.56-2.6-1.56-2.98 0a1.532 1.532 0 01-2.286.948c-1.372-.836-2.942.734-2.106 2.106.54.886.061 2.042-.947 2.287-1.561.379-1.561 2.6 0 2.978a1.532 1.532 0 01.947 2.287c-.836 1.372.734 2.942 2.106 2.106a1.532 1.532 0 012.287.947c.379 1.561 2.6 1.561 2.978 0a1.533 1.533 0 012.287-.947c1.372.836 2.942-.734 2.106-2.106a1.533 1.533 0 01.947-2.287c1.561-.379 1.561-2.6 0-2.978a1.532 1.532 0 01-.947-2.287c.836-1.372-.734-2.942-2.106-2.106a1.532 1.532 0 01-2.287-.947zM10 13a3 3 0 100-6 3 3 0 000 6z",
      :fill-rule "evenodd"}]]])

(defn controls
  []
  (let [state (store/get-state)
        playback-state (get-in state [:playback :state])
        target (get-in state [:slideshow :context :target])
        {:keys [photos current]} (get-in state [:photos])]
    [:div.controls
     {:class "flex flex-row justify-between items-center bg-black opacity-60 fixed bottom-0 left-0 right-0 p-4 transform translate-y-full transition-transform duration-300"}
     [:div.playback-controls
      {:class "flex flex-row justify-between items-center space-x-4"}
      [prev
       {:class "w-8 h-8"
        :on-click #(store/dispatch (photos/prev))}]
      (if (= playback-state :paused)
        [play
         {:class "w-8 h-8"
          :on-click #(store/dispatch (playback/play))}]
        [pause
         {:class "w-8 h-8 "
          :on-click #(store/dispatch (playback/pause))}])
      [next
       {:class "w-8 h-8"
        :on-click #(store/dispatch (photos/next))}]]

     [:div.playback-info
      {:class "flex flex-row justify-between items-center space-x-4"}
      (when (not-empty photos)
        [:<>
         [:span (nth photos target)]
         [:span (str (inc current) "/" (count photos))]
         [settings
          {:class "w-8 h-8"
           :on-click #(store/dispatch (settings/open :config))}]])]]))
