(ns bubbles.core
  (:require [phzr.core :as p :refer [pset!]]
            [phzr.game :as game]
            [phzr.state-manager :as sm]
            [bubbles.boot-state :as boot-state]
            [bubbles.menu-state :as menu-state]
            [bubbles.play-state :as play-state]
            [bubbles.game-over-state :as game-over-state]
            [bubbles.utils :refer [environment log]]))

(defonce game-atom (atom nil))
(def size-atom (atom {}))

(def mobile-height 854)
(def game-size-desktop {:width 854 :height 569})

(defn diagonal [game-size]
  (let [width (:width game-size)
        height (:height game-size)]
    (.sqrt js/Math (+ (* width width) (* height height)))))

(defn run-game [game-size]
  (when-let [old-game @game-atom]
    (game/destroy old-game)
    (reset! game-atom nil))
  (let [game (game/->Game (:width game-size) (:height game-size)
                          (p/phaser-constants :auto) "game")
        state-manager (:state game)
        correction-coefficient (/ (diagonal game-size) 768)] ;;; TODO: EDIT THIS !!! 
    (reset! game-atom game)
    (set! (.-cc game) correction-coefficient)
    (sm/add state-manager "boot" boot-state/state-obj)
    (sm/add state-manager "menu" menu-state/state-obj)
    (sm/add state-manager "play" play-state/state-obj)
    (sm/add state-manager "game-over" game-over-state/state-obj)
    (sm/start state-manager "boot" nil)))

(defn get-size-android [size]
  (let [screen-width (:width size)
        screen-height (:height size)
        screen-ratio (/ screen-width screen-height)
        height mobile-height
        width (int (+ (* screen-ratio height) 0.5))]
    {:width screen-width
     :height screen-height}))

(defn init-game []
  (let [pixel-ratio (aget js/window "devicePixelRatio")]
    (run-game {:width (* (aget js/window "innerWidth") pixel-ratio)
               :height (* (aget js/window "innerHeight") pixel-ratio)})))

(defn ^:export start []
  (init-game))
