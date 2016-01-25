(ns turtle-attention.play-state
  (:require
   [turtle-attention.characters :as characters]
   [turtle-attention.utils :as utils]
   [phzr.animation-manager :as animation-manager]
   [phzr.game-object-factory :as object-factory]
   [phzr.group :as group]
   [phzr.physics.arcade :as arcade-physics]))

(defonce *play-atom* (atom {}))

(defn turtle-left [turtle]
  (utils/set-attr! turtle [:body :velocity :x] -25)
  (animation-manager/play (:animations turtle) "left"))

(defn turtle-right [turtle]
  (utils/set-attr! turtle [:body :velocity :x] 25)
  (animation-manager/play (:animations turtle) "right"))

(defn turtle-box-collide [turtle box]
  (let [touch-state (:touching (:body turtle))]
    (cond
      (.-left touch-state) (turtle-right turtle)
      (.-right touch-state) (turtle-left turtle))))

(defn state-create [game]
  (let [turtles-group (object-factory/physics-group (:add game))
        boxes-group (object-factory/physics-group (:add game))
        box1 (group/create boxes-group 0 0 "box")
        box2 (group/create boxes-group 736 0 "box")
        turtle (characters/add-turtle game turtles-group 64 0)]
    (utils/set-attr! box1 [:body :immovable] true)
    (utils/set-attr! box2 [:body :immovable] true)
    (turtle-right turtle)
    (swap! *play-atom*
           (fn [play-atom]
             (-> play-atom
                 (assoc-in [:turtles] turtles-group)
                 (assoc-in [:boxes] boxes-group))))))

(defn state-update [game]
  (let [{:keys [turtles boxes]} @*play-atom*]
    (arcade-physics/collide (:arcade (:physics game)) turtles boxes turtle-box-collide)))

(def state-obj
  {:create state-create
   :update (fn [game] (state-update game))})
