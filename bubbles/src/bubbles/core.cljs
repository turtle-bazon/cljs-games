(ns bubbles.core
  (:require [phzr.core :as p :refer [pset!]]
            [phzr.game :as game]
            [phzr.state-manager :as sm]
            [bubbles.boot-state :as boot-state]
            [bubbles.menu-state :as menu-state]
            [bubbles.play-state :as play-state]
            [bubbles.game-over-state :as game-over-state]
            [bubbles.utils :refer [log cordova?]]))

(defonce game-atom (atom nil))

(def mobile-height 854)
(def game-size-desktop {:width 854 :height 569})

(defn get-game-size-mobile []
  (let [device-ratio (/ (aget js/screen "width")
                        (aget js/screen "height"))
        ;; dev
        device-ratio (/ 480 800)
        height mobile-height
        width (* device-ratio height)]
    {:width width :height height}))

(defn get-game-size []
  (if (cordova?)
    (get-game-size-mobile)
    game-size-desktop)
  ;; dev
  (get-game-size-mobile))

(defn ^:export start []
  (when-let [old-game @game-atom]
    (game/destroy old-game))
  (let [game-size (get-game-size)
        game (game/->Game (:width game-size) (:height game-size)
                          (p/phaser-constants :auto) "game")
        state-manager (:state game)]
    (reset! game-atom game)
    (sm/add state-manager "boot" boot-state/state-obj)
    (sm/add state-manager "menu" menu-state/state-obj)
    (sm/add state-manager "play" play-state/state-obj)
    (sm/add state-manager "game-over" game-over-state/state-obj)
    (sm/start state-manager "boot" nil)))
