(ns montage.features.reorder.views
  (:require
   [montage.store :as store]
   [montage.features.photos.feature :as photos]
   [montage.features.settings.views :as settings]))

(defn reorder-panel
  [{:keys [active]}]
  [settings/panel
   {:class "w-80" :active active}
   (let [state (store/get-state)
         photos (photos/select-photos state)]
     [:div.photos-list
      {:class "flex flex-col gap-4"}
      (for [[i photo] (map-indexed vector photos)]
        [:div.photo
         {:key photo}
         [:div
          {:class "w-60 h-40 bg-cover bg-center m-auto"
           :style {:background-image (str "url(/img/" photo ")")}}]
         [:span
          {:class "text-center block text-white text-xs"}
          photo]])])])
