(ns memroy-game.core
  (:require
   [reagent.core :as r]))

;;; easy - 4x5
;;; normal - 6x7
;;; hard - 8x9

(def game (r/atom {}))

(defn card-component [card]
  [:div.card {:type (if (= (:state card) :closed)
                      "closed"
                      (:type-id card))}])

(defn board-component []
  (let [cur-game @game]
    [:div.board
     (for [row (-> cur-game :board)]
       [:div.board-row
        (for [card row]
          [card-component card])])]))

(defn game-component []
  (let [cur-game @game]
    [:div
     [board-component]]))

(defn new-game [game' level]
  (let [[w h] (case level
                :easy [4 5]
                :normal [6 7]
                :hard [8 9])
        total-cards (* w h)
        ids-to-play (range 0 (/ total-cards 2))
        board (vec (map vec (partition
                             w
                             (for [id (shuffle (concat ids-to-play ids-to-play))]
                               {:type-id id
                                :state :closed}))))]
    (-> game'
        (assoc-in [:board] board))))

(defn start [level]
  (swap! game new-game level)
  (r/render-component [game-component]
                      (.-body js/document)))

(set! (.-onload js/window) #(start :normal))

