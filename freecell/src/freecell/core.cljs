(ns freecell.core
  (:require [reagent.core :as r]
            [goog.string :refer [unescapeEntities]]))

(def state (r/atom 0))
(def card-width 80)
(def card-height 100)
(def mini-card-height 20)
(def suits {:hearts "&hearts;"
            :diamonds "&diams;"
            :clubs "&clubs;"
            :spades "&spades;"})
(def ranks {1 "A"
            2 "2"
            3 "3"
            4 "4"
            5 "5"
            6 "6"
            7 "7"
            8 "8"
            9 "9"
            10 "10"
            11 "J"
            12 "Q"
            13 "K"})

(defn elapsed-component
  []
  (let [seconds (r/atom 0)]
    (fn []
      (js/setTimeout #(swap! seconds inc) 1000)
      [:p.elapsed-time "Elapsed: " @seconds])))

;; TODO need block id
(defn select-pile
  [position event]
  (let [offset {:x (.-clientX event)
                :y (.-clientY event)}]
    (swap! state #(assoc % :draggable-pile {:id position
                                            :offset offset}))))

(defn deselect-pile
  [event]
  (swap! state #(assoc % :draggable-pile nil)))

;; (defn move-pile
;;   [event]
;;   (let [offset {:x (.-clientX event)
;;                 :y (.-clientY event)}]
;;     (swap! state #(update % :draggable-pile {:id position
;;                                              :offset offset}))))

(defn card-component
  [card position]
  (let [rank (:rank card)
        rank-html (get ranks rank)
        suit (:suit card)
        suit-html (unescapeEntities (get suits suit))
        color (case suit
                :hearts "red"
                :diamonds "red"
                :clubs "black"
                :spades "black")]
    [:div.card-place.card
     {:style {:top (str (* position mini-card-height) "px") :color color}
      :on-mouse-down (fn [event]
                       (select-pile position event))
      :on-mouse-up (fn [event]
                     (deselect-pile event))}
     (str rank-html suit-html)]))

(defn pile-component
  [cards position placeholder]
  (let [height (+ card-height (* (count cards) mini-card-height))]
    [:div.cards-pile {:style {:left (str (* position card-width) "px")
                              :height height}}
     (if (not (empty? cards))
       (for [position (range 0 (count cards))
             :let [card (nth cards position)]]
         ^{:key (:key card)} [card-component card position])
       [:div.card-place placeholder])]))

(defn draggable-pile-component
  []
  (when-let [pile (:draggable-pile @state)]
    (let [position 12.5
          placeholder "D"]
      [pile-component () position placeholder])))

(defn cards-block-component
  ([piles]
   (cards-block-component piles nil))
  ([piles placeholder]
   (let [width (* (count piles) card-width)]
     [:div.cards-block {:style {:width width}}
      (for [position (range 0 (count piles))
            :let [pile (nth piles position)]]
        ^{:key position} [pile-component pile position placeholder])])))

(defn freecells-component
  []
  (cards-block-component (:freecells @state)))

(defn foundations-component
  []
  (cards-block-component (:foundations @state) "A"))

(defn tableau-component
  []
  (cards-block-component (:tableau @state)))

(defn board-component
  []
  [:div.board
   ^{:key :freecells} [:div.row [freecells-component] [foundations-component]]
   ^{:key :tableau} [:div.row [tableau-component]]
   ^{:key :draggable-pile} [draggable-pile-component]
   ^{:key :elapsed} [:div.row [elapsed-component]]])

(defn mountit
  []
  (r/render-component [board-component]
                      (.getElementById js/document "app")))

(defn shuffle-deck
  []
  (let [deck (for [suit (keys suits)
                   rank (keys ranks)]
               {:suit suit
                :rank rank
                :key (str suit rank)})
        shuffled (shuffle deck)]
    {:freecells [() () () ()]
     :foundations [() () () ()]
     :tableau
     (into (vec (for [index (range 0 4)]
                  (take 7 (drop (* index 7) shuffled))))
           (vec (for [index (range 0 4)]
                  (take 6 (drop (+ 28 (* index 6)) shuffled)))))
     :draggable-pile nil}))

(defn init-state
  []
  (reset! state (shuffle-deck)))

(defn ^:export run
  []
  (init-state)
  (mountit))
