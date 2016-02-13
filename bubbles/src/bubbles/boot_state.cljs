(ns bubbles.boot-state
  (:require
   [phzr.loader :as loader]
   [phzr.physics :as physics]
   [phzr.state-manager :as sm]
   [bubbles.utils :as utils :refer [log]]))

(defn state-preload [game]
  (doto (:load game)
    (loader/image "background" "assets/images/background.png")
    (loader/spritesheet "start-button" "assets/images/start-button.png" 64 64)
    (loader/spritesheet "restart-button" "assets/images/restart-button.png" 64 64)
    (loader/spritesheet "bubble" "assets/images/bubble.png" 64 64)
    (loader/spritesheet "fullscreen-button" "assets/images/fullscreen-button.png" 50 50)
    (loader/audio "music" "assets/audio/music.ogg")
    (loader/audio "bubble-vanish-sound" "assets/audio/bubble-vanish.ogg")))

(defn state-create [game]
  (physics/start-system (:physics game) (physics/const :arcade))
  (sm/start (:state game) "menu" true))

(def state-obj
  {:preload state-preload
   :create state-create})
