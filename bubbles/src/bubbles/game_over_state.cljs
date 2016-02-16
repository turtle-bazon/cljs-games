(ns bubbles.game-over-state
  (:require
   [phzr.animation-manager :as animation-manager]
   [phzr.game-object-factory :as object-factory]
   [phzr.group :as group]
   [phzr.impl.utils.core :refer [phaser->clj]]
   [phzr.physics.arcade :as arcade-physics]
   [phzr.scale-manager :as scale-manager]
   [phzr.sound :as sound]
   [phzr.sprite :as sprite]
   [phzr.state-manager :as sm]
   [phzr.timer :as timer]
   [bubbles.bubble :as bubble]
   [bubbles.info-panel :as info-panel]
   [bubbles.utils :as utils :refer [log cordova?]]))

(def state-atom (atom))

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
                         (- (:width game) 50) 0
                         "fullscreen-button"
                         #(switch-fullscreen game)
                         nil
                         0 1 1))

(defn exit-app []
  (.exitApp (aget js/navigator "app")))

(defn create-exit-button [game]
  (object-factory/button (:add game)
                         0 0
                         "exit-button"
                         #(exit-app)
                         nil
                         0 1 1))

(defn handle-desktop [game]
  (create-fullscreen-button game))

(defn handle-mobile [game]
  (create-exit-button game))

(defn state-create [game]
  (bubble/add-background game (fn [background event]))
  (info-panel/init! game @state-atom)
  (create-restart-button game)
  (if (cordova?)
    (handle-mobile game)
    (handle-desktop game)))

(defn state-init [game-state]
  (reset! state-atom (phaser->clj game-state)))

(def state-obj
  {:init state-init
   :create state-create})
