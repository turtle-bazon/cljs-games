(ns freecell.core
  (:require [reagent.core :as r]
            [goog.events :as events]
            [freecell.logic :as logic])
  (:import [goog.events EventType]))

(def card-width 80)
(def card-height 100)
(def mini-card-height 20)
(def animation-speed 100)
(def animation-interval 50)
(def image-url "url('./images/playingCards.png')")
(def card-original-tile-width 149.75)
(def card-original-tile-height 199)
(def image-original-width 2086)
(def image-original-height 786)
(def card-tile-width 75)
(def card-tile-height 100)
(def image-width (* (/ image-original-width card-original-tile-width) card-tile-width))
(def image-height (* (/ image-original-height card-original-tile-height) card-tile-height))

(defonce ui-state (r/atom {}))

(defn log
  [& msgs]
  (.log js/console (apply str msgs)))

(defn elapsed-component!
  []
  (let [seconds (r/atom 0)]
    (fn []
      (js/setTimeout #(swap! seconds inc) 1000)
      [:div.elapsed-time "Elapsed: " @seconds])))

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

(defn- get-location
  [card]
  (let [board-bounds (.getBoundingClientRect (.getElementById js/document "board"))
        from-block-bounds (.getBoundingClientRect (.getElementById js/document
                                                                   (str (:block card))))
        from-block-location-x (.-left from-block-bounds)
        from-block-location-y (.-top from-block-bounds)
        pile-relative-location-x (* (:pile card) card-width)
        location-x (+ (- (.-left board-bounds)) from-block-location-x pile-relative-location-x)
        card-relative-location-y (if (not= (:block card) :foundations)
                                   (* (:card card) mini-card-height)
                                   0)
        location-y (+ (- (.-top board-bounds)) from-block-location-y card-relative-location-y)]
    {:x location-x :y location-y}))

(defn- abs
  [n]
  (if (< 0 n)
    n
    (- n)))

(defn- next-location!
  [current-location]
  (when-let [animation (:animation @ui-state)]
    (let [on-stop (:on-stop animation)
          stop-location (get-location (:to animation))
          distance-x (- (:x stop-location) (:x current-location))
          distance-y (- (:y stop-location) (:y current-location))
          max-distance (max (abs distance-x) (abs distance-y))
          turns-count (/ max-distance animation-speed)
          dx (/ distance-x turns-count)
          dy (/ distance-y turns-count)
          new-location-x (if (< (abs distance-x) (abs dx))
                           (:x stop-location)
                           (+ (:x current-location) dx))
          new-location-y (if (< (abs distance-y) (abs dy))
                           (:y stop-location)
                           (+ (:y current-location) dy))]
      (when (and (= new-location-x (:x stop-location))
                 (= new-location-y (:y stop-location)))
        (swap! ui-state assoc :animation nil)
        (on-stop true))
      {:x new-location-x :y new-location-y})))

(defn get-card-tile-position
  [card]
  (let [x (- (* (dec (:rank card)) card-tile-width))
        suit-index (case (:suit card)
                     :spades 0
                     :clubs 1
                     :diamonds 2
                     :hearts 3)
        y (- (* suit-index card-tile-height))]
    (str x "px " y "px")))

(defn card-component
  [card x y options]
  [:div.unselectable.card-place
   (merge {:style {:left (str x "px")
                   :top (str y "px")
                   :background-image image-url
                   :background-size (str image-width "px " image-height "px")
                   :background-repeat "no-repeat"
                   :background-position (get-card-tile-position card)}}
          options)])

(defn- animated-card-component!
  []
  (let [location (r/atom nil)]
    (fn []
      (when-let [animation (:animation @ui-state)]
        (if (not (:started animation))
          (do
            (reset! location (get-location (:from animation)))
            (swap! ui-state assoc-in [:animation :started] true)
            nil)
          (let [current-location @location]
            (js/setTimeout #(swap! location next-location!) animation-interval)
            (let [card (:card animation)]
              (card-component card (:x current-location) (:y current-location) nil))))))))

(defn- set-card-animation!
  [from to card on-stop]
  (swap! ui-state assoc :animation {:from from
                                    :to to
                                    :card card
                                    :on-stop on-stop}))

(defn- stop-animation!
  []
  (when-let [animation (:animation @ui-state)]
    ((:on-stop animation) false))
  (swap! ui-state assoc :animation nil))

(defn on-pile-select!
  [block pile-position card-position event]
  (when (not (logic/win?!))
    (logic/select-pile! block pile-position card-position)
    (set-pile-selection-location! event)
    ;; TODO use reagent events
    (events/listen js/window EventType.MOUSEMOVE on-drag-pile!)
    (events/listen js/window EventType.MOUSEUP on-drop-pile!)))

(defn fixed-card-component
  [card block pile-position card-position selected]
  (let [location-y (if (not= block :foundations)
                     (* card-position mini-card-height)
                     0)]
    (card-component card 0 location-y
                    {:class (when selected "selected-card")
                     :on-mouse-down (fn [event]
                                      (on-pile-select! block pile-position
                                                       card-position event))})))

(defn pile-component
  [cards block pile-position placeholder draggable-card-position]
  (.log js/console "pile-component")
  (let [height (+ card-height (* (dec (count cards)) mini-card-height))]
    [:div.cards-pile
     {:style {:left (str (* pile-position card-width) "px")
              :height height}
      :on-mouse-up (fn [event]
                     (logic/drop-pile-to! block pile-position)
                     1)} ;; react doesn't like boolean values to return from event handlers
     (if (not (empty? cards))
       (for [card-position (range 0 (count cards))
             :let [card (nth cards card-position)
                   selected (and draggable-card-position
                                 (<= draggable-card-position card-position))]]
         ^{:key (:key card)} [fixed-card-component card block pile-position
                              card-position selected])
       [:div.unselectable.card-place.no-card placeholder])]))

(defn pile-component-at
  [cards block location]
  [:div.draggable-pile.cards-pile {:style {:left (str (:x location) "px")
                                           :top (str (:y location) "px")}}
   (for [card-position (range 0 (count cards))
         :let [card (nth cards card-position)]]
     ^{:key (:key card)} [fixed-card-component card block nil card-position false])])

(defn draggable-pile-component!
  []
  (when-let [pile-info (logic/get-selected-pile-info!)]
    [pile-component-at (logic/get-draggable-pile!) (:block pile-info)
     (get-in @ui-state [:draggable-pile :location])]))

(defn block-component
  [block piles draggable-pile-info placeholder]
  (let [width (* (count piles) card-width)
        draggable-pile-position (:position draggable-pile-info)]
    [:div.cards-block {:style {:width width}
                       :id (str block)}
     (for [position (range 0 (count piles))
           :let [pile (nth piles position)]]
       (let [draggable-card-position-for-pile
             (when (= position draggable-pile-position)
               (:card-position draggable-pile-info))]
         ^{:key position} [pile-component pile block position placeholder
                           draggable-card-position-for-pile]))]))

(defn block-component!
  ([block piles]
   (block-component! block piles nil))
  ([block piles placeholder]
   (let [draggable-pile-info (logic/get-selected-pile-info!)
         draggable-pile-info-for-block (when (= block (:block draggable-pile-info))
                                         draggable-pile-info)]
     (block-component block piles draggable-pile-info-for-block placeholder))))

(defn freecells-component!
  []
  (block-component! :freecells (logic/get-block! :freecells)))

(defn foundations-component!
  []
  (block-component! :foundations (logic/get-block! :foundations) "A"))

(defn tableau-component!
  []
  (block-component! :tableau (logic/get-block! :tableau)))

(defn win-component!
  []
  (when (logic/win?!)
    [:div.win-block "Congratulations! You've won!"]))

(defn start-new-game!
  []
  (logic/init-game-state! set-card-animation!)
  (logic/auto-move-to-foundations! false))

(defn controls-component!
  []
  [:div.controls-panel
   ^{:key :new-game} [:button {:on-click (fn []
                                           (stop-animation!)
                                           (start-new-game!))}
                      "New game"]
   ^{:key :restart} [:button {:on-click (fn []
                                          (stop-animation!)
                                          (logic/restart!))}
                     "<<"]
   ^{:key :undo} [:button {:on-click (fn []
                                       (stop-animation!)
                                       (when (not (logic/win?!))
                                         (logic/undo!)))}
                  "<"]
   ^{:key :redo} [:button {:on-click (fn []
                                       (stop-animation!)
                                       (when (not (logic/win?!))
                                         (logic/redo!)))}
                  ">"]])

(defn board-component!
  []
  [:div#board.board
   ^{:key :controls} [:div.row [controls-component!] [elapsed-component!]]
   ^{:key :freecells} [:div.row [freecells-component!] [foundations-component!]]
   ^{:key :tableau} [:div.row [tableau-component!]]
   ^{:key :draggable-pile} [draggable-pile-component!]
   ^{:key :win} [win-component!]
   ^{:key :animation} [:div.row [animated-card-component!]]])

(defn mountit!
  []
  (r/render-component [board-component!]
                      (.getElementById js/document "app"))
  (events/listen js/window EventType.MOUSEDOWN stop-animation!))

(defn run
  []
  (mountit!)
  (start-new-game!))

(set! (.-onload js/window) run)
