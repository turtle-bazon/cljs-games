(ns bubbles.boot-state
  (:require
   [phzr.loader :as loader]
   [phzr.physics :as physics]
   [phzr.state-manager :as sm]
   [bubbles.dimensions :as dimens]
   [bubbles.utils :as utils :refer [log mobile?]]))

(defn state-preload [game]
  (doto (:load game)
    (loader/image "background" (if mobile?
                                 "assets/images/background-portrait.png"
                                 "assets/images/background.png"))
    (loader/spritesheet "start-button" "assets/images/start-button.png"
                        (:width dimens/start-button) (:height dimens/start-button))
    (loader/spritesheet "restart-button" "assets/images/restart-button.png"
                        (:width dimens/restart-button) (:height dimens/restart-button))
    (loader/spritesheet "bubble" "assets/images/bubble.png"
                        (:width dimens/bubble) (:height dimens/bubble))
    (loader/image "lives" "assets/images/lives.png")
    (loader/image "score" "assets/images/score.png")
    (loader/image "highscore" "assets/images/highscore.png")
    (loader/audio "music" "assets/audio/music.ogg")
    (loader/audio "bubble-create-sound" "assets/audio/bubble-create.ogg")
    (loader/audio "bubble-vanish-sound" "assets/audio/bubble-vanish.ogg"))
  (if mobile?
    (doto (:load game)
      (loader/spritesheet "about-button" "assets/images/about-button.png"
                          (:width dimens/about-button) (:height dimens/about-button)))
    (doto (:load game)
      (loader/spritesheet "fullscreen-button" "assets/images/fullscreen-button.png"
                          (:width dimens/fullscreen-button)
                          (:height dimens/fullscreen-button)))))

(defn state-create [game]
  (physics/start-system (:physics game) (physics/const :arcade))
  (sm/start (:state game) "menu" true))

(def state-obj
  {:preload state-preload
   :create state-create})
