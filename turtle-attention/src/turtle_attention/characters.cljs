(ns turtle-attention.characters
  (:require
   [turtle-attention.utils :as utils]
   [phzr.animation :as animation-object]
   [phzr.animation-manager :as animation-manager]
   [phzr.core :as pcore]
   [phzr.game-object-factory :as object-factory]
   [phzr.group :as group]
   [phzr.signal :as signal]))

(defn calculate-frames [animations]
  [(reduce + (map (fn [[k n]] n) animations))
   (into {}
         (reduce
          (fn [[[pkey [plength pstart]] & _ :as data] [ckey clength]]
            (let [cstart (+ (or pstart 0) (or plength 0))]
              (cons [ckey [clength cstart (dec (+ cstart clength))]] data)))
          () animations))])

(defn animations-range [[total-frames calculated-frames] key direction]
  (let [end-frame (dec (* total-frames 2))
        [length start end] (get calculated-frames key)]
    (map
     (fn [index]
       (str "sprite" (inc index)))
     (case direction
       :right (range start (inc end))
       :left (range (- end-frame start) (dec (- end-frame end)) -1)))))

(defn after-animation [sprite animation-key callback]
  (let [animation (animation-manager/get-animation (:animations sprite) animation-key)]
    (signal/add (:on-complete animation) callback)
    animation))

(defn turtle-left [turtle]
  (set! (.-direction turtle) :left)
  (utils/set-attr! turtle [:body :velocity :x] -25)
  (animation-manager/play (:animations turtle) "walk-left"))

(defn turtle-right [turtle]
  (set! (.-direction turtle) :right)
  (utils/set-attr! turtle [:body :velocity :x] 25)
  (animation-manager/play (:animations turtle) "walk-right"))

(defn turtle-walk [turtle]
  (case (.-direction turtle)
    :right (turtle-right turtle)
    :left (turtle-left turtle)))

(defn turtle-stop [turtle]
  (utils/set-attr! turtle [:body :velocity :x] 0)
  (animation-manager/stop (:animations turtle))
  (utils/set-attr! turtle [:frame] (case (.-direction turtle)
                                     :right 0
                                     :left 73)))

(defn turtle-hide [turtle]
  (utils/set-attr! turtle [:body :velocity :x] 0)
  (set! (.-clickable turtle) false)
  (animation-manager/play (:animations turtle) (case (.-direction turtle)
                                                 :right "hide-right"
                                                 :left "hide-left")))

(defn turtle-continue-move [turtle]
  (set! (.-clickable turtle) false)
  (animation-manager/play (:animations turtle) (case (.-direction turtle)
                                                 :right "unhide-right"
                                                 :left "unhide-left")))

(defn turtle-after-hide [turtle animation]
  (set! (.-clickable turtle) true))

(defn turtle-after-unhide [turtle animation]
  (set! (.-clickable turtle) true)
  (turtle-walk turtle))

(defn add-turtle [game turtles-group x y direction tap-listener]
  (let [turtle (group/create turtles-group x y "turtle")
        animations [[:walk 25] [:hide 12]]
        calculated-frames (calculate-frames animations)]
    (doto (:animations turtle)
      (animation-manager/add
       "walk-right" (animations-range calculated-frames :walk :right) 25 true)
      (animation-manager/add
       "walk-left" (animations-range calculated-frames :walk :left) 25 true)
      (animation-manager/add
       "hide-right" (animations-range calculated-frames :hide :right) 32 false)
      (animation-manager/add
       "hide-left" (animations-range calculated-frames :hide :left) 32 false)
      (animation-manager/add
       "unhide-right" (reverse (animations-range calculated-frames :hide :right)) 25 false)
      (animation-manager/add
       "unhide-left" (reverse (animations-range calculated-frames :hide :left)) 25 false))
    (after-animation turtle "hide-right" turtle-after-hide)
    (after-animation turtle "hide-left" turtle-after-hide)
    (after-animation turtle "unhide-right" turtle-after-unhide)
    (after-animation turtle "unhide-left" turtle-after-unhide)
    (utils/set-attr! turtle [:body :collide-world-bounds] true)
    (utils/set-attr! turtle [:input-enabled] true)
    (signal/add (get-in turtle [:events :on-input-down]) tap-listener turtle)
    (set! (.-clickable turtle) true)
    (set! (.-direction turtle) direction)
    (turtle-walk turtle)
    turtle))

(defn crab-stay-animated [crab]
  (case (.-direction crab)
    :right (animation-manager/play (:animations crab) "stay-right")
    :left (animation-manager/play (:animations crab) "stay-left")))

(defn add-crab [game carnivorous-group x y direction]
  (let [crab (group/create carnivorous-group x y "crab")]
    (doto (:animations crab)
      (animation-manager/add "appear-right" [0] 16 false)
      (animation-manager/add "appear-left" [1] 16 false)
      (animation-manager/add "disappear-right" [0] 16 false)
      (animation-manager/add "disappear-left" [1] 16 false)
      (animation-manager/add "stay-right" [0] 16 true)
      (animation-manager/add "stay-left" [1] 16 true)
      (animation-manager/add "attack-right" [0] 16 false)
      (animation-manager/add  "attack-left" [1] 16 false))
    (utils/set-attr! crab [:body :immovable] true)
    (set! (.-direction crab) direction)
    (set! (.-lifetime crab) 15000)
    (crab-stay-animated crab)
    crab))

(defn add-crocodile [game carnivorous-group x y]
  )
