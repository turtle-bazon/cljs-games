(ns freecell.logic
  (:require [reagent.core :as r]
            [goog.string :refer [unescapeEntities]]
            [goog.events :as events])
  (:import [goog.events EventType]))

(def state (r/atom nil))

(defn get-block!
  [block]
  (block @state))

(defn- get-pile
  [state pile-info]
  (get-in state [(:block pile-info)
                 (:pile pile-info)]))

(defn- get-card
  [state card-info]
  (let [pile (get-in state [(:block card-info)
                            (:pile card-info)])
        card-position (:card card-info)]
    (if card-position
      (nth pile card-position)
      (last pile))))

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

(defn- get-card-color
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

(defn- get-max-moves-count
  ([state]
   (get-max-moves-count state nil nil))
  ([state to-block to-card]
   (let [max-moves (+ (count (filter empty? (:freecells state)))
                      (count (filter empty? (:tableau state)))
                      1)]
     (if (and (= to-block :tableau)
              (nil? to-card))
       (dec max-moves)
       max-moves))))

(defn- get-cards-chain-size
  [state pile]
  (let [max-moves (get-max-moves-count state)
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
    (if (= (count pile) (get-cards-chain-size @state pile))
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

(defn can-move-to-tableau-pile?
  [state draggable-pile to-card]
  (let [max-moves-count (get-max-moves-count state :tableau to-card)
        moves-count (count draggable-pile)]
    (when (<= moves-count max-moves-count)
      (if to-card
        (in-tableau-order? (first draggable-pile) to-card)
        true))))

(defn- can-move-to?
  [state draggable-pile to-block to-card]
  (case to-block
    :freecells (and (= (count draggable-pile) 1)
                    (nil? to-card))
    :foundations (can-move-to-foundations-pile? draggable-pile to-card)
    :tableau (can-move-to-tableau-pile? state draggable-pile to-card)))

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

(defn move-card
  [state from to]
  (let [card (get-card state from)]
    (-> state
        (dissoc :next-state)
        ((fn [state]
           (update state :history conj state)))
        (update-in [(:block from) (:pile from)]
                   (fn [pile]
                     (vec (drop-last 1 pile))))
        (update-in [(:block to) (:pile to)]
                   conj card))))

(defn- animation-move-card
  [state from to]
  (-> state
      (move-card from to)
      (assoc :intermediate true)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; here ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- get-moves
  [state from to draggable-pile]
  (let [buffer-freecells (map (fn [[n _]] {:block :freecells
                                           :pile n})
                              (filter (fn [[_ pile]] (empty? pile))
                                      (map vector (range) (:freecells state))))
        buffer-tableau (map (fn [[n _]] {:block :tableau
                                         :pile n})
                            (filter (fn [[_ pile]] (empty? pile))
                                    (map vector (range) (:tableau state))))]
    (loop [new-state state]
      (let [from-card (get-card new-state from)
            to-card (get-card new-state to)]
        (cond
          ;; all cards have been moved
          (= to-card (last draggable-pile))
          (assoc new-state :intermediate false)
          ;; move to target pile
          (and (= from-card (first draggable-pile))
               (or (not to-card)
                   (in-tableau-order? (get-card new-state from) to-card)))
          (recur (animation-move-card new-state from to))
          ;; move to free buffer
          (and (some #(= % from-card) draggable-pile)
               (some empty? (:freecells new-state)))
          (let [[pile-position _] (first (filter (fn [[_ pile]] (empty? pile)) (map vector (range) (:freecells new-state))))
                to {:block :freecells
                    :pile pile-position}]
            (recur (animation-move-card new-state from to)))
          ;; move from buffer to target pile
          (some (fn [card]
                  (and card
                       (in-tableau-order? card to-card)))
                (map (partial get-card new-state) buffer-freecells))
          (let [from (first (filter (fn [card-info]
                                      (if-let [card (get-card new-state card-info)]
                                        (in-tableau-order? card to-card)))
                                    buffer-freecells))]
            (recur (animation-move-card new-state from to)))
          ;; move is inposibble
          :else state)))))

(defn- get-block-changes
  [from-state to-state block]
  (map (fn [[[n pile] _]]
         {:block block
          :pile n
          :card (dec (count pile))})
       (filter (fn [[[_ before] [_ after]]]
                 (not= (count before) (count after)))
               (map vector
                    (map vector (range) (block from-state))
                    (map vector (range) (block to-state))))))

(defn- get-changes
  [from-state to-state]
  (let [get-block-changes (partial get-block-changes from-state to-state)
        changes (into (into (get-block-changes :freecells)
                            (get-block-changes :foundations))
                      (get-block-changes :tableau))]
    {:from (first (filter #(> (count (get-pile from-state %))
                              (count (get-pile to-state %)))
                          changes))
     :to (first (filter #(< (count (get-pile from-state %))
                            (count (get-pile to-state %)))
                        changes))}))

(defn- animate-move-pile!
  [from-state force]
  (if-let [next-state (:next-state from-state)]
    (let [changes (get-changes from-state next-state)
          from (:from changes)
          to (:to changes)
          card (get-card from-state from)
          on-animation-stop (fn [propagate]
                              (reset! state next-state)
                              (animate-move-pile! next-state (not propagate)))]
      (if force
        (on-animation-stop false)
        ((:animate-fn from-state) from to card on-animation-stop)))))

(defn move-pile!
  [from-block from-position draggable-pile to-block to-position to-pile]
  (let [from {:block from-block
              :pile from-position}
        to {:block to-block
            :pile to-position}]
    (if (= to-block :tableau)
      (let [current-state @state
            end-state (get-moves current-state from to draggable-pile)
            from-state end-state
            from-state (loop [step-state end-state
                              next-state nil]
                         (let [new-state (if next-state
                                           (assoc step-state :next-state next-state)
                                           step-state)]
                           (if (or (not step-state)
                                   (= step-state current-state))
                             new-state
                             (recur (first (:history new-state)) new-state))))]
        (animate-move-pile! from-state false))
      (when (can-move-to? @state draggable-pile to-block (last to-pile))
        (swap! state move-card from to)))))

;; (defn move-pile!
;;   [from-block from-position draggable-pile to-block to-position to-pile]
;;   (when (can-move-to? @state draggable-pile to-block (last to-pile))
;;     (let [cards-count (count draggable-pile)]
;;       (swap! state (fn [state]
;;                      (dissoc state :next-state)))
;;       (swap! state (fn [state]
;;                      (update state :history conj state))) 
;;       (swap! state (fn [state]
;;                      (update-in state [from-block from-position]
;;                                 (fn [pile]
;;                                   (vec (drop-last cards-count pile))))))
;;       (swap! state #(update-in % [to-block to-position]
;;                                (fn [pile]
;;                                  (into pile draggable-pile)))))))

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
                                            (next-fn false)))]
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
