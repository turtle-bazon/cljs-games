(ns turtle-attention.world
  (:require
   [turtle-attention.utils :as utils]
   [phzr.group :as group]))

(defn berrybox-dec [box]
  (let [current-berries (.-berryCount box)
        berries-after-eat (dec current-berries)]
    (when (>= berries-after-eat 0)
      (set! (.-berryCount box) berries-after-eat)
      (utils/set-attr! box [:frame] berries-after-eat))))

(defn add-berrybox [game boxes-group x y]
  (let [box (group/create boxes-group x y "berrybox")]
    (set! (.-berryCount box) 3)
    (utils/set-attr! box [:frame] 3)
    (utils/set-attr! box [:body :immovable] true)
    box))
