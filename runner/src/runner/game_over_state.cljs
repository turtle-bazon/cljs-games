(ns runner.game-over-state
  (:require
   [phzr.animation-manager :as animation-manager]
   [phzr.game-object-factory :as object-factory]
   [phzr.group :as group]
   [phzr.impl.utils.core :refer [phaser->clj]]
   [phzr.keyboard :as keyboard]
   [phzr.physics.arcade :as arcade-physics]
   [phzr.scale-manager :as scale-manager]
   [phzr.signal :as signal]
   [phzr.sound :as sound]
   [phzr.sprite :as sprite]
   [phzr.state-manager :as sm]
   [phzr.timer :as timer]
   [runner.bubble :as bubble]
   [runner.dimensions :as dimens]
   [runner.info-panel :as info-panel]
   [runner.utils :as utils :refer [log mobile? exit-app]]))

(def state-atom (atom))

(defn restart-game [game]
  (sm/start (:state game) "play" true))

(defn create-restart-button [game]
  (object-factory/button (:add game)
                         (/ (- (:width game) (:width dimens/restart-button)) 2)
                         (/ (- (:height game) (:height dimens/restart-button)) 2)
                         "restart-button"
                         #(do (sound/play (object-factory/audio (:add game)
                                                                "bubble-vanish-sound"))
                              (restart-game game))
                         nil
                         0 1 0 1))

(defn switch-fullscreen [game]
  (let [scale (:scale game)]
    (if (:is-full-screen scale)
      (scale-manager/stop-full-screen scale)
      (scale-manager/start-full-screen scale false))))

(defn create-fullscreen-button [game]
  (object-factory/button (:add game)
                         (- (:width game) 50) 0
                         "fullscreen-button"
                         #(switch-fullscreen game)
                         nil
                         0 1 0 1))

(defn create-exit-button [game]
  (object-factory/button (:add game)
                         0 0
                         "exit-button"
                         #(exit-app)
                         nil
                         0 1 0 1))

(defn handle-desktop [game]
  (create-fullscreen-button game)
  (let [key (keyboard/add-key (get-in game [:input :keyboard])
                              (keyboard/const :spacebar))]
    (signal/add (:on-down key) #(restart-game game))))

(defn handle-mobile [game]
  (create-exit-button game))

(defn state-create [game]
  (bubble/add-background game)
  (info-panel/init! game @state-atom)
  (create-restart-button game)
  (if mobile?
    (handle-mobile game)
    (handle-desktop game)))

(defn state-init [game-state]
  (reset! state-atom (phaser->clj game-state)))

(def state-obj
  {:init state-init
   :create state-create})
