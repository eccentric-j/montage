(ns framework.stream
  (:require
   [cljs.pprint :refer [pprint]]
   ["baconjs" :as bacon]))

(def Bus (.-Bus bacon))
(def EventStream (.-EventStream bacon))

(defn from-event
  [el event]
  (.fromEvent bacon el event))

(defn from-array
  [arr]
  (.fromArray bacon arr))

(defn from-seq
  [xs]
  (.fromArray bacon (clj->js xs)))

(defn from-atom
  [atom]
  (.fromBinder bacon
               (fn source [sink]
                 (let [key (str "from-atom-" (js/Date.now))]
                   (add-watch atom key
                              (fn [_key _ref _prev next]
                                (sink next)))
                   (fn cleanup []
                     (remove-watch atom key))))))

(defn merge-all
  [streams]
  (apply (.-mergeAll bacon) streams))

(defn from-promise
  ([promise]
   (.fromPromise bacon promise false))
  ([promise can-abort]
   (.fromPromise bacon promise can-abort)))

(defn log
  [^EventStream stream]
  (-> stream
      (.doAction pprint)))

(defn later
  [ms-delay v]
  (.later bacon ms-delay v))

(defn interval
  [ms-delay v]
  (.interval bacon ms-delay v))

(defn of
  [v]
  (.once bacon v))
