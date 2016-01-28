(ns bubbles.play-state
  (:require
   [phzr.animation-manager :as animation-manager]
   [phzr.game-object-factory :as object-factory]
   [phzr.group :as group]
   [phzr.physics.arcade :as arcade-physics]
   [bubbles.bubble :as bubble]
   [bubbles.utils :as utils]))

(defonce state-atom (atom {}))

(defonce score-atom (atom 0))
(defonce lives-atom (atom 10))
(defonce bubble-create-interval (atom 1000))

(defn add-score [game]
  (object-factory/text (:add game) 64 64 (str "Score: " @score-atom)
                       {:font "32px Arial",
                        :fill "#ff0044",
                        :align "center"}))

(defn show-score [score-text]
  (.setText score-text (str "Score: " @score-atom)))

(defn add-lives [game]
  (object-factory/text (:add game) 256 64 (str "Lives: " @lives-atom)
                       {:font "32px Arial",
                        :fill "#ff0044",
                        :align "center"}))

(defn show-lives [lives-text]
  (.setText lives-text (str "Lives: " @lives-atom)))

(defn add-bubble [game group]
  (when (< 0 @lives-atom)
    (let [bubble (bubble/add-random-bubble game group
                                           (fn [bubble]
                                             (swap! score-atom inc)))]
      (bubble/bubble-up bubble)
      (.add (.. game -time -events)
            @bubble-create-interval
            (fn [] (add-bubble game group)) nil)
      (swap! bubble-create-interval (fn [t] (* t 0.95))))))

(defn state-create [game]
  (let [bubbles-group (object-factory/physics-group (:add game))
        score-text (add-score game)
        lives-text (add-lives game)]
    (add-bubble game bubbles-group)
    (swap! state-atom
           (fn [state]
             (-> state
                 (assoc-in [:bubbles-group] bubbles-group)
                 (assoc-in [:score-text] score-text)
                 (assoc-in [:lives-text] lives-text))))))

(defn state-update [game]
  (let [{:keys [bubbles-group score-text lives-text]} @state-atom]
    (doall (map (fn [bubble]
                  (bubble/bubble-update bubble
                                        (fn []
                                          (swap! lives-atom dec))))
                (.-children bubbles-group)))
    (show-score score-text)
    (show-lives lives-text)))

(def state-obj
  {:create state-create
   :update (fn [game] (state-update game))})
