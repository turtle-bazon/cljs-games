(ns turtle-attention.world
  (:require
   [turtle-attention.utils :as utils]
   [phzr.group :as group]))

(defn add-berrybox [game boxes-group x y]
  (let [box (group/create boxes-group x y "berrybox")]
    (set! (.-berryCount box) 3)
    (utils/set-attr! box [:frame] 3)
    (utils/set-attr! box [:body :immovable] true)
    box))
