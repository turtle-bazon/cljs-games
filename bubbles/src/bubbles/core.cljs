(ns bubbles.core
  (:require [phzr.core :as p :refer [pset!]]
            [phzr.game :as game]
            [phzr.state-manager :as sm]
            [bubbles.boot-state :as boot-state]
            [bubbles.menu-state :as menu-state]
            [bubbles.play-state :as play-state]
            [bubbles.game-over-state :as game-over-state]
            [bubbles.utils :refer [log cordova?]]))

(defonce game-atom (atom nil))

(def mobile-height 854)
(def game-size-desktop {:width 854 :height 569})

(defn run-game [game-size]
  (when-let [old-game @game-atom]
    (game/destroy old-game))
  (let [game (game/->Game (:width game-size) (:height game-size)
                          (p/phaser-constants :auto) "game")
        state-manager (:state game)]
    (reset! game-atom game)
    (sm/add state-manager "boot" boot-state/state-obj)
    (sm/add state-manager "menu" menu-state/state-obj)
    (sm/add state-manager "play" play-state/state-obj)
    (sm/add state-manager "game-over" game-over-state/state-obj)
    (sm/start state-manager "boot" nil)))

(def size-atom (atom {}))

(defn success []
  (log "success"))

(defn fail []
  (log "fail"))

(defn set-size-android [size]
  (let [screen-width (:width size)
        screen-height (:height size)
        device-ratio (/ screen-width screen-height)
        height mobile-height
        width (* device-ratio height)]
    (run-game {:width width :height height})))

(defn on-width [width]
  (let [game-width (/ width (aget js/window "devicePixelRatio"))
        size (swap! size-atom assoc :width game-width)]
    (when (:height size)
      (set-size-android size))))

(defn on-height [height]
  (let [game-height (/ height (aget js/window "devicePixelRatio"))
        size (swap! size-atom assoc :height game-height)]
    (when (:width size)
      (set-size-android size))))

(defn get-game-size-mobile []
  (.immersiveMode js/AndroidFullScreen success fail)
  (.immersiveWidth js/AndroidFullScreen on-width fail)
  (.immersiveHeight js/AndroidFullScreen on-height fail))

(defn init-game []
  (if (cordova?)
    (get-game-size-mobile)
    (run-game game-size-desktop)))

(defn ^:export start []
  (init-game))
