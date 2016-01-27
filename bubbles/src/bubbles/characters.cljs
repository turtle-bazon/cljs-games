(ns bubbles.characters
  (:require
   [bubbles.utils :as utils]
   [phzr.animation-manager :as animation-manager]
   [phzr.core :as pcore]
   [phzr.game-object-factory :as object-factory]
   [phzr.group :as group]
   [phzr.signal :as signal]))

(defn add-bubble [game bubbles-group x y tap-listener]
  (let [bubble (group/create bubbles-group x y "bubble")]
    (utils/set-attr! bubble [:body :collide-world-bounds] true)
    (utils/set-attr! bubble [:input-enabled] true)
    (signal/add (get-in bubble [:events :on-input-down]) tap-listener bubble)
    bubble))
