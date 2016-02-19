(ns bubbles.bubble
  (:require
   [phzr.animation-manager :as animation-manager]
   [phzr.core :as pcore]
   [phzr.game-object-factory :as object-factory]
   [phzr.group :as group]
   [phzr.rectangle :as rect]
   [phzr.signal :as signal]
   [phzr.sound :as sound]
   [phzr.sprite :as sprite]
   [phzr.timer :as timer]
   [bubbles.sound-wrapper :as sw]
   [bubbles.utils :as utils :refer [log]]))

(def playfield-rect (rect/->Rectangle 0 55 800 425))
(def bubble-size 96)
(def bubble-create-offset-y 150)
(def bubble-velocity 30)
(def bubble-life-time 10000)
(def initial-state {:small-interval 100
                    :big-interval 3000
                    :big-interval-factor 1.05
                    :wave-size 5
                    :wave-step 1
                    :wave-number 1
                    :wave-bubble-number 1})

(defn bubble-stop [bubble]
  (utils/set-attr! bubble [:body :velocity :y] 0))

(defn destroy [bubble]
  (sprite/destroy bubble))

(defn square [x]
  (* x x))

(defn bubble-tapped [game bubble bubbles event]
  (let [radius (/ (:width bubble) 2)
        dx (- (:x event) (+ (:x bubble) radius))
        dy (- (:y event) (+ (:y bubble) radius))
        squared-distance (+ (square dx) (square dy))
        distance (- (+ (square dx) (square dy))
                    (square (/ bubble-size 2)))]
    (when (<= squared-distance
              (square radius))
      (sw/play (sw/get-sound game "bubble-vanish-sound"))
      (destroy bubble)
      true)))

(defn vanish [bubble bubbles]
  (destroy bubble)
  ((:on-vanish bubbles)))

(defn update-bubble [game bubble bubbles]
  (if (< (:y bubble) (:y playfield-rect))
    (vanish bubble bubbles)
    (let [elapsed (get-in game [:time :physics-elapsed-ms])
          left-time (- (.-leftTime bubble) elapsed)]
      (if (<= left-time 0)
        (vanish bubble bubbles)
        (let [scale (/ left-time bubble-life-time)
              cur-size (:width bubble)
              new-size (* bubble-size scale)
              offset (/ (- cur-size new-size) 2)]
          (set! (.-leftTime bubble) left-time)
          (utils/set-attr! bubble [:scale :x] scale)
          (utils/set-attr! bubble [:scale :y] scale)
          (utils/set-attr! bubble [:x] (+ (:x bubble) offset))
          (utils/set-attr! bubble [:y] (+ (:y bubble) offset)))))))

(defn update-bubbles [game bubbles]
  (if (not ((:is-game-over-fn bubbles)))
    (doall (map (fn [bubble]
                  (update-bubble game bubble bubbles))
                (get-in bubbles [:group :children])))
    (doall (map (fn [bubble]
                  (destroy bubble))
                (get-in bubbles [:group :children])))))

(defn add-bubble [game bubbles x y]
  (let [bubble (group/create (:group bubbles) x y "bubble")
        bubble-index (int (+ 0.5 (rand 8)))]
    (utils/set-attr! bubble [:body :velocity :y] (- bubble-velocity))
    (utils/set-attr! bubble [:frame] bubble-index)
    (set! (.-leftTime bubble) bubble-life-time)
    bubble))

(defn add-random-bubble [game bubbles]
  (add-bubble game bubbles
              (rand-int (- (:width game) bubble-size))
              (+ (rand-int (- (:height game) bubble-size bubble-create-offset-y))
                 bubble-create-offset-y)))

(defn next-state [state]
  (let [{:keys [small-interval
                big-interval
                big-interval-factor
                wave-size
                wave-bubble-number
                wave-step]} state]
    (cond
      (= (:wave-bubble-number state) 1)
      (-> state
          (update :wave-bubble-number inc)
          (assoc :next-interval small-interval))
      (< wave-bubble-number wave-size)
      (update state :wave-bubble-number inc)
      (= wave-bubble-number wave-size)
      (-> state
          (update :wave-number inc)
          (update :wave-size #(+ % wave-step))
          (assoc :wave-bubble-number 1)
          (assoc :next-interval big-interval)
          (update :big-interval #(* % big-interval-factor)))
      (= (:wave-bubble-number state) wave-size)
      (assoc state :next-interval big-interval))))

(defn generate-bubble [game bubbles state]
  (when (not ((:is-game-over-fn bubbles)))
    (let [bubble (add-random-bubble game bubbles)
          new-state (next-state state)]
      (timer/add (get-in game [:time :events])
                 (:next-interval state)
                 (fn []
                   (generate-bubble game bubbles new-state))
                 nil nil))))

(defn handle-click [game event bubbles on-hit on-miss]
  (when (not (some some?
                   (for [bubble (reverse (:children bubbles))]
                     (when (bubble-tapped game bubble bubbles event)
                       (on-hit)))))
    (when (rect/contains-point- playfield-rect (:position event))
      (on-miss))))

(defn init-bubbles [game on-hit on-miss on-vanish is-game-over-fn]
  (let [bubbles-group (object-factory/physics-group (:add game))
        bubbles bubbles-group]
    (signal/add (get-in game [:input :on-down])
                (fn [event]
                  (handle-click game event bubbles on-hit on-miss)))
    {:group bubbles-group
     :on-vanish on-vanish
     :is-game-over-fn is-game-over-fn}))

(defn start-bubbles [game bubbles]
  (generate-bubble game bubbles initial-state))

(defn add-background [game]
  (let [image-width 569
        image-height 854
        background-offset-x (- (/ (- image-width (:width game)) 2))
        background (object-factory/tile-sprite (:add game)
                                               background-offset-x 0
                                               image-width image-height
                                               ;; (:width game) (:height game)
                                               "background")]
    (object-factory/tile-sprite (:add game)
                                0 50 (:width game) 5
                                "separator")
    background))
