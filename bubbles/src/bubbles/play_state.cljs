(ns bubbles.play-state
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
   [bubbles.info-panel :as info-panel]
   [bubbles.utils :as utils :refer [log]]))

(def state-atom (atom))

(def initial-state {:score 0
                    :highscore 0
                    :lives 10})

(defn set-score! [score]
  (let [state (swap! state-atom assoc :score score)]
    (info-panel/set-score-text! score)))

(defn update-score! [update-fn]
  (let [state (swap! state-atom update :score update-fn)]
    (info-panel/set-score-text! (:score state))))

(defn set-highscore! [highscore]
  (let [state (swap! state-atom assoc :highscore highscore)]
    (info-panel/set-highscore-text! highscore)))

(defn get-highscore []
  (or (.getItem js/localStorage "highscore") 0))

(defn save-highscore! []
  (let [score (:score @state-atom)
        highscore (get-highscore)]
    (when (< highscore score)
      (.setItem js/localStorage "highscore" score)
      (set-highscore! score))))

(defn game-over! [game]
  (save-highscore!)
  (let [state @state-atom]
    (sm/start (:state game) "game-over" true false
              (select-keys state [:score :highscore :lives]))))

(defn update-lives! [update-fn]
  (let [state (swap! state-atom update :lives update-fn)
        lives (:lives state)]
    (info-panel/set-lives-text! lives)))

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
        bubbles (bubble/init-bubbles game on-bubble-hit on-bubble-miss
                                     on-bubble-vanish is-game-over?)]
    (reset! state-atom
            (merge initial-state
                   {:background background
                    :bubbles bubbles}))
    (info-panel/init! game initial-state)
    (set-highscore! (get-highscore))
    (bubble/start-bubbles game bubbles)))

(defn state-update [game]
  (let [{:keys [bubbles lives]} @state-atom]
    (bubble/update-bubbles game bubbles)
    (when (<= lives 0)
      (game-over! game))))

(def state-obj
  {:create state-create
   :update state-update})
