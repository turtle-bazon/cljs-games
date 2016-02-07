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
   [bubbles.utils :as utils :refer [log]]))

(def state-atom (atom))

(def initial-state {:score 0
                    :highscore 0
                    :lives 1
                    :bubble-create-interval 800})
(def info-position-y 16)

(defn add-score [game]
  (object-factory/text (:add game) 220 info-position-y
                       (str "Score: " (:score initial-state))
                       {:font "24px Arial",
                        :fill "#FFFFFF",
                        :align "center"}))

(defn add-highscore [game]
  (object-factory/text (:add game) 32 info-position-y
                       (str "Highscore: " (:highscore initial-state))
                       {:font "24px Arial",
                        :fill "#FFFFFF",
                        :align "center"}))

(defn add-lives [game]
  (object-factory/text (:add game) 340 info-position-y
                       (str "Lives: " (:lives initial-state))
                       {:font "24px Arial",
                        :fill "#FFFFFF",
                        :align "center"}))

(defn update-score! [update-fn]
  (let [state (swap! state-atom update :score update-fn)
        score-text (:score-text state)
        score (:score state)]
    (utils/set-attr! score-text [:text] (str "Score: " score))))

(defn set-highscore! [highscore]
  (let [state (swap! state-atom assoc :highscore highscore)
        highscore-text (:highscore-text state)]
    (utils/set-attr! highscore-text [:text] (str "Highscore: " highscore))))

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
  (sm/start (:state game) "game-over" true))

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
        score-text (add-score game)
        highscore-text (add-highscore game)
        lives-text (add-lives game)
        bubbles (bubble/init-bubbles game on-bubble-hit on-bubble-miss
                                     on-bubble-vanish is-game-over?)]
    (reset! state-atom
            (merge initial-state
                   {:background background
                    :bubbles bubbles
                    :score-text score-text
                    :highscore-text highscore-text
                    :lives-text lives-text}))
    (set-highscore! (get-highscore))
    (bubble/start-bubbles game bubbles)))

(defn state-update [game]
  (let [{:keys [bubbles lives]} @state-atom]
    (bubble/update-bubbles game bubbles)
    (when (<= lives 0)
      (game-over! game))))

;; (defn state-shutdown [game]
;;   (let [{:keys [background
;;                 score-text
;;                 highscore-text
;;                 lives-text]} @state-atom]
;;     (sprite/destroy background)
;;     (sprite/destroy highscore-text)
;;     (sprite/destroy score-text)
;;     (sprite/destroy lives-text)
;;     (sprite/destroy background)))

(def state-obj
  {:create state-create
   :update state-update
   ;; :shutdown state-shutdown
   })
