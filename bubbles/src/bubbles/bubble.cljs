(ns bubbles.bubble
  (:require
   [bubbles.utils :as utils :refer [log]]
   [phzr.animation-manager :as animation-manager]
   [phzr.core :as pcore]
   [phzr.game-object-factory :as object-factory]
   [phzr.group :as group]
   [phzr.signal :as signal]))

(def bubble-size 64)
(def bubble-create-offset-y 100)
(def bubble-velocity 30)
(def bubble-life-time 10000)

(defn bubble-up [bubble]
  (set! (.-direction bubble) :up)
  (utils/set-attr! bubble [:body :velocity :y] (- bubble-velocity)))

(defn bubble-stop [bubble]
  (utils/set-attr! bubble [:body :velocity :y] 0))

(defn destroy [bubble]
  (.destroy bubble))

(defn square [x]
  (* x x))

(defn bubble-tapped [bubble event]
  (let [radius (/ (.-width bubble) 2)
        dx (- (.-x event) (+ (.-x bubble) radius))
        dy (- (.-y event) (+ (.-y bubble) radius))
        squared-distance (+ (square dx) (square dy))
        distance (- (+ (square dx) (square dy))
                    (square (/ bubble-size 2)))]
    (when (<= squared-distance
              (square radius))
      (destroy bubble))))

(defn vanish [bubble on-vanish]
  (destroy bubble)
  (on-vanish))

(defn bubble-update [game bubble on-vanish]
  (if (< (.-y bubble) 0)
    (vanish bubble on-vanish)
    (let [elapsed (.. game -time -elapsed)
          left-time (- (.-leftTime bubble) elapsed)]
      (if (<= left-time 0)
        (vanish bubble on-vanish)
        (let [scale (/ left-time bubble-life-time)
              cur-size (.-width bubble)
              new-size (* bubble-size scale)
              offset (/ (- cur-size new-size) 2)]
          (set! (.-leftTime bubble) left-time)
          (.setTo (.-scale bubble) scale scale)
          (set! (.-x bubble) (+ (.-x bubble) offset))
          (set! (.-y bubble) (+ (.-y bubble) offset)))))))

(defn add-bubble [game bubbles-group x y tap-listener]
  (let [bubble (group/create bubbles-group x y "bubble")]
    (utils/set-attr! bubble [:input-enabled] true)
    (signal/add (get-in bubble [:events :on-input-down])
                (fn [bubble event]
                  (bubble-tapped bubble event)
                  (tap-listener bubble))
                bubble)
    (set! (.-leftTime bubble) bubble-life-time)
    bubble))

(defn add-random-bubble [game bubble-group tap-listener]
  (add-bubble game bubble-group
              (rand-int (- (.-width game) bubble-size))
              (+ (rand-int (- (.-height game) bubble-size bubble-create-offset-y))
                 bubble-create-offset-y)
              tap-listener))
