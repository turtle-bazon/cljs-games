(ns freecell.logic
  (:require [reagent.core :as r]
            [goog.string :refer [unescapeEntities]]
            [goog.events :as events])
  (:import [goog.events EventType]))

(def state (r/atom nil))

(defn get-block!
  [block]
  (block @state))

(defn get-card!
  [card-info]
  (get-in @state [(:block card-info)
                  (:pile card-info)
                  (:card card-info)]))

(defn drop-pile!
  []
  (swap! state #(assoc % :draggable-pile nil)))

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

(defn get-max-moves-count!
  ([]
   (get-max-moves-count! nil nil))
  ([to-block to-card]
   (let [max-moves (+ (count (filter empty? (:freecells @state)))
                      (count (filter empty? (:tableau @state)))
                      1)]
     (if (and (= to-block :tableau)
              (nil? to-card))
       (dec max-moves)
       max-moves))))

(defn- get-cards-chain-size!
  [pile]
  (let [max-moves (get-max-moves-count!)
        reversed-pile (reverse pile)]
    (inc (count
          (take (dec max-moves)
                (take-while some?
                            (map (fn [top-card bottom-card]
                                   (if (in-tableau-order? top-card
                                                          bottom-card)
                                     bottom-card
                                     nil))
                                 reversed-pile
                                 (rest reversed-pile))))))))

(defn select-pile!
  [block pile-position card-position]
  (let [pile (drop card-position (get-in @state [block pile-position]))]
    (if (= (count pile) (get-cards-chain-size! pile))
      (swap! state #(assoc % :draggable-pile {:block block
                                              :position pile-position
                                              :card-position card-position
                                              :pile pile})))))

(defn- can-move-to-foundations-pile?
  [draggable-pile to-card]
  (and (= (count draggable-pile) 1)
       (if (nil? to-card)
         (= (:rank (first draggable-pile)) 1)
         (let [from-card (first draggable-pile)]
           (and (= (:suit from-card) (:suit to-card))
                (= (- (:rank from-card) (:rank to-card)) 1))))))

(defn can-move-to-tableau-pile?!
  [draggable-pile to-card]
  (let [max-moves-count (get-max-moves-count! :tableau to-card)
        moves-count (count draggable-pile)]
    (when (<= moves-count max-moves-count)
      (if to-card
        (in-tableau-order? (first draggable-pile) to-card)
        true))))

(defn can-move-to?!
  [draggable-pile to-block to-card]
  (case to-block
    :freecells (and (= (count draggable-pile) 1)
                    (nil? to-card))
    :foundations (can-move-to-foundations-pile? draggable-pile to-card)
    :tableau (can-move-to-tableau-pile?! draggable-pile to-card)))

(defn update-foundations-rank!
  [card]
  (swap! state (fn [state]
                 (assoc state :foundations-rank
                        (inc (apply min (map (fn [pile]
                                               (:rank (last pile)))
                                             (:foundations state))))))))

(defn- take-card!
  [card-info]
  (swap! state (fn [state]
                 (dissoc state :next-state)))
  (swap! state (fn [state]
                 (update state :history conj state))) 
  (swap! state (fn [state]
                 (update-in state [(:block card-info)
                                   (:pile card-info)]
                            (fn [pile]
                              (vec (drop-last 1 pile)))))))

(defn- drop-card!
  [card-info card]
  (swap! state #(update-in % [(:block card-info)
                              (:pile card-info)]
                           conj card)))

(defn- get-moves
  [state from to draggable-pile]
  (let [free-freecells (filter empty? (:freecells state))
        free-tableau (filter empty? (:tableau state))]
    ))

(defn move-pile!
  [from-block from-position draggable-pile to-block to-position to-pile]
  (when (can-move-to?! draggable-pile to-block (last to-pile))
    (let [cards-count (count draggable-pile)]
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
                                 (into pile draggable-pile)))))))

(defn- auto-move-to-foundations-from-block!
  [foundations foundations-rank block next-fn force]
  (let [current-state @state
        piles (get current-state block)]
    (some some?
          (for [from-pile-position (range 0 (count piles))
                :let [pile (nth piles from-pile-position)
                      card (last pile)]
                :when card]
            (when (<= (:rank card) foundations-rank)
              (let [suit-position (count (take-while #(not= (:suit card) (:suit (first %)))
                                                     foundations))
                    to-pile-position (if (< suit-position (count foundations))
                                       suit-position
                                       (count (take-while seq foundations)))
                    to-pile (nth foundations to-pile-position)]
                (let [from {:block block
                            :pile from-pile-position
                            :card (dec (count pile))}
                      to {:block :foundations
                          :pile to-pile-position
                          :card 0}
                      on-animation-stop (fn [propagate]
                                          (drop-card! to card)
                                          (update-foundations-rank! (last pile))
                                          (when propagate
                                            (next-fn)))]
                  (take-card! from)
                  (if force
                    (on-animation-stop false)
                    ((:animate-fn current-state) from to card on-animation-stop))
                  true)))))))

(defn auto-move-to-foundations!
  [force]
  (let [current-state @state
        foundations (:foundations current-state)
        foundations-rank (:foundations-rank current-state)]
    (when (and (or (auto-move-to-foundations-from-block! foundations foundations-rank :freecells auto-move-to-foundations! force)
                   (auto-move-to-foundations-from-block! foundations foundations-rank :tableau auto-move-to-foundations! force))
               force)
      (recur true))))

(defn drop-pile-to!
  [block position]
  (when-let [from-pile-info (:draggable-pile @state)]
    (let [from-pile (get-in @state [(:block from-pile-info)
                                    (:position from-pile-info)])
          to-pile (get-in @state [block position])]
      (drop-pile!)
      (move-pile! (:block from-pile-info)
                  (:position from-pile-info)
                  (:pile from-pile-info)
                  block
                  position
                  to-pile)
      (auto-move-to-foundations! false))))

(defn get-draggable-pile!
  []
  (get-in @state [:draggable-pile :pile]))

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

(defn- shuffle-deck
  []
  (let [deck (for [suit (list :hearts
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
  [animate-fn]
  (reset! state (assoc (shuffle-deck) :animate-fn animate-fn)))
