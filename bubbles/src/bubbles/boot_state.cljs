(ns bubbles.boot-state
  (:require
   [phzr.loader :as loader]
   [phzr.physics :as physics]
   [phzr.state-manager :as sm]
   [bubbles.utils :as utils :refer [log]]))

(defn state-preload [game]
  (doto (:load game)
    (loader/image "background" "assets/background.png")
    (loader/spritesheet "bubble" "assets/bubble.png" 64 64)
    (loader/audio "music" "assets/audio/music.ogg")
    (loader/audio "bubble-vanish-sound" "assets/audio/bubble-vanish.ogg")))

(defn state-create [game]
  (physics/start-system (:physics game) (physics/const :arcade))
  (sm/start (:state game) "play" nil))

(def state-obj
  {:preload state-preload
   :create state-create})
