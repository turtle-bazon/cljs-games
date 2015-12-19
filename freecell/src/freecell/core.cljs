(ns freecell.core
  (:require [reagent.core :as r]))

(def state (r/atom 0))
(def card-width 80)
(def card-height 100)

(defn elapsed-component
  []
  (let [seconds (r/atom 0)]
    (fn []
      (js/setTimeout #(swap! seconds inc) 1000)
      [:p "Elapsed: " @seconds])))

(defn card-component
  [card]
  (let [position (:position card)
        value (:value card)]
    [:div.card {:style {:left (str (* position card-width) "px")}} value]))

(defn cards-block-component
  [cards]
  (let [width (* (count cards) card-width)]
    [:div.cards-block {:style {:width width}}
     (for [card cards]
       ^{:key card} [card-component card])]))

(defn freecells-component
  []
  (cards-block-component (map (fn [position]
                                {:position position
                                 :value nil})
                              (range 0 4))))

(defn foundations-component
  []
  (cards-block-component (map (fn [position]
                                {:position position
                                 :value "A"})
                              (range 0 4))))

(defn tableau-component
  []
  (cards-block-component (map (fn [position]
                                {:position position
                                 :value "?"})
                              (range 0 8))))

(defn board-component
  []
  [:div
   [:div.row [freecells-component] [foundations-component]]
   [:div.row [tableau-component]]
   [:div.row [elapsed-component]]])

(defn mountit
  []
  (r/render-component [board-component]
                      (.getElementById js/document "app")))

(defn ^:export run
  []
  (mountit))
