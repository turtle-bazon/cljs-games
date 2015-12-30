(ns memroy-game.core
  (:require
   [reagent.core :as r]))

;;; easy - 4x5
;;; normal - 6x7
;;; hard - 8x9

(def game (r/atom {}))

(defn open-card [game id card]
  (let [type-id (:type-id card)
        opened-cards (:opened game)
        [[ao-id ao-type-id]] (:opened game)
        already-opened (not= (:state card) :closed)
        cards-equal (= type-id ao-type-id)
        will-can-open (or (not ao-id) cards-equal)]
    (if (or (not (:can-open game))
            already-opened)
      (-> game
          (assoc-in [:state-changed] false))
      (-> game
          (assoc-in [:state-changed] true)
          (update-in [:board id] assoc :state :opened)
          (update-in [:opened] (if cards-equal empty (fn [col] (conj col [id type-id]))))
          (update-in [:opened-count] (if cards-equal inc identity))
          (assoc-in [:can-open] will-can-open)))))

(defn close-not-matched [game]
  (let [[[o-id1 _] [o-id2 _]] (:opened game)]
    (-> game
        (assoc-in [:state-changed] true)
        (update-in [:board o-id1] assoc :state :closed)
        (update-in [:board o-id2] assoc :state :closed)
        (update-in [:opened] empty)
        (assoc-in [:can-open] true))))

(defn open-card! [id card]
  (let [new-game (swap! game open-card id card)]
    (when (and (:state-changed new-game) (not (:can-open new-game)))
      (js/setTimeout #(swap! game close-not-matched) 1000))))

(defn card-component [id card]
  [:div.card {:type (if (= (:state card) :closed)
                      "closed"
                      (:type-id card))
              :card-id id
              :on-click #(open-card! id card)}])

(defn board-component []
  (let [cur-game @game
        w (:w cur-game)
        h (:h cur-game)
        cards-array (partition
                     w
                     (map (fn [id card]
                            [id card])
                          (for [row (range 0 h)
                                col (range 0 w)]
                            (+ (* w row) col))
                          (:board cur-game)))]
    [:div.board
     (for [row cards-array]
       ^{:key (str "r" row)}
       [:div.board-row
        (for [[id card] row]
          ^{:key id}
          [card-component id card])])]))

(defn counter-component [game]
  [:div (str "Opened: " (:opened-count game))])

(defn game-component []
  (let [cur-game @game]
    [:div
     [board-component]
     [counter-component cur-game]]))

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
        (assoc-in [:state-changed] true)
        (assoc-in [:w] w)
        (assoc-in [:h] h)
        (assoc-in [:can-open] true)
        (assoc-in [:board] board)
        (assoc-in [:opened] [])
        (assoc-in [:opened-count] 0))))

(defn start [level]
  (swap! game new-game level)
  (r/render-component [game-component]
                      (.-body js/document)))

(set! (.-onload js/window) #(start :normal))

