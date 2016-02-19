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

(def game-size-mobile {:width 480 :height 854})
(def game-size-desktop {:width 854 :height 480})

(defn get-game-size []
  (if (cordova?)
    game-size-mobile
    game-size-desktop)
  ;; dev
  game-size-mobile)

(defn ^:export start []
  (when-let [old-game @game-atom]
    (game/destroy old-game))
  (let [game-size (get-game-size)
        game (game/->Game (:width game-size) (:height game-size)
                          (p/phaser-constants :canvas) "game")
        state-manager (:state game)]
    (reset! game-atom game)
    (sm/add state-manager "boot" boot-state/state-obj)
    (sm/add state-manager "menu" menu-state/state-obj)
    (sm/add state-manager "play" play-state/state-obj)
    (sm/add state-manager "game-over" game-over-state/state-obj)
    (sm/start state-manager "boot" nil)))
