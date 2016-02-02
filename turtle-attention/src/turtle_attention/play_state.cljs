(ns turtle-attention.play-state
  (:require
   [turtle-attention.characters :as characters]
   [turtle-attention.utils :as utils]
   [phzr.animation-manager :as animation-manager]
   [phzr.game-object-factory :as object-factory]
   [phzr.group :as group]
   [phzr.physics.arcade :as arcade-physics]
   [phzr.sprite :as sprite]))

(def vertical-offset 10)

(def vertical-margin 5)

(def vertical-size 64)

(defonce world-state! (atom {}))

(defn turtle-meet-berrybox [turtle box]
  (let [touch-state (:touching (:body turtle))
        current-berries (.-berryCount box)
        eaten-berries (dec current-berries)]
    (set! (.-berryCount box) (dec current-berries))
    (when (>= eaten-berries 0)
      (utils/set-attr! box [:frame] eaten-berries))
    (cond
      (.-left touch-state) (characters/turtle-right turtle)
      (.-right touch-state) (characters/turtle-left turtle))))

(defn turtle-meet-carnivorous [turtle carnivorous]
  (sprite/destroy turtle))

(defn turtle-tapped [turtle]
  (if (= (get-in turtle [:body :velocity :x]) 0)
    (characters/turtle-continue-move turtle)
    (characters/turtle-stop turtle)))

(defn create-turtle-path [game turtles-group boxes-group number]
  (let [path-y (+ (* number (+ vertical-size vertical-margin)) vertical-offset)
        box1 (group/create boxes-group 0 path-y "berrybox")
        box2 (group/create boxes-group 736 path-y "berrybox")
        turtle (characters/add-turtle game turtles-group 64 path-y :right turtle-tapped)]
    (set! (.-pathNumber turtle) number)
    (set! (.-berryCount box1) 3)
    (set! (.-berryCount box2) 3)
    (utils/set-attr! box1 [:frame] 3)
    (utils/set-attr! box2 [:frame] 3)
    (utils/set-attr! box1 [:body :immovable] true)
    (utils/set-attr! box2 [:body :immovable] true)
    {:number number
     :box1 box1
     :box2 box2}))

(defn carnivorous-update [game carnivorous-group]
  (let [elapsed (utils/elapsed game)]
    (doseq [carnivorous (:children carnivorous-group)]
      (let [cur-lifetime (.-lifetime carnivorous)
            new-lifetime (- cur-lifetime elapsed)]
        (set! (.-lifetime carnivorous) new-lifetime)
        (when (< new-lifetime 0)
          (sprite/destroy carnivorous))))))

(defn state-create [game]
  (let [turtles-group (object-factory/physics-group (:add game))
        boxes-group (object-factory/physics-group (:add game))
        carnivorous-group (object-factory/physics-group (:add game))
        path1 (create-turtle-path game turtles-group boxes-group 0)
        path2 (create-turtle-path game turtles-group boxes-group 1)
        path3 (create-turtle-path game turtles-group boxes-group 2)
        path4 (create-turtle-path game turtles-group boxes-group 3)
        path5 (create-turtle-path game turtles-group boxes-group 4)
        path6 (create-turtle-path game turtles-group boxes-group 5)
        path7 (create-turtle-path game turtles-group boxes-group 6)
        path8 (create-turtle-path game turtles-group boxes-group 7)
        crab (characters/add-crab game carnivorous-group 500 vertical-offset :left)]
    (swap! world-state!
           (fn [world-state]
             (-> world-state
                 (assoc-in [:turtles-group] turtles-group)
                 (assoc-in [:boxes-group] boxes-group)
                 (assoc-in [:carnivorous-group] carnivorous-group)
                 (assoc-in [:turtle-paths] [path1 path2 path3 path4 path5 path6 path7 path8]))))))

(defn state-update [game]
  (let [{:keys [turtles-group boxes-group carnivorous-group]} @world-state!]
    (arcade-physics/collide (:arcade (:physics game))
                            turtles-group boxes-group turtle-meet-berrybox)
    (arcade-physics/collide (:arcade (:physics game))
                            turtles-group carnivorous-group turtle-meet-carnivorous)
    (carnivorous-update game carnivorous-group)))

(def state-obj
  {:create state-create
   :update (fn [game] (state-update game))})
