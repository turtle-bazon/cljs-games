(ns bubbles.info-panel
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
   [bubbles.utils :as utils :refer [log]]))

(def state-atom (atom))
(def info-position-y 16)

(defn add-score [game score]
  (object-factory/text (:add game) 220 info-position-y
                       (str "Score: " score)
                       {:font "24px Arial",
                        :fill "#FFFFFF",
                        :align "center"}))

(defn add-highscore [game highscore]
  (object-factory/text (:add game) 32 info-position-y
                       (str "Highscore: " highscore)
                       {:font "24px Arial",
                        :fill "#FFFFFF",
                        :align "center"}))

(defn add-lives [game lives]
  (object-factory/text (:add game) 340 info-position-y
                       (str "Lives: " lives)
                       {:font "24px Arial",
                        :fill "#FFFFFF",
                        :align "center"}))

(defn set-score-text! [score]
  (let [score-text (:score-text @state-atom)]
    (utils/set-attr! score-text [:text] (str "Score: " score))))

(defn set-highscore-text! [highscore]
  (let [highscore-text (:highscore-text @state-atom)]
    (utils/set-attr! highscore-text [:text] (str "Highscore: " highscore))))

(defn set-lives-text! [lives]
  (let [lives-text (:lives-text @state-atom)]
    (utils/set-attr! lives-text [:text] (str "Lives: " lives))))

(defn init! [game initial-state]
  (let [{:keys [score highscore lives]} initial-state]
    (reset! state-atom
            {:score-text (add-score game score)
             :highscore-text (add-highscore game highscore)
             :lives-text (add-lives game lives)})))