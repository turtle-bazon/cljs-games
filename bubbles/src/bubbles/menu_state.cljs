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
   [bubbles.sound-wrapper :as sw]
   [bubbles.utils :as utils :refer [log cordova?]]))

(defn get-highscore []
  (or (.getItem js/localStorage "highscore") 0))

(defn exit-app []
  (.exitApp (aget js/navigator "app")))

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
                         #(do (sw/play (sw/get-sound game "bubble-vanish-sound"))
                              (start-game game))
                         nil
                         0 1 1))

(defn handle-desktop [game]
  (create-fullscreen-button game)
  (utils/set-attr! game [:scale :full-screen-scale-mode]
                   (scale-manager/const :show-all))
  ;(aset (:scale game) "pageAlignHorizontally" true)
  )

(defn handle-mobile [game]
  (create-exit-button game)
  (let [scale (:scale game)]
    (utils/set-attr! game [:scale :scale-mode]
                     (scale-manager/const :show-all))
    (aset scale "pageAlignHorizontally" true)
    (aset scale "pageAlignVertically" true)
    (aset scale "setScreenSize" true)
    (scale-manager/refresh scale)))

(defn state-create [game]
  (bubble/add-background game)
  (create-start-button game)
  (if (cordova?)
    (handle-mobile game)
    (handle-desktop game))
  (sw/loop-sound (sw/get-sound game "music") 37000))

(def state-obj
  {:create state-create})
