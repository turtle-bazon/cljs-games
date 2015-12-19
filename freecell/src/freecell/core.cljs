(ns freecell.core
  (:require [reagent.core :as r]))

(def state (r/atom 0))

(defn elapsed
  []
  (let [seconds (r/atom 0)]
    (fn []
      (js/setTimeout #(swap! seconds inc) 1000)
      [:p "Elapsed: " @seconds])))

(defn freecells
  []
  [:div
   (for [item (range 0 4)]
     ^{:key item} [:div.card])])

(defn foundations
  []
  [:div
   (for [item (range 0 4)]
     ^{:key item} [:div.card "A"])])

(defn tableau
  []
  [:div
   (for [item (range 0 8)]
     ^{:key item} [:div.card "?"])])

(defn board
  []
  [:div
   [:div.row [freecells] [foundations]]
   [:div.row [tableau]]
   [:div.row [elapsed]]])

(defn mountit
  []
  (r/render-component [board]
                      (.getElementById js/document "app")))

(defn ^:export run
  []
  (mountit))
