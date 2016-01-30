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
(def bubble-create-interval-factor 0.98)

(defn add-score [game]
  (object-factory/text (:add game) 64 64 (str "Score: " @score-atom)
                       {:font "32px Arial",
                        :fill "#ff0044",
                        :align "center"}))

(defn show-score [score-text]
  (utils/set-attr! score-text [:text] (str "Score: " @score-atom)))

(defn add-lives [game]
  (object-factory/text (:add game) 256 64 (str "Lives: " @lives-atom)
                       {:font "32px Arial",
                        :fill "#ff0044",
                        :align "center"}))

(defn show-lives [lives-text]
  (utils/set-attr! lives-text [:text] (str "Lives: " @lives-atom)))

(defn add-bubble [game group bubble-vanish-sound]
  (when (< 0 @lives-atom)
    (let [bubble (bubble/add-random-bubble game group bubble-vanish-sound
                                           (fn [bubble hit]
                                             (if hit
                                               (swap! score-atom inc)
                                               (swap! lives-atom dec))))]
      (bubble/bubble-up bubble)
      (timer/add (get-in game [:time :events])
                 @bubble-create-interval
                 (fn [] (add-bubble game group bubble-vanish-sound)) nil nil)
      (swap! bubble-create-interval (fn [t] (* t bubble-create-interval-factor))))))

(defn state-create [game]
  (let [background (bubble/add-background game (fn [background event]
                                                 (swap! lives-atom dec)))
        music (object-factory/audio (:add game) "music")
        bubble-vanish-sound (object-factory/audio (:add game) "bubble-vanish-sound")
        bubbles-group (object-factory/physics-group (:add game))
        score-text (add-score game)
        lives-text (add-lives game)]
    (sound/loop-full music)
    (reset! score-atom 0)
    (reset! lives-atom 10)
    (reset! bubble-create-interval 800)
    (reset! state-atom
            (-> {}
                (assoc-in [:bubbles-group] bubbles-group)
                (assoc-in [:score-text] score-text)
                (assoc-in [:lives-text] lives-text)))
    (add-bubble game bubbles-group bubble-vanish-sound)))

(defn state-update [game]
  (let [{:keys [bubbles-group score-text lives-text]} @state-atom]
    (doall (map (fn [bubble]
                  (bubble/bubble-update game bubble
                                        (fn []
                                          (swap! lives-atom dec))))
                (:children bubbles-group)))
    (show-score score-text)
    (show-lives lives-text)))

(def state-obj
  {:create state-create
   :update (fn [game] (state-update game))})
