(ns bubbles.info-panel
  (:require
   [cljsjs.phaser :as jsphaser]
   [phzr.animation-manager :as animation-manager]
   [phzr.game-object-factory :as object-factory]
   [phzr.group :as group]
   [phzr.impl.utils.core :refer [clj->phaser phaser->clj]]
   [phzr.physics.arcade :as arcade-physics]
   [phzr.scale-manager :as scale-manager]
   [phzr.sound :as sound]
   [phzr.sprite :as sprite]
   [phzr.state-manager :as sm]
   [phzr.timer :as timer]
   [bubbles.utils :as utils :refer [log mobile?]]))

(def state-atom (atom))
(def info-position-x 20)
(def info-position-y 14)
(def font {:font "28px Flubber"
           :fill "#FFFFFF"
           :align "center"})

;; For text effect
(defn wrap-text [value]
  (str " " value " "))

(defn apply-text-effects [text]
  (let [gradient (.createLinearGradient (:context text) 0 0 0 (:height text))]
    (.addColorStop gradient 0 "#FFFFFF")
    (.addColorStop gradient 1 "#707070")
    (utils/set-attr! text [:stroke] "#000000")
    (utils/set-attr! text [:stroke-thickness] 2)
    (utils/set-attr! text [:fill] gradient)
    (phaser->clj (.setShadow text
                             (clj->phaser 1)
                             (clj->phaser 1)
                             (clj->phaser "#101010")
                             (clj->phaser 5)))
    text))

(defn add-score [game score]
  (object-factory/image (:add game)
                        (+ info-position-x 120) 3
                        "score")
  (apply-text-effects
   (object-factory/text (:add game)
                        (+ info-position-x 165) info-position-y
                        (wrap-text score)
                        font)))

(defn add-highscore [game highscore]
  (object-factory/image (:add game)
                        info-position-x 3
                        "highscore")
  (apply-text-effects
   (object-factory/text (:add game)
                        (+ info-position-x 45) info-position-y
                        (wrap-text highscore)
                        font)))

(defn add-lives [game lives]
  (object-factory/image (:add game)
                        (+ info-position-x 230) 3
                        "lives")
  (apply-text-effects
   (object-factory/text (:add game)
                        (+ info-position-x 270) info-position-y
                        (wrap-text lives)
                        font)))

(defn set-score-text! [score]
  (let [score-text (:score-text @state-atom)]
    (utils/set-attr! score-text [:text] (wrap-text score))))

(defn set-highscore-text! [highscore]
  (let [highscore-text (:highscore-text @state-atom)]
    (utils/set-attr! highscore-text [:text] (wrap-text highscore))))

(defn set-lives-text! [lives]
  (let [lives-text (:lives-text @state-atom)]
    (utils/set-attr! lives-text [:text] (wrap-text lives))))

(defn init! [game initial-state]
  (let [{:keys [score highscore lives]} initial-state]
    (reset! state-atom
            {:score-text (add-score game score)
             :highscore-text (add-highscore game highscore)
             :lives-text (add-lives game lives)})))
