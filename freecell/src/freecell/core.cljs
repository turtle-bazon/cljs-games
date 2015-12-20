(ns freecell.core
  (:require [reagent.core :as r]
            [goog.string :refer [unescapeEntities]]
            [goog.events :as events])
  (:import [goog.events EventType]))

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

(defn get-client-location
  [node]
  (let [rect (.getBoundingClientRect node)]
    {:x (.-left rect)
     :y (.-top rect)}))

(defn select-pile
  [block position event]
  (let [origin-location (get-client-location (.getElementById js/document "board"))
        target-location (get-client-location (.-target event))
        offset {:x (- (.-clientX event) (:x target-location))
                :y (- (.-clientY event) (:y target-location))}
        location {:x (- (.-clientX event) (:x origin-location) (:x offset))
                  :y (- (.-clientY event) (:y origin-location) (:y offset))}]
    (swap! state #(assoc % :draggable-pile {:block block
                                            :position position
                                            :offset offset
                                            :location location}))))

(defn move-pile
  [event]
  (let [origin-location (get-client-location (.getElementById js/document "board"))
        offset (get-in @state [:draggable-pile :offset])
        location {:x (- (.-clientX event) (:x origin-location) (:x offset))
                  :y (- (.-clientY event) (:y origin-location) (:y offset))}]
    (swap! state #(assoc-in % [:draggable-pile :location] location))))

(defn drop-pile-to
  [block position event]
  (let [from-pile-info (:draggable-pile @state)
        from-pile (get-in @state [(:block from-pile-info)
                                  (:position from-pile-info)])
        card (last from-pile)]
    (swap! state (fn [state]
                   (update-in state [(:block from-pile-info)
                                     (:position from-pile-info)]
                              (fn [pile]
                                (pop pile)))))
    (swap! state (fn [state]
                   (update-in state [block position] (fn [pile]
                                                       (conj pile card)))))))

(defn drop-pile
  [event]
  (swap! state #(assoc % :draggable-pile nil))
  (events/unlisten js/window EventType.MOUSEMOVE move-pile)
  (events/unlisten js/window EventType.MOUSEUP drop-pile))

(defn on-card-click
  [block position event]
  (select-pile block position event)
  (events/listen js/window EventType.MOUSEMOVE move-pile)
  (events/listen js/window EventType.MOUSEUP drop-pile))

(defn card-component
  [card block position]
  (let [rank (:rank card)
        rank-html (get ranks rank)
        suit (:suit card)
        suit-html (unescapeEntities (get suits suit))
        color (case suit
                :hearts "red"
                :diamonds "red"
                :clubs "black"
                :spades "black")]
    [:div.unselectable.card-place.card
     {:style {:top (str (* position mini-card-height) "px") :color color}}
     (str rank-html suit-html)]))

(defn pile-component
  [cards block position placeholder]
  (let [height (+ card-height (* (count cards) mini-card-height))]
    [:div.cards-pile
     {:style {:left (str (* position card-width) "px")
              :height height}
      :on-mouse-down (fn [event]
                       (when (not (empty? cards))
                         (on-card-click block position event)))
      :on-mouse-up (fn [event]
                     (when (:draggable-pile @state)
                       (drop-pile-to block position event)))}
     (if (not (empty? cards))
       (for [position (range 0 (count cards))
             :let [card (nth cards position)]]
         ^{:key (:key card)} [card-component card block position])
       [:div.unselectable.card-place placeholder])]))

(defn pile-component-at
  [cards block location placeholder]
  (let [height (+ card-height (* (count cards) mini-card-height))]
    [:div.draggable-pile.cards-pile {:style {:left (str (:x location) "px")
                                             :top (str (:y location) "px")}}
     (if (not (empty? cards))
       (for [position (range 0 (count cards))
             :let [card (nth cards position)]]
         ^{:key (:key card)} [card-component card block position])
       [:div.unselectable.card-place placeholder])]))

(defn draggable-pile-component
  []
  (when-let [pile (:draggable-pile @state)]
    (let [placeholder "D"]
      [pile-component-at () (:block pile) (:location pile) placeholder])))

(defn cards-block-component
  ([piles block]
   (cards-block-component piles block nil))
  ([piles block placeholder]
   (let [width (* (count piles) card-width)]
     [:div.cards-block {:style {:width width}}
      (for [position (range 0 (count piles))
            :let [pile (nth piles position)]]
        ^{:key position} [pile-component pile block position placeholder])])))

(defn freecells-component
  []
  (cards-block-component (:freecells @state) :freecells))

(defn foundations-component
  []
  (cards-block-component (:foundations @state) :foundations "A"))

(defn tableau-component
  []
  (cards-block-component (:tableau @state) :tableau))

(defn board-component
  []
  [:div#board.board
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
    {:freecells [[] [] [] []]
     :foundations [[] [] [] []]
     :tableau
     (into (vec (for [index (range 0 4)]
                  (vec (take 7 (drop (* index 7) shuffled)))))
           (vec (for [index (range 0 4)]
                  (vec (take 6 (drop (+ 28 (* index 6)) shuffled))))))
     :draggable-pile nil}))

(defn init-state
  []
  (reset! state (shuffle-deck)))

(defn ^:export run
  []
  (init-state)
  (mountit))
