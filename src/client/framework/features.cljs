(ns framework.features)

(def features (atom {}))

(defn register
  [name feature]
  (swap! features assoc name feature))

