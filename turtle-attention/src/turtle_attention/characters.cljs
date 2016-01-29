(ns turtle-attention.characters
  (:require
   [turtle-attention.utils :as utils]
   [phzr.animation-manager :as animation-manager]
   [phzr.core :as pcore]
   [phzr.game-object-factory :as object-factory]
   [phzr.group :as group]
   [phzr.signal :as signal]))

(defn turtle-left [turtle]
  (set! (.-direction turtle) :left)
  (utils/set-attr! turtle [:body :velocity :x] -25)
  (animation-manager/play (:animations turtle) "left"))

(defn turtle-right [turtle]
  (set! (.-direction turtle) :right)
  (utils/set-attr! turtle [:body :velocity :x] 25)
  (animation-manager/play (:animations turtle) "right"))

(defn turtle-stop [turtle]
  (utils/set-attr! turtle [:body :velocity :x] 0)
  (animation-manager/stop (:animations turtle))
  (utils/set-attr! turtle [:frame] (case (.-direction turtle)
                                     :right 3
                                     :left 10)))

(defn add-turtle [game turtles-group x y tap-listener]
  (let [turtle (group/create turtles-group x y "turtle")]
    (animation-manager/add (:animations turtle) "right" [0 1 2 3 4 5 6 5 4 3 2 1] 16 true)
    (animation-manager/add (:animations turtle) "left" [7 8 9 10 11 12 13 12 11 10 9 8] 16 true)
    (utils/set-attr! turtle [:body :collide-world-bounds] true)
    (utils/set-attr! turtle [:input-enabled] true)
    (signal/add (get-in turtle [:events :on-input-down]) tap-listener turtle)
    turtle))

(defn add-crab [game carnivorous-group x y]
  )

(defn add-crocodile [game carnivorous-group x y]
  )
