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
   [bubbles.utils :as utils :refer [log mobile? create-exit-button]]))

(def update-number-atom (atom 0))

(defn get-highscore []
  (or (.getItem js/localStorage "highscore") 0))

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
                         0 1 0 1))

(defn show-about []
  (.showAboutDialog js/aboutExtension "" (fn [] false)))

(defn create-about-button [game]
  (object-factory/button (:add game)
                         (/ (- (:width game) (:width dimens/about-button)) 2)
                         (+ (/ (- (:height game) (:height dimens/about-button)) 2) (:height dimens/about-button) 10)
                         "about-button"
                         #(do (sound/play (object-factory/audio (:add game)
                                                                "bubble-vanish-sound"))
                              (show-about))
                         nil
                         0 1 0 1))

(defn handle-desktop [game]
  (create-fullscreen-button game)
  (utils/set-attr! game [:scale :full-screen-scale-mode]
                   (scale-manager/const :show-all)))

(defn handle-mobile [game]
  (create-about-button game)
  (create-exit-button game)
  (let [scale (:scale game)]
    (utils/set-attr! game [:scale :scale-mode]
                     (scale-manager/const :show-all))
    (scale-manager/refresh scale)))

(defn state-create [game]
  (bubble/add-background game)
  (create-start-button game)
  (if mobile?
    (handle-mobile game)
    (handle-desktop game))
  (sound/loop-full (object-factory/audio (:add game) "music")))

(defn state-render [game]
  (when mobile?
    (let [update-number @update-number-atom]
      (when (< update-number 5)
        (when (= 4 update-number)
          (.hideLaunchScreen js/launchScreenExtension "" (fn [] nil)))
        (swap! update-number-atom inc)))))

(def state-obj
  {:create state-create
   :render state-render})
