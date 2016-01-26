(ns bubbles.play-state
  (:require
   [phzr.animation-manager :as animation-manager]
   [phzr.game-object-factory :as object-factory]
   [phzr.group :as group]
   [phzr.physics.arcade :as arcade-physics]
   [bubbles.characters :as characters]
   [bubbles.utils :as utils]))

(defonce state-atom (atom {}))

(defonce score-atom (atom 0))

(defn bubble-right [bubble]
  (set! (.-direction bubble) :up)
  (utils/set-attr! bubble [:body :velocity :y] 5))

(defn bubble-stop [bubble]
  (utils/set-attr! bubble [:body :velocity :y] 0))

(defn bubble-tapped [bubble]
  (bubble-stop bubble)
  (swap! score-atom inc))

(defn state-create [game]
  (let [bubbles-group (object-factory/physics-group (:add game))
        bubble (characters/add-bubble game bubbles-group 64 0
                                      (fn [bubble] (bubble-tapped bubble)))]
    (set! (.-direction bubble) :up)
    (swap! state-atom
           (fn [state]
             (-> state
                 (assoc-in [:bubbles-group] bubbles-group))))))

(defn state-update [game]
  (let [{:keys [bubbles-group]} @state-atom]
    ;; (arcade-physics/collide (:arcade (:physics game))
    ;;                         turtles-group boxes-group turtle-box-collide)
    ))

(def state-obj
  {:create state-create
   :update (fn [game] (state-update game))})
