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
   [bubbles.utils :as utils :refer [log]]))

(def playfield-rect (rect/->Rectangle 0 55 800 425))
(def bubble-size 96)
(def bubble-create-offset-y 150)
(def bubble-velocity 30)
(def bubble-life-time 10000)
(def initial-create-interval 800)
(def bubble-create-interval-min 350)
(def bubble-create-interval-factor 0.98)

(defn bubble-stop [bubble]
  (utils/set-attr! bubble [:body :velocity :y] 0))

(defn destroy [bubble]
  (sprite/destroy bubble))

(defn square [x]
  (* x x))

(defn bubble-tapped [bubble bubbles event]
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
  (let [bubble (group/create (:group bubbles) x y "bubble")]
    (utils/set-attr! bubble [:input-enabled] true)
    (signal/add (get-in bubble [:events :on-input-down])
                (fn [bubble event]
                  (((if (bubble-tapped bubble bubbles event)
                      :on-hit
                      :on-miss) bubbles)))
                bubble)
    (utils/set-attr! bubble [:body :velocity :y] (- bubble-velocity))
    (set! (.-leftTime bubble) bubble-life-time)
    bubble))

(defn add-random-bubble [game bubbles]
  (add-bubble game bubbles
              (rand-int (- (:width game) bubble-size))
              (+ (rand-int (- (:height game) bubble-size bubble-create-offset-y))
                 bubble-create-offset-y)))

(defn next-interval [interval]
  (+ (* (- interval bubble-create-interval-min)
        bubble-create-interval-factor)
     bubble-create-interval-min))

(defn generate-bubble [game bubbles create-interval]
  (when (not ((:is-game-over-fn bubbles)))
    (let [bubble (add-random-bubble game bubbles)
          new-create-interval (next-interval create-interval)]
      (timer/add (get-in game [:time :events])
                 create-interval
                 (fn []
                   (generate-bubble game bubbles new-create-interval))
                 nil nil))))

(defn init-bubbles [game on-hit on-miss on-vanish is-game-over-fn]
  (let [vanish-sound (object-factory/audio (:add game) "bubble-vanish-sound")
        bubbles-group (object-factory/physics-group (:add game))]
    {:group bubbles-group
     :vanish-sound vanish-sound
     :on-hit on-hit
     :on-miss on-miss
     :on-vanish on-vanish
     :is-game-over-fn is-game-over-fn}))

(defn start-bubbles [game bubbles]
  (generate-bubble game bubbles initial-create-interval))

(defn add-background [game tap-listener]
  (let [background (object-factory/tile-sprite (:add game)
                                               0 0 (:width game) (:height game)
                                               "background")]
    (utils/set-attr! background [:input-enabled] true)
    (signal/add (get-in background [:events :on-input-down])
                (fn [background event]
                  (when (rect/contains-point- playfield-rect (:position event))
                    (tap-listener))))
    background))
