(ns bubbles.core
  (:require [phzr.core :as p :refer [pset!]]
            [phzr.game :as game]
            [phzr.state-manager :as sm]
            [bubbles.boot-state :as boot-state]
            [bubbles.menu-state :as menu-state]
            [bubbles.play-state :as play-state]
            [bubbles.game-over-state :as game-over-state]))

(defonce game-atom (atom nil))

(defn ^:export start []
  (when-let [old-game @game-atom]
    (game/destroy old-game))
  (let [game (game/->Game 854 480 (p/phaser-constants :canvas) "game")
        state-manager (:state game)]
    (reset! game-atom game)
    (sm/add state-manager "boot" boot-state/state-obj)
    (sm/add state-manager "menu" menu-state/state-obj)
    (sm/add state-manager "play" play-state/state-obj)
    (sm/add state-manager "game-over" game-over-state/state-obj)
    (sm/start state-manager "boot" nil)))
