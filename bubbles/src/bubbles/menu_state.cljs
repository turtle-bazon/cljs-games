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
   [bubbles.utils :as utils :refer [log]]))

(def state-atom (atom {}))

(defn get-highscore []
  (or (.getItem js/localStorage "highscore") 0))

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

(defn start-game [game]
  (sm/start (:state game) "play" true))

(defn create-start-button [game]
  (object-factory/button (:add game)
                         368 233
                         "start-button"
                         #(start-game game)
                         nil
                         0 1 1))

(defn handle-mobile [game]
  (let [scale (:scale game)]
    (utils/set-attr! game [:scale :scale-mode]
                     (scale-manager/const :show-all))
    (set! (.-pageAlignHorizontally scale) true)
    (set! (.-pageAlignVertically scale) true)
    (set! (.-setScreenSize scale) true)
    (scale-manager/refresh scale)))

(defn state-create [game]
  (let [background (bubble/add-background game (fn [background event]
                                                 ))
        music (object-factory/audio (:add game) "music")
        fullscreen-button (create-fullscreen-button game)
        start-button (create-start-button game)]
    (utils/set-attr! game [:scale :full-screen-scale-mode]
                     (scale-manager/const :show-all))
    (set! (.-pageAlignHorizontally (:scale game)) true)
    (when (not (.-desktop (get-in game [:device])))
      (handle-mobile game))
    (sound/loop-full music)
    (reset! state-atom {:background background
                        :music music
                        :fullscreen-button fullscreen-button
                        :start-button start-button})))

(def state-obj
  {:create state-create})
