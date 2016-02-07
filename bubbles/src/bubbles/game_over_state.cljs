(ns bubbles.game-over-state
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

(def state-atom (atom {}))

(defn get-highscore []
  (or (.getItem js/localStorage "highscore") 0))

(defn restart-game [game]
  (sm/start (:state game) "play" true))

(defn create-restart-button [game]
  (object-factory/button (:add game)
                         368 233
                         "restart-button"
                         #(restart-game game)
                         nil
                         0 1 1))

(defn state-create [game]
  (let [restart-button (create-restart-button game)]
    (reset! state-atom {:restart-button restart-button})))

;; (defn state-shutdown [game]
;;   (let [{:keys [restart-button]} @state-atom]
;;     (sprite/destroy restart-button)))

(def state-obj
  {:create state-create
   ;; :shutdown state-shutdown
   })
