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


(defn elapsed-component!
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

(defn select-pile!
  [block position event]
  (let [origin-location (get-client-location (.getElementById js/document "board"))
        target-location (get-client-location (.-target event))
        offset {:x (- (.-clientX event) (:x target-location))
                :y (- (.-clientY event) (:y target-location))}
        location {:x (- (.-clientX event) (:x origin-location) (:x offset))
                  :y (- (.-clientY event) (:y origin-location) (:y offset))}]
    (swap! state #(assoc % :draggable-pile {:block block
                                            :position position
                                            :offset offset}))
    (swap! state #(assoc % :draggable-pile-location location))))

(defn on-drag-pile!
  [event]
  (let [origin-location (get-client-location (.getElementById js/document "board"))
        offset (get-in @state [:draggable-pile :offset])
        location {:x (- (.-clientX event) (:x origin-location) (:x offset))
                  :y (- (.-clientY event) (:y origin-location) (:y offset))}]
    (swap! state #(assoc % :draggable-pile-location location))))

(defn get-card-color
  [card]
  (case (:suit card)
    :hearts :red
    :diamonds :red
    :clubs :black
    :spades :black))

(defn in-tableau-order?
  [top-card bottom-card]
  (and (not= (get-card-color top-card) (get-card-color bottom-card))
       (= (- (:rank bottom-card) (:rank top-card)) 1)))

(defn get-card-to-move!
  [from-pile to-pile to-block]
  (case to-block
    :freecells (if (empty? to-pile)
                 (last from-pile)
                 nil)
    :foundations (if (empty? to-pile)
                   (if (= (:rank (last from-pile)) 1)
                     (last from-pile)
                     nil)
                   (let [from-card (last from-pile)
                         to-card (last to-pile)]
                     (if (and (= (:suit from-card) (:suit to-card))
                              (= (- (:rank from-card) (:rank to-card)) 1))
                       (last from-pile)
                       nil)))
    :tableau
    (let [max-moves (+ (count (filter empty? (:freecells @state)))
                       (count (filter empty? (:tableau @state)))
                       1)
          to-card (last to-pile)
          reversed-pile (reverse from-pile)]
      (.log js/console "max-moves: " max-moves)
      (first (filter (fn [card]
                       (in-tableau-order? card to-card))
                     (cons (first reversed-pile)
                           (take (dec max-moves)
                                 (take-while some?
                                             (map (fn [top-card bottom-card]
                                                    (if (in-tableau-order? top-card bottom-card)
                                                      bottom-card
                                                      nil))
                                                  reversed-pile
                                                  (rest reversed-pile))))))))))

(defn get-cards-to-drop-count!
  [from-pile to-pile to-block]
  (case to-block
    :freecells (if (empty? to-pile)
                 1 0)
    :foundations (if (empty? to-pile)
                   (if (= (:rank (last from-pile)) 1)
                     1 0)
                   (let [from-card (last from-pile)
                         to-card (last to-pile)]
                     (if (and (= (:suit from-card) (:suit to-card))
                              (= (- (:rank from-card) (:rank to-card)) 1))
                       1 0)))
    :tableau (if (empty? to-pile)
               1
               (if-let [from-card (get-card-to-move! from-pile to-pile :tableau)]
                 (inc (count (take-while #(not= % from-card) (reverse from-pile))))
                 0))))

(defn update-foundations-rank!
  [card]
  (swap! state (fn [state]
                 (assoc state :foundations-rank
                   (inc (apply min (map (fn [pile]
                                          (:rank (last pile)))
                                        (:foundations state))))))))

(defn move-pile!
  [from-block from-position from-pile to-block to-position to-pile]
  (let [cards-count (get-cards-to-drop-count! from-pile to-pile to-block)]
    (.log js/console "drop cards count: " cards-count)
    (swap! state (fn [state]
                   (update-in state [from-block from-position]
                              (fn [pile]
                                (vec (drop-last cards-count pile))))))
    (swap! state (fn [state]
                   (update-in state [to-block to-position] (fn [pile]
                                                             (into pile (take-last cards-count from-pile))))))))
(defn auto-move-to-foundations!
  []
  (let [current-state @state
        foundations (:foundations current-state)
        tableau (:tableau current-state)
        foundations-rank (:foundations-rank current-state)]
    (.log js/console "rank " foundations-rank)
    (when (some some?
                (for [from-pile-position (range 0 (count tableau))
                      :let [pile (nth tableau from-pile-position)
                            card (last pile)]
                      :when card]
                  (do
                    (when (<= (:rank card) foundations-rank)
                      (.log js/console "to foundations: " card)
                      (let [suit-position (count (take-while #(not= (:suit card) (:suit (first %)))
                                                             foundations))
                            to-pile-position (if (< suit-position (count foundations))
                                               suit-position
                                               (count (take-while seq foundations)))
                            to-pile (nth foundations to-pile-position)]
                        (move-pile! :tableau from-pile-position pile :foundations to-pile-position to-pile)
                        (update-foundations-rank! (last pile))
                        true)))))
      (recur))))

(defn drop-pile-to!
  [block position]
  (let [from-pile-info (:draggable-pile @state)
        from-pile (get-in @state [(:block from-pile-info)
                                  (:position from-pile-info)])
        to-pile (get-in @state [block position])]
    (move-pile! (:block from-pile-info)
                   (:position from-pile-info)
                   from-pile
                   block
                   position
                   to-pile)
    (auto-move-to-foundations!)))

(defn on-drop-pile!
  [event]
  (swap! state #(assoc % :draggable-pile nil))
  (swap! state #(assoc % :draggable-card nil))
  (events/unlisten js/window EventType.MOUSEMOVE on-drag-pile!)
  (events/unlisten js/window EventType.MOUSEUP on-drop-pile!))

(defn set-draggable-card!
  [block position event]
  (let [from-pile-info (:draggable-pile @state)
        from-pile (get-in @state [(:block from-pile-info)
                                  (:position from-pile-info)])
        to-pile (get-in @state [block position])
        card-to-move (get-card-to-move! from-pile to-pile block)]
    (swap! state #(assoc % :draggable-card card-to-move))))

(defn on-pile-select!
  [block position event]
  (select-pile! block position event)
  (events/listen js/window EventType.MOUSEMOVE on-drag-pile!)
  (events/listen js/window EventType.MOUSEUP on-drop-pile!))

(defn card-component
  [card block position selected]
  (let [rank (:rank card)
        rank-html (get ranks rank)
        suit (:suit card)
        suit-html (unescapeEntities (get suits suit))
        location-y (if (not= block :foundations)
                     (* position mini-card-height)
                     0)
        color (case suit
                :hearts "red"
                :diamonds "red"
                :clubs "black"
                :spades "black")]
    [:div.unselectable.card-place.card
     {:class (when selected "selected-card")
      :style {:top (str location-y "px") :color color}}
     (str rank-html suit-html)]))

(defn pile-component
  [cards block position placeholder draggable-pile draggable-card draggable-card-position]
  (.log js/console "pile-component")
  (let [height (+ card-height (* (count cards) mini-card-height))]
    [:div.cards-pile
     {:style {:left (str (* position card-width) "px")
              :height height}
      :on-mouse-down (fn [event]
                       (when (not (empty? cards))
                         (on-pile-select! block position event)))
      :on-mouse-up (fn [event]
                     (when draggable-pile
                       (drop-pile-to! block position)))
      :on-mouse-enter (fn [event]
                        (when draggable-pile
                          (set-draggable-card! block position event)))}
     (if (not (empty? cards))
       (for [position (range 0 (count cards))
             :let [card (nth cards position)
                   selected (and draggable-card-position
                                 (<= draggable-card-position position))]]
         ^{:key (:key card)} [card-component card block position selected])
       [:div.unselectable.card-place placeholder])]))

(defn pile-component-at
  [card block location]
  [:div.draggable-pile.cards-pile {:style {:left (str (:x location) "px")
                                           :top (str (:y location) "px")}}
   (if card
     [card-component card block 0]
     [:div.unselectable.card-place])])

(defn draggable-pile-component!
  []
  (when-let [pile (:draggable-pile @state)]
    [pile-component-at (:draggable-card @state) (:block pile) (:draggable-pile-location @state)]))

(defn cards-block-component!
  ([piles block]
   (cards-block-component! piles block nil))
  ([piles block placeholder]
   (let [width (* (count piles) card-width)
         draggable-pile (:draggable-pile @state)
         draggable-card (:draggable-card @state)]
     [:div.cards-block {:style {:width width}}
      (for [position (range 0 (count piles))
            :let [pile (nth piles position)]]
        (let [draggable-card-position (if (and (= block (:block draggable-pile))
                                               (= position (:position draggable-pile)))
                                        (if draggable-card
                                          (count (take-while #(not= % draggable-card) pile))))]
          ^{:key position} [pile-component pile block position placeholder draggable-pile (and draggable-card-position draggable-card) draggable-card-position]))])))

(defn freecells-component!
  []
  (.log js/console "freecells-component")
  (cards-block-component! (:freecells @state) :freecells))

(defn foundations-component!
  []
  (cards-block-component! (:foundations @state) :foundations "A"))

(defn tableau-component!
  []
  (cards-block-component! (:tableau @state) :tableau))

(defn board-component!
  []
  [:div#board.board
   ^{:key :freecells} [:div.row [freecells-component!] [foundations-component!]]
   ^{:key :tableau} [:div.row [tableau-component!]]
   ^{:key :draggable-pile} [draggable-pile-component!]
   ^{:key :elapsed} [:div.row [elapsed-component!]]])

(defn mountit!
  []
  (r/render-component [board-component!]
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
     :foundations-rank 1
     :draggable-pile nil
     :draggable-card nil}))

(defn init-state!
  []
  (reset! state (shuffle-deck)))

(defn ^:export run
  []
  (init-state!)
  (mountit!)
  (auto-move-to-foundations!))
