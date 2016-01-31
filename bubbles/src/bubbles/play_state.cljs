(ns bubbles.play-state
  (:require
   [phzr.animation-manager :as animation-manager]
   [phzr.game-object-factory :as object-factory]
   [phzr.group :as group]
   [phzr.physics.arcade :as arcade-physics]
   [phzr.sound :as sound]
   [phzr.timer :as timer]
   [bubbles.bubble :as bubble]
   [bubbles.utils :as utils :refer [log]]))

(def state-atom (atom))

(def initial-state {:score 0
                    :lives 10
                    :bubble-create-interval 800})
(def info-position-y 20)

(defn add-score [game]
  (object-factory/text (:add game) 32 info-position-y
                       (str "Score: " (:score initial-state))
                       {:font "24px Arial",
                        :fill "#FFFFFF",
                        :align "center"}))

(defn add-lives [game]
  (object-factory/text (:add game) 170 info-position-y
                       (str "Lives: " (:lives initial-state))
                       {:font "24px Arial",
                        :fill "#FFFFFF",
                        :align "center"}))

(defn update-score! [update-fn]
  (let [state (swap! state-atom update :score update-fn)
        score-text (:score-text state)
        score (:score state)]
    (utils/set-attr! score-text [:text] (str "Score: " score))))

(defn update-lives! [update-fn]
  (let [state (swap! state-atom update :lives update-fn)
        lives-text (:lives-text state)
        lives (:lives state)]
    (utils/set-attr! lives-text [:text] (str "Lives: " lives))))

(defn is-game-over? []
  (<= (:lives @state-atom) 0))

(defn on-bubble-hit []
  (update-score! inc))

(defn on-bubble-miss []
  (update-lives! dec))

(defn on-bubble-vanish []
  (update-lives! dec))

(defn state-create [game]
  (let [background (bubble/add-background game (fn [background event]
                                                 (when (not (is-game-over?))
                                                   (update-lives! dec))))
        music (object-factory/audio (:add game) "music")
        score-text (add-score game)
        lives-text (add-lives game)
        bubbles (bubble/init-bubbles game on-bubble-hit on-bubble-miss
                                     on-bubble-vanish is-game-over?)]
    (sound/loop-full music)
    (reset! state-atom
            (merge initial-state
                   {:bubbles bubbles
                    :score-text score-text
                    :lives-text lives-text}))
    (bubble/start-bubbles game bubbles)))

(defn state-update [game]
  (let [{:keys [bubbles]} @state-atom]
    (bubble/update-bubbles game bubbles)))

(def state-obj
  {:create state-create
   :update (fn [game] (state-update game))})
