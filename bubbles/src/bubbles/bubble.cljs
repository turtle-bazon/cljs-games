(ns bubbles.bubble
  (:require
   [phzr.animation-manager :as animation-manager]
   [phzr.core :as pcore]
   [phzr.game-object-factory :as object-factory]
   [phzr.group :as group]
   [phzr.point :as point]
   [phzr.rectangle :as rect]
   [phzr.signal :as signal]
   [phzr.sound :as sound]
   [phzr.sprite :as sprite]
   [phzr.timer :as timer]
   [bubbles.dimensions :as dimens]
   [bubbles.utils :as utils :refer [log]]))

(def playfield-offset-y 55)
(def bubble-size (:width dimens/bubble))
(def bubble-create-bottom-offset-y 650)
(def bubble-life-time 6000)
(def initial-state {:small-interval 100
                    :big-interval 1000
                    :big-interval-factor 1
                    :min-velocity 120
                    :max-velocity 350
                    :min-velocity-step 3
                    :max-velocity-step 5
                    :min-wave-size 3
                    :max-wave-size 4
                    :current-wave-size 4
                    :wave-step 0
                    :wave-number 1
                    :wave-bubble-number 1})

(defn bubble-stop [bubble]
  (utils/set-attr! bubble [:body :velocity :y] 0))

(defn destroy [bubble]
  (sprite/destroy bubble))

(defn square [x]
  (* x x))

(defn interval-rand [min max]
  (+ (rand-int (+ max (- min) 1)) min))

(defn bubble-tapped [game bubble bubbles event]
  (let [radius (/ (:width bubble) 2)
        dx (- (:x event) (+ (:x bubble) radius))
        dy (- (:y event) (+ (:y bubble) radius))
        squared-distance (+ (square dx) (square dy))
        distance (- (+ (square dx) (square dy))
                    (square (/ bubble-size 2)))]
    (when (<= squared-distance
              (square radius))
      (sound/play (:vanish-sound bubbles))
      (destroy bubble)
      true)))

(defn vanish [game bubble bubbles]
  (destroy bubble)
  (sound/play (.-lifeloss-sound game))
  ((:on-vanish bubbles)))

(defn update-bubble [game bubble bubbles]
  (if (< (:y bubble) playfield-offset-y)
    (vanish game bubble bubbles)
    (let [elapsed (get-in game [:time :physics-elapsed-ms])
          left-time (- (.-leftTime bubble) elapsed)]
      (if (<= left-time 0)
        (vanish game bubble bubbles)
        (let [scale (* (.-cc game) (/ left-time bubble-life-time))
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
    (doseq [bubble (get-in bubbles [:group :children])]
      (update-bubble game bubble bubbles))
    (do (sound/play (.-gameover-sound game))
        (doseq [bubble (get-in bubbles [:group :children])]
          (destroy bubble)))))

(defn add-bubble [game bubbles x y velocity]
  (let [bubble (group/create (:group bubbles) x y "bubble")
        bubble-index (int (+ 0.5 (rand 8)))]
    (utils/set-attr! bubble [:body :velocity :y] (- velocity))
    (utils/set-attr! bubble [:frame] bubble-index)
    (set! (.-leftTime bubble) bubble-life-time)
    (sound/play (:create-sound bubbles))
    bubble))

(defn add-random-bubble [state game bubbles]
  (let [correction-coefficient (.-cc game)
        {:keys [min-velocity max-velocity]} state
        bubble (add-bubble game bubbles
                           (rand-int (- (:width game) (* correction-coefficient bubble-size)))
                           (interval-rand bubble-create-bottom-offset-y
                                          (- (:height game)
                                             (* correction-coefficient bubble-size)))
                           (interval-rand min-velocity max-velocity))]
    (utils/set-attr! bubble [:scale :x] correction-coefficient)
    (utils/set-attr! bubble [:scale :y] correction-coefficient)))

(defn next-state [state]
  (let [{:keys [small-interval
                big-interval
                big-interval-factor
                min-velocity
                max-velocity
                min-velocity-step
                max-velocity-step
                min-wave-size
                max-wave-size
                current-wave-size
                wave-bubble-number
                wave-step]} state]
    (cond
      (= (:wave-bubble-number state) 1)
      (-> state
          (update :wave-bubble-number inc)
          (assoc :next-interval small-interval))
      (< wave-bubble-number current-wave-size)
      (update state :wave-bubble-number inc)
      (= wave-bubble-number current-wave-size)
      (-> state
          (update :wave-number inc)
          (assoc :current-wave-size
                 (interval-rand min-wave-size max-wave-size))
          (assoc :wave-bubble-number 1)
          (update :min-velocity #(+ % min-velocity-step))
          (update :max-velocity #(+ % max-velocity-step))
          (assoc :next-interval big-interval)
          (update :big-interval #(* % big-interval-factor)))
      (= (:wave-bubble-number state) current-wave-size)
      (assoc state :next-interval big-interval))))

(defn generate-bubble [game bubbles state]
  (when (not ((:is-game-over-fn bubbles)))
    (let [bubble (add-random-bubble state game bubbles)
          new-state (next-state state)]
      (timer/add (get-in game [:time :events])
                 (:next-interval new-state)
                 (fn []
                   (generate-bubble game bubbles new-state))
                 nil nil))))

(defn handle-click [game event playfield-rect bubbles on-hit on-miss]
  (when (not (some some?
                   (for [bubble (reverse (get-in bubbles [:group :children]))]
                     (when (bubble-tapped game bubble bubbles event)
                       (on-hit)))))
    (when (rect/contains-point- playfield-rect (:position event))
      (sound/play (.-lifeloss-sound game))
      (on-miss))))

(defn init-bubbles [game on-hit on-miss on-vanish is-game-over-fn]
  (let [group (object-factory/physics-group (:add game))
        playfield-rect (rect/->Rectangle 0 playfield-offset-y
                                         (:width game)
                                         (- (:height game) playfield-offset-y))
        bubbles {:group group
                 :on-vanish on-vanish
                 :vanish-sound (object-factory/audio (:add game) "bubble-vanish-sound")
                 :create-sound (object-factory/audio (:add game) "bubble-create-sound")
                 :is-game-over-fn is-game-over-fn}]
    (set! (.-lifeloss-sound game) (object-factory/audio (:add game) "lifeloss"))
    (set! (.-gameover-sound game) (object-factory/audio (:add game) "gameover"))
    (signal/add (get-in game [:input :on-down])
                (fn [event]
                  (handle-click game event playfield-rect bubbles on-hit on-miss)))
    bubbles))

(defn start-bubbles [game bubbles]
  (generate-bubble game bubbles initial-state))

(defn add-background [game]
  (let [game-width (inc (:width game))
        game-height (inc (:height game))
        image-width (:width dimens/background-landscape)
        image-height (:height dimens/background-landscape)
        background (object-factory/image (:add game) 0 0 "background")
        scale-width (/ game-width image-width)
        scale-height (/ game-height image-height)
        result-scale (max scale-width scale-height)]
    (point/set-to (:scale background) result-scale result-scale)
    background))
