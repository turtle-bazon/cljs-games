(ns turtle-attention.play-state
  (:require
   [turtle-attention.characters :as characters]
   [turtle-attention.utils :as utils]
   [phzr.animation-manager :as animation-manager]
   [phzr.game-object-factory :as object-factory]
   [phzr.group :as group]
   [phzr.physics.arcade :as arcade-physics]))

(defonce world-state! (atom {}))

(defn turtle-box-collide [turtle box]
  (let [touch-state (:touching (:body turtle))]
    (cond
      (.-left touch-state) (characters/turtle-right turtle)
      (.-right touch-state) (characters/turtle-left turtle))))

(defn turtle-tapped [turtle]
  (if (= (get-in turtle [:body :velocity :x]) 0)
    (case (.-direction turtle)
      :right (characters/turtle-right turtle)
      :left (characters/turtle-left turtle))
    (characters/turtle-stop turtle)))

(defn state-create [game]
  (let [turtles-group (object-factory/physics-group (:add game))
        boxes-group (object-factory/physics-group (:add game))
        box1 (group/create boxes-group 0 0 "box")
        box2 (group/create boxes-group 736 0 "box")
        turtle (characters/add-turtle game turtles-group 64 0 (fn [turtle] (turtle-tapped turtle)))]
    (utils/set-attr! box1 [:body :immovable] true)
    (utils/set-attr! box2 [:body :immovable] true)
    (set! (.-direction turtle) :right)
    (characters/turtle-right turtle)
    (swap! world-state!
           (fn [world-state]
             (-> world-state
                 (assoc-in [:turtles-group] turtles-group)
                 (assoc-in [:boxes-group] boxes-group))))))

(defn state-update [game]
  (let [{:keys [turtles-group boxes-group]} @world-state!]
    (arcade-physics/collide (:arcade (:physics game))
                            turtles-group boxes-group turtle-box-collide)))

(def state-obj
  {:create state-create
   :update (fn [game] (state-update game))})
