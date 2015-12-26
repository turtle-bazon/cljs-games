(ns freecell.core
  (:require [reagent.core :as r]
            [goog.string :refer [unescapeEntities]]
            [goog.events :as events]
            [freecell.logic :as logic])
  (:import [goog.events EventType]))

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

(def ui-state (r/atom {}))

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

(defn set-pile-selection-location!
  [event]
  (let [origin-location (get-client-location (.getElementById js/document "board"))
        target-location (get-client-location (.-target event))
        offset {:x (- (.-clientX event) (:x target-location))
                :y (- (.-clientY event) (:y target-location))}
        location {:x (- (.-clientX event) (:x origin-location) (:x offset))
                  :y (- (.-clientY event) (:y origin-location) (:y offset))}]
    (swap! ui-state #(assoc % :draggable-pile {:offset offset
                                               :location location}))))

(defn on-drag-pile!
  [event]
  (let [origin-location (get-client-location (.getElementById js/document "board"))
        offset (get-in @ui-state [:draggable-pile :offset])
        location {:x (- (.-clientX event) (:x origin-location) (:x offset))
                  :y (- (.-clientY event) (:y origin-location) (:y offset))}]
    (swap! ui-state  #(assoc-in % [:draggable-pile :location] location))))

(defn on-drop-pile!
  [event]
  (logic/drop-pile!)
  ;; TODO use reagent events
  (events/unlisten js/window EventType.MOUSEMOVE on-drag-pile!)
  (events/unlisten js/window EventType.MOUSEUP on-drop-pile!))

(defn on-pile-select!
  [block pile-position card-position event]
  (logic/select-pile! block pile-position card-position)
  (set-pile-selection-location! event)
  ;; TODO use reagent events
  (events/listen js/window EventType.MOUSEMOVE on-drag-pile!)
  (events/listen js/window EventType.MOUSEUP on-drop-pile!))

(defn card-component
  [card block pile-position card-position selected]
  (let [rank (:rank card)
        rank-html (get ranks rank)
        suit (:suit card)
        suit-html (unescapeEntities (get suits suit))
        location-y (if (not= block :foundations)
                     (* card-position mini-card-height)
                     0)
        color (case suit
                :hearts "red"
                :diamonds "red"
                :clubs "black"
                :spades "black")]
    [:div.unselectable.card-place.card
     {:class (when selected "selected-card")
      :style {:top (str location-y "px") :color color}
      :on-mouse-down (fn [event]
                       (on-pile-select! block pile-position card-position event))}
     (str rank-html suit-html)]))

(defn pile-component
  [cards block pile-position placeholder draggable-pile draggable-card-position]
  (.log js/console "pile-component")
  (let [height (+ card-height (* (count cards) mini-card-height))]
    [:div.cards-pile
     {:style {:left (str (* pile-position card-width) "px")
              :height height}
      :on-mouse-up (fn [event]
                     (when draggable-pile
                       (logic/drop-pile-to! block pile-position)))}
     (if (not (empty? cards))
       (for [card-position (range 0 (count cards))
             :let [card (nth cards card-position)
                   selected (and draggable-card-position
                                 (<= draggable-card-position card-position))]]
         ^{:key (:key card)} [card-component card block pile-position card-position selected])
       [:div.unselectable.card-place placeholder])]))

(defn pile-component-at
  [cards block location]
  [:div.draggable-pile.cards-pile {:style {:left (str (:x location) "px")
                                           :top (str (:y location) "px")}}
   (for [card-position (range 0 (count cards))
         :let [card (nth cards card-position)]]
     ^{:key (:key card)} [card-component card block nil card-position false])])

(defn draggable-pile-component!
  []
  (when-let [pile-info (logic/get-selected-pile-info!)]
    [pile-component-at (logic/get-draggable-pile!) (:block pile-info)
     (get-in @ui-state [:draggable-pile :location])]))

(defn cards-block-component!
  ([piles block]
   (cards-block-component! piles block nil))
  ([piles block placeholder]
   (let [width (* (count piles) card-width)
         draggable-pile-info (logic/get-selected-pile-info!)]
     [:div.cards-block {:style {:width width}}
      (for [position (range 0 (count piles))
            :let [pile (nth piles position)]]
        (let [draggable-card-position (if (and (= block (:block draggable-pile-info))
                                               (= position (:position draggable-pile-info)))
                                        (:card-position draggable-pile-info))]
          ^{:key position} [pile-component pile block position placeholder draggable-pile-info
                            draggable-card-position]))])))

(defn freecells-component!
  []
  (.log js/console "freecells-component")
  (cards-block-component! (logic/get-block! :freecells) :freecells))

(defn foundations-component!
  []
  (cards-block-component! (logic/get-block! :foundations) :foundations "A"))

(defn tableau-component!
  []
  (cards-block-component! (logic/get-block! :tableau) :tableau))

(defn undo!
  []
  (on-drop-pile! nil)
  (logic/undo!))

(defn controls-component!
  []
  [:div.controls-panel
   ^{:key :undo} [:button {:on-click undo!} "Undo"]
   ^{:key :redo} [:button {:on-click logic/redo!} "Redo"]])

(defn board-component!
  []
  [:div#board.board
   ^{:key :freecells} [:div.row [freecells-component!] [foundations-component!]]
   ^{:key :tableau} [:div.row [tableau-component!]]
   ^{:key :draggable-pile} [draggable-pile-component!]
   ^{:key :controls} [controls-component!]
   ^{:key :elapsed} [:div.row [elapsed-component!]]])

(defn mountit!
  []
  (r/render-component [board-component!]
                      (.getElementById js/document "app")))

(defn ^:export run
  []
  (logic/init-game-state!)
  (mountit!)
  (logic/auto-move-to-foundations!))
