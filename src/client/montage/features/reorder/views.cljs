(ns montage.features.reorder.views
  (:require
   [reagent.core :as r]
   [montage.store :as store]
   [montage.features.photos.feature :as photos]
   [montage.features.reorder.feature :as reorder]
   [montage.features.settings.views :as settings]))

(defn photo-item
  [{:keys [photo source target class-name]}]
  [:div
   (when (and target source (= target photo))
     [photo-item
      {:photo source
       :source nil
       :target nil
       :class-name "brightness-110 mb-4"}])
   [:div.photo
    {:key photo
     :class (r/class-names
             class-name
             (when (= photo source) "opacity-40"))
     :data-photo photo
     :on-mouse-down #(do
                       (.preventDefault %)
                       (store/dispatch (reorder/mousedown photo)))}
    [:div
     {:class "w-60 h-40 bg-cover bg-center m-auto"
      :style {:background-image (str "url(/img/" photo ")")}}]
    [:span.photo-url
     {:class "text-center block text-white text-xs"}
     photo]]])

(defn reorder-panel
  [{:keys [active]}]
  [settings/panel
   {:class "w-80" :active active}
   (let [state (store/get-state)
         {reorder-state :state context :context} (reorder/select-machine state)

         {:keys [source target]} context
         photos (photos/select-photos state)]
     [:div.photos-list
      {:class (r/class-names
               "flex flex-col gap-4 overflow-y-auto max-h-full"
               (when (= reorder-state :dragging) "cursor-move"))}
      (for [[i photo] (map-indexed vector photos)]
        [photo-item
         {:photo photo
          :source source
          :target target
          :key photo}])])])
