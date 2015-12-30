(ns memroy-game.core
  (:require
   [reagent.core :as r]))

;;; easy - 4x5
;;; normal - 6x7
;;; hard - 8x9

(def game (r/atom {}))

(def hz (atom 0))

(defn open-card! [card]
  (js/alert (str card)))

(defn card-component [id card]
  [:div.card {:type (if (= (:state card) :closed)
                      "closed"
                      (:type-id card))
              :card-id id
              :on-click #(open-card! card)}])

(defn board-component []
  (let [cur-game @game
        w (:w cur-game)
        h (:h cur-game)]
    [:div.board
     (for [row (range 0 h)]
       [:div.board-row
        (for [col (range 0 w)]
          (let [id (+ (* row w) col)]
            ^{:key id} [card-component id nil]))])]))

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
        board (vec (for [id (shuffle (concat ids-to-play ids-to-play))]
                     {:type-id id
                      :state :closed}))]
    (-> game'
        (assoc-in [:w] w)
        (assoc-in [:h] h)
        (assoc-in [:board] board))))

(defn start [level]
  (swap! game new-game level)
  (r/render-component [game-component]
                      (.-body js/document)))

(set! (.-onload js/window) #(start :normal))

