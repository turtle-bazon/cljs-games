(ns bubbles.bubble
  (:require
   [phzr.animation-manager :as animation-manager]
   [phzr.core :as pcore]
   [phzr.game-object-factory :as object-factory]
   [phzr.group :as group]
   [phzr.signal :as signal]
   [phzr.sprite :as sprite]
   [bubbles.utils :as utils :refer [log]]))

(def bubble-size 64)
(def bubble-create-offset-y 100)
(def bubble-velocity 30)
(def bubble-life-time 10000)

(defn bubble-up [bubble]
  (utils/set-attr! bubble [:body :velocity :y] (- bubble-velocity)))

(defn bubble-stop [bubble]
  (utils/set-attr! bubble [:body :velocity :y] 0))

(defn destroy [bubble]
  (sprite/destroy bubble))

(defn square [x]
  (* x x))

(defn bubble-tapped [bubble event]
  (let [radius (/ (:width bubble) 2)
        dx (- (:x event) (+ (:x bubble) radius))
        dy (- (:y event) (+ (:y bubble) radius))
        squared-distance (+ (square dx) (square dy))
        distance (- (+ (square dx) (square dy))
                    (square (/ bubble-size 2)))]
    (when (<= squared-distance
              (square radius))
      (destroy bubble)
      true)))

(defn vanish [bubble on-vanish]
  (destroy bubble)
  (on-vanish))

(defn bubble-update [game bubble on-vanish]
  (if (< (:y bubble) 0)
    (vanish bubble on-vanish)
    (let [elapsed (get-in game [:time :physics-elapsed-ms])
          left-time (- (.-leftTime bubble) elapsed)]
      (if (<= left-time 0)
        (vanish bubble on-vanish)
        (let [scale (/ left-time bubble-life-time)
              cur-size (:width bubble)
              new-size (* bubble-size scale)
              offset (/ (- cur-size new-size) 2)]
          (set! (.-leftTime bubble) left-time)
          (utils/set-attr! bubble [:scale :x] scale)
          (utils/set-attr! bubble [:scale :y] scale)
          (utils/set-attr! bubble [:x] (+ (:x bubble) offset))
          (utils/set-attr! bubble [:y] (+ (:y bubble) offset)))))))

(defn add-bubble [game bubbles-group x y tap-listener]
  (let [bubble (group/create bubbles-group x y "bubble")]
    (utils/set-attr! bubble [:input-enabled] true)
    (signal/add (get-in bubble [:events :on-input-down])
                (fn [bubble event]
                  (let [hit (bubble-tapped bubble event)]
                    (tap-listener bubble hit)))
                bubble)
    (set! (.-leftTime bubble) bubble-life-time)
    bubble))

(defn add-random-bubble [game bubble-group tap-listener]
  (add-bubble game bubble-group
              (rand-int (- (:width game) bubble-size))
              (+ (rand-int (- (:height game) bubble-size bubble-create-offset-y))
                 bubble-create-offset-y)
              tap-listener))

(defn add-background [game tap-listener]
  (let [background (object-factory/tile-sprite (:add game)
                                               0 0 (:width game) (:height game)
                                               "background")]
    (utils/set-attr! background [:input-enabled] true)
    (signal/add (get-in background [:events :on-input-down]) tap-listener)))
