(ns bubbles.menu-state
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
   [bubbles.dimensions :as dimens]
   [bubbles.utils :as utils :refer [log mobile? exit-app]]))

(defn get-highscore []
  (or (.getItem js/localStorage "highscore") 0))

(defn create-exit-button [game]
  (object-factory/button (:add game)
                         0 0
                         "exit-button"
                         #(exit-app)
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

(defn start-game [game]
  (sm/start (:state game) "play" true))

(defn create-start-button [game]
  (object-factory/button (:add game)
                         (/ (- (:width game) (:width dimens/start-button)) 2)
                         (/ (- (:height game) (:height dimens/start-button)) 2)
                         "start-button"
                         #(do (sound/play (object-factory/audio (:add game)
                                                                "bubble-vanish-sound"))
                              (start-game game))
                         nil
                         0 1 1))

(defn handle-desktop [game]
  (create-fullscreen-button game)
  (utils/set-attr! game [:scale :full-screen-scale-mode]
                   (scale-manager/const :show-all)))

(defn handle-mobile [game]
  (create-exit-button game)
  (let [scale (:scale game)]
    (utils/set-attr! game [:scale :scale-mode]
                     (scale-manager/const :show-all))
    (scale-manager/refresh scale))
    (.hideLaunchScreen js/launchScreen "" (fn [] nil)))

(defn state-create [game]
  (bubble/add-background game)
  (create-start-button game)
  (if mobile?
    (handle-mobile game)
    (handle-desktop game))
  (sound/loop-full (object-factory/audio (:add game) "music")))

(def state-obj
  {:create state-create})
