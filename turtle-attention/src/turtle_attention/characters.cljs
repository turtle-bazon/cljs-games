(ns turtle-attention.characters
  (:require
   [turtle-attention.utils :as utils]
   [phzr.animation-manager :as animation-manager]
   [phzr.core :as pcore]
   [phzr.game-object-factory :as object-factory]
   [phzr.group :as group]
   [phzr.signal :as signal]))

(defn add-turtle [game turtles-group x y tap-listener]
  (let [turtle (group/create turtles-group x y "turtle")]
    (animation-manager/add (:animations turtle) "right" [0 1 2 3] 8 true)
    (animation-manager/add (:animations turtle) "left" [4 5 6 7] 8 true)
    (utils/set-attr! turtle [:body :collide-world-bounds] true)
    (utils/set-attr! turtle [:input-enabled] true)
    (signal/add (get-in turtle [:events :on-input-down]) tap-listener turtle)
    turtle))
