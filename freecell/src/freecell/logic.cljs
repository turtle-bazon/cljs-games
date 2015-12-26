(ns freecell.logic
  (:require [reagent.core :as r]
            [goog.string :refer [unescapeEntities]]
            [goog.events :as events])
  (:import [goog.events EventType]))

(def state (r/atom nil))

(defn get-block!
  [block]
  (block @state))

(defn select-pile!
  [block position]
  (swap! state #(assoc % :draggable-pile {:block block
                                          :position position})))

(defn drop-pile!
  []
  (swap! state #(assoc % :draggable-pile nil))
  (swap! state #(assoc % :draggable-card nil)))

(defn get-selected-pile-info!
  []
  (:draggable-pile @state))

(defn get-selected-pile!
  []
  (let [info (get-selected-pile-info!)]
    (get-in @state [(:block info) (:position info)])))

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
                   (dissoc state :next-state)))
    (swap! state (fn [state]
                   (update state :history conj state))) 
    (swap! state (fn [state]
                   (update-in state [from-block from-position]
                              (fn [pile]
                                (vec (drop-last cards-count pile))))))
    (swap! state #(update-in % [to-block to-position] 
                             (fn [pile]
                               (into pile (take-last cards-count from-pile)))))))

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
                  (when (<= (:rank card) foundations-rank)
                    (.log js/console "to foundations: " card)
                    (let [suit-position (count (take-while #(not= (:suit card) (:suit (first %)))
                                                           foundations))
                          to-pile-position (if (< suit-position (count foundations))
                                             suit-position
                                             (count (take-while seq foundations)))
                          to-pile (nth foundations to-pile-position)]
                      (move-pile! :tableau from-pile-position pile
                                  :foundations to-pile-position to-pile)
                      (update-foundations-rank! (last pile))
                      true))))
      (recur))))

(defn drop-pile-to!
  [block position]
  (let [from-pile-info (:draggable-pile @state)
        from-pile (get-in @state [(:block from-pile-info)
                                  (:position from-pile-info)])
        to-pile (get-in @state [block position])]
    (drop-pile!)
    (move-pile! (:block from-pile-info)
                (:position from-pile-info)
                from-pile
                block
                position
                to-pile)
    (auto-move-to-foundations!)))

(defn set-draggable-card!
  [block position]
  (let [from-pile-info (:draggable-pile @state)
        from-pile (get-in @state [(:block from-pile-info)
                                  (:position from-pile-info)])
        to-pile (get-in @state [block position])
        card-to-move (get-card-to-move! from-pile to-pile block)]
    (swap! state #(assoc % :draggable-card card-to-move))))

(defn get-draggable-card!
  []
  (:draggable-card @state))

(defn undo!
  []
  (drop-pile!)
  (let [current-state @state]
    (when-let [prev-state (first (:history @state))]
      (reset! state (assoc prev-state :next-state current-state)))))

(defn redo!
  []
  (when-let [next-state (:next-state @state)]
    (reset! state next-state)))

(defn shuffle-deck
  []
  (let [deck (for [suit '(:hearts
                          :diamonds
                          :clubs
                          :spades)
                   rank (range 1 14)]
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
     :draggable-card nil
     :history ()}))

(defn init-game-state!
  []
  (reset! state (shuffle-deck)))
