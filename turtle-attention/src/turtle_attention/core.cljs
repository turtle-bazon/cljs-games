(ns turtle-attention.core
  (:require
   [turtle-attention.boot-state :as boot-state]
   [turtle-attention.play-state :as play-state]
   [phzr.core :as p :refer [pset!]]
   [phzr.game :as game]
   [phzr.state-manager :as sm]))

(defonce game! (atom nil))

(defn start []
  (when-let [old-game @game!]
    (game/destroy old-game))
  (let [game (game/->Game 1280 720 (p/phaser-constants :auto) "game")
        state-manager (:state game)]
    (reset! game! game)
    (sm/add state-manager "boot" boot-state/state-obj)
    (sm/add state-manager "play" play-state/state-obj)
    (sm/start state-manager "boot" nil)))

(set! (.-onload js/window) start)
