(ns runner.bubble
  (:require
   [phzr.animation-manager :as animation-manager]
   [phzr.core :as pcore]
   [phzr.game-object-factory :as object-factory]
   [phzr.group :as group]
   [phzr.keyboard :as keyboard]
   [phzr.rectangle :as rect]
   [phzr.signal :as signal]
   [phzr.sound :as sound]
   [phzr.sprite :as sprite]
   [phzr.timer :as timer]
   [runner.dimensions :as dimens]
   [runner.utils :as utils :refer [log]]))

(def playfield-offset-x 0)
(def top 100)
(def bottom 500)
(def bubble-size (:width dimens/bubble))
(def player-x 50)
(def player-y (- bottom bubble-size))
(def score-per-second 10)
(def initial-state {:score 0
                    :min-interval 500
                    :max-interval 1500
                    :velocity-x 400
                    :velocity-y 500
                    :acceleration-x 200})
(def state-atom (atom initial-state))

(defn bubble-stop [bubble]
  (utils/set-attr! bubble [:body :velocity :y] 0))

(defn destroy [bubble]
  (sprite/destroy bubble))

(defn square [x]
  (* x x))

(defn interval-rand [min max]
  (+ (rand-int (- max min)) min))

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

(defn vanish [bubble bubbles hit?]
  (destroy bubble)
  (when hit?
    ((:on-vanish bubbles))))

(defn update-bubble [game bubble bubbles dx velocity-y]
  (if (< (:x bubble) playfield-offset-x)
    (vanish bubble bubbles false)
    (if (and (< player-x (:x bubble) (+ player-x bubble-size))
             (< player-y (+ (:y bubble) bubble-size)))
      (vanish bubble bubbles true)
      (let [elapsed (get-in game [:time :physics-elapsed-ms])
            left-time (- (.-leftTime bubble) elapsed)]
        (utils/set-attr! bubble [:body :position :x]
                         (+ (get-in bubble [:body :position :x]) dx))
        (if (< (:y bubble) top)
          (utils/set-attr! bubble [:body :velocity :y] velocity-y))
        (if (< bottom (+ (:y bubble) bubble-size))
          (utils/set-attr! bubble [:body :velocity :y] (- velocity-y)))))))

(defn clear-score! [bubbles]
  (swap! state-atom assoc :score 0)
  ((:set-score bubbles) 0))

(defn update-score! [game bubbles]
  (let [score-delta (* (get-in game [:time :physics-elapsed])
                                            score-per-second)
        state (swap! state-atom update :score #(+ % score-delta))]
    ((:set-score bubbles) (int (:score state)))))

(defn update-bubbles [game background bubbles]
  (if (not ((:is-game-over-fn bubbles)))
    (let [{:keys [velocity-x
                  velocity-y
                  acceleration-x]} @state-atom
          cursors (:cursors bubbles)
          delta-vx (if (get-in cursors [:left :is-down])
                     (- acceleration-x)
                     (if (get-in cursors [:right :is-down])
                       acceleration-x
                       0))
          dx (- (* (+ velocity-x delta-vx)
                   (get-in game [:time :physics-elapsed])))]
      (update-score! game bubbles)
      (utils/set-attr! background [:tile-position :x]
                       (+ (get-in background [:tile-position :x])
                          dx))
      (doall (map (fn [bubble]
                    (update-bubble game bubble bubbles dx velocity-y))
                  (get-in bubbles [:group :children]))))
    (do
      (clear-score! bubbles)
      (doall (map (fn [bubble]
                   (destroy bubble))
                 (get-in bubbles [:group :children]))))))

(defn add-bubble [game bubbles x y velocity-x velocity-y]
  (let [bubble (group/create (:group bubbles) x y "bubble")
        bubble-index (int (+ 0.5 (rand 8)))]
    (utils/set-attr! bubble [:body :velocity :y] (- velocity-y))
    (utils/set-attr! bubble [:frame] bubble-index)
    (sound/play (:create-sound bubbles))
    bubble))

(defn add-random-bubble [state game bubbles]
  (let [correction-coefficient (.-cc game)
        {:keys [velocity-x velocity-y]} state
        bubble (add-bubble game bubbles
                           (:width game)
                           (interval-rand top bottom)
                           velocity-x
                           velocity-y)]
    (utils/set-attr! bubble [:scale :x] correction-coefficient)
    (utils/set-attr! bubble [:scale :y] correction-coefficient)))

(defn generate-bubble [game bubbles]
  (when (not ((:is-game-over-fn bubbles)))
    (let [state @state-atom
          bubble (add-random-bubble state game bubbles)
          next-interval (interval-rand (:min-interval state) (:max-interval state))]
      (timer/add (get-in game [:time :events])
                 next-interval
                 (fn []
                   (generate-bubble game bubbles))
                 nil nil))))

(defn init-bubbles [game set-score on-miss on-vanish is-game-over-fn]
  (let [group (object-factory/physics-group (:add game))
        cursors (keyboard/create-cursor-keys (get-in game [:input :keyboard]))
        player (object-factory/sprite (:add game) player-x player-y "bubble")
        bubbles {:group group
                 :set-score set-score
                 :on-vanish on-vanish
                 :vanish-sound (object-factory/audio (:add game) "bubble-vanish-sound")
                 :create-sound (object-factory/audio (:add game) "bubble-create-sound")
                 :is-game-over-fn is-game-over-fn
                 :cursors cursors
                 :player player}]
    bubbles))

(defn start-bubbles [game bubbles]
  (generate-bubble game bubbles))

(defn add-background [game]
  (let [image-width (:width dimens/background)
        image-height (:height dimens/background)
        background-offset-x (- (/ (- image-width (:width game)) 2))
        background (object-factory/tile-sprite (:add game)
                                               0 0
                                               (:width game) (:height game)
                                               "background")]
    background))
