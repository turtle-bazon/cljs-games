(ns bubbles.core
  (:require [phzr.core :as p :refer [pset!]]
            [phzr.game :as game]
            [phzr.state-manager :as sm]
            [bubbles.boot-state :as boot-state]
            [bubbles.menu-state :as menu-state]
            [bubbles.play-state :as play-state]
            [bubbles.game-over-state :as game-over-state]
            [bubbles.utils :refer [environment log]]))

(defonce game-atom (atom nil))
(def size-atom (atom {}))

(def mobile-height 854)
(def game-size-desktop {:width 854 :height 569})

(defn diagonal [game-size]
  (let [width (:width game-size)
        height (:height game-size)]
    (.sqrt js/Math (+ (* width width) (* height height)))))

(defn run-game [game-size]
  (log "run-game")
  (log @game-atom)
  (log (str game-size))
  (when-let [old-game @game-atom]
    (log "when-let")
    (log old-game)
    (game/destroy old-game))
  (let [game (game/->Game (:width game-size) (:height game-size)
                          (p/phaser-constants :auto) "game")
        state-manager (:state game)
        correction-coefficient (/ (diagonal game-size) 768)] ;;; TODO: EDIT THIS !!! 
    (reset! game-atom game)
    (set! (.-cc game) correction-coefficient)
    (sm/add state-manager "boot" boot-state/state-obj)
    (sm/add state-manager "menu" menu-state/state-obj)
    (sm/add state-manager "play" play-state/state-obj)
    (sm/add state-manager "game-over" game-over-state/state-obj)
    (sm/start state-manager "boot" nil)))

(defn get-size-android [size]
  (let [screen-width (:width size)
        screen-height (:height size)
        screen-ratio (/ screen-width screen-height)
        height mobile-height
        width (int (+ (* screen-ratio height) 0.5))]
    {:width width
     :height height}))

(defn init-game []
  (log "init fn")
  (log @game-atom)
  (run-game (if (= :mobile (:display environment))
              (get-size-android {:width (or (aget js/window "deviceWidth")
                                            (aget js/window "innerWidth"))
                                 :height (or (aget js/window "deviceHeight")
                                             (aget js/window "innerHeight"))})
              game-size-desktop)))

(defn ^:export start []
  (log "start fn")
  (log @game-atom)
  (init-game))
