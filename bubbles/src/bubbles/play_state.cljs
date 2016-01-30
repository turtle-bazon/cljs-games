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

(defonce state-atom (atom {}))

(defonce score-atom (atom))
(defonce lives-atom (atom))
(defonce bubble-create-interval (atom))
(def info-position-y 20)

(defn add-score [game]
  (object-factory/text (:add game) 64 info-position-y (str "Score: " @score-atom)
                       {:font "32px Arial",
                        :fill "#ff0044",
                        :align "center"}))

(defn show-score [score-text]
  (utils/set-attr! score-text [:text] (str "Score: " @score-atom)))

(defn add-lives [game]
  (object-factory/text (:add game) 256 info-position-y (str "Lives: " @lives-atom)
                       {:font "32px Arial",
                        :fill "#ff0044",
                        :align "center"}))

(defn show-lives [lives-text]
  (utils/set-attr! lives-text [:text] (str "Lives: " @lives-atom)))

(defn is-game-over? []
  (<= @lives-atom 0))

(defn on-bubble-hit []
  (swap! score-atom inc))

(defn on-bubble-miss []
  (swap! lives-atom dec))

(defn on-bubble-vanish []
  (swap! lives-atom dec))

(defn state-create [game]
  (let [background (bubble/add-background game (fn [background event]
                                                 (swap! lives-atom dec)))
        music (object-factory/audio (:add game) "music")
        score-text (add-score game)
        lives-text (add-lives game)
        bubbles (bubble/init-bubbles game on-bubble-hit on-bubble-miss
                                     on-bubble-vanish is-game-over?)]
    (sound/loop-full music)
    (reset! score-atom 0)
    (reset! lives-atom 10)
    (reset! bubble-create-interval 800)
    (reset! state-atom
            (-> {}
                (assoc-in [:bubbles] bubbles)
                (assoc-in [:score-text] score-text)
                (assoc-in [:lives-text] lives-text)))))

(defn state-update [game]
  (let [{:keys [bubbles score-text lives-text]} @state-atom]
    (bubble/update-bubbles game bubbles)
    (show-score score-text)
    (show-lives lives-text)))

(def state-obj
  {:create state-create
   :update (fn [game] (state-update game))})
