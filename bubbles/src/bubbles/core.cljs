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
(def size-atom (atom {}))

(def mobile-height 854)
(def game-size-desktop {:width 854 :height 569})

(defn run-game [game-size]
  (when-let [old-game @game-atom]
    (game/destroy old-game))
  (let [game (game/->Game (:width game-size) (:height game-size)
                          (p/phaser-constants :canvas) "game")
        state-manager (:state game)
        correction-coefficient (/ (:height game-size) 569)] ;;; TODO: EDIT THIS !!! 
    (reset! game-atom game)
    (set! (.-cc game) correction-coefficient)
    (sm/add state-manager "boot" boot-state/state-obj)
    (sm/add state-manager "menu" menu-state/state-obj)
    (sm/add state-manager "play" play-state/state-obj)
    (sm/add state-manager "game-over" game-over-state/state-obj)
    (sm/start state-manager "boot" nil)))

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

(defn on-height [height]
  (let [game-height (/ height (aget js/window "devicePixelRatio"))
        size (swap! size-atom assoc :height game-height)]
    (set-size-android size)))

(defn on-width [width]
  (let [game-width (/ width (aget js/window "devicePixelRatio"))
        size (swap! size-atom assoc :width game-width)]
    (.immersiveHeight js/AndroidFullScreen on-height fail)))

(defn handle-immersive-mode []
  (.immersiveWidth js/AndroidFullScreen on-width fail))

(defn handle-lean-mode []
  (let [width (aget js/screen "width")
        height (aget js/screen "height")]
    (run-game {:width width :height height})))

(defn handle-is-immersive-mode-supported [supported]
  (if supported
    (.immersiveMode js/AndroidFullScreen handle-immersive-mode fail)
    (.leanMode js/AndroidFullScreen handle-lean-mode fail)))

(defn get-game-size-mobile []
  (.isImmersiveModeSupported js/AndroidFullScreen
                             handle-is-immersive-mode-supported fail))

(defn init-game []
  (if (cordova?)
    (get-game-size-mobile)
    (let [width (aget js/window "innerWidth")
          height (aget js/window "innerHeight")]
      (run-game {:width width :height height}))))

(defn ^:export start []
  (init-game))
