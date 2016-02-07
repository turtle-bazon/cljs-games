(ns bubbles.game-over-state
  (:require
   [phzr.animation-manager :as animation-manager]
   [phzr.game-object-factory :as object-factory]
   [phzr.group :as group]
   [phzr.physics.arcade :as arcade-physics]
   [phzr.scale-manager :as scale-manager]
   [phzr.sound :as sound]
   [phzr.sprite :as sprite]
   [phzr.state-manager :as sm]
   [phzr.timer :as timer]
   [bubbles.bubble :as bubble]
   [bubbles.info-panel :as info-panel]
   [bubbles.utils :as utils :refer [log]]))

(defn set-highscore! [highscore]
  (info-panel/set-highscore-text! highscore))

(defn get-highscore []
  (or (.getItem js/localStorage "highscore") 0))

(defn restart-game [game]
  (sm/start (:state game) "play" true))

(defn create-restart-button [game]
  (object-factory/button (:add game)
                         368 233
                         "restart-button"
                         #(restart-game game)
                         nil
                         0 1 1))

(defn switch-fullscreen [game]
  (let [scale (:scale game)]
    (if (:is-full-screen scale)
      (scale-manager/stop-full-screen scale)
      (scale-manager/start-full-screen scale false))))

(defn create-fullscreen-button [game]
  (object-factory/button (:add game)
                         750 0
                         "fullscreen-button"
                         #(switch-fullscreen game)
                         nil
                         0 1 1))

(defn state-create [game]
  (bubble/add-background game (fn [background event]))
  (create-fullscreen-button game)
  (info-panel/init! game)
  (set-highscore! (get-highscore))
  (create-restart-button game))

(def state-obj
  {:create state-create})
