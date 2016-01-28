(ns bubbles.bubble
  (:require
   [bubbles.utils :as utils]
   [phzr.animation-manager :as animation-manager]
   [phzr.core :as pcore]
   [phzr.game-object-factory :as object-factory]
   [phzr.group :as group]
   [phzr.signal :as signal]))

(def bubble-size 64)
(def bubble-create-offset-y 100)

(defn bubble-up [bubble]
  (set! (.-direction bubble) :up)
  (utils/set-attr! bubble [:body :velocity :y] -100))

(defn bubble-stop [bubble]
  (utils/set-attr! bubble [:body :velocity :y] 0))

(defn destroy [bubble]
  (.destroy bubble))

(defn bubble-tapped [bubble]
  (destroy bubble))

(defn bubble-update [bubble on-vanish]
  (when (< (.-y bubble) 0)
    (destroy bubble)
    (on-vanish)))

(defn add-bubble [game bubbles-group x y tap-listener]
  (let [bubble (group/create bubbles-group x y "bubble")]
    (utils/set-attr! bubble [:input-enabled] true)
    (signal/add (get-in bubble [:events :on-input-down])
                (fn [bubble]
                  (bubble-tapped bubble)
                  (tap-listener bubble))
                bubble)
    bubble))

(defn add-random-bubble [game bubble-group tap-listener]
  (add-bubble game bubble-group
              (rand-int (- (.-width game) bubble-size))
              (+ (rand-int (- (.-height game) bubble-size bubble-create-offset-y))
                 bubble-create-offset-y)
              tap-listener))
