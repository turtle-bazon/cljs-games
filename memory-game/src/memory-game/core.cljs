(ns memroy-game.core
  (:require
   [reagent.core :as r]))

;;; easy - 4x5
;;; normal - 6x7
;;; hard - 8x9

(def *score-inc* 10)

(def *score-dec* 1)

(def game (r/atom {}))

(defn inc-score [score]
  (+ score *score-inc*))

(defn dec-score [score]
  (- score *score-dec*))

(defn open-card [game id card]
  (let [type-id (:type-id card)
        opened-cards (:opened game)
        [[ao-id ao-type-id]] (:opened game)
        already-opened (not= (:state card) :closed)
        cards-equal (= type-id ao-type-id)
        will-be-able-to-open (or (not ao-id) cards-equal)]
    (if (or (not (:can-open game))
            already-opened)
      (-> game
          (assoc-in [:state-changed] false))
      (-> game
          (assoc-in [:state-changed] true)
          (update-in [:board id] assoc :state :opened)
          (update-in [:opened] (if cards-equal empty (fn [col] (conj col [id type-id]))))
          (update-in [:opened-count] (if cards-equal inc identity))
          (assoc-in [:can-open] will-be-able-to-open)
          (update-in [:score] (cond
                                cards-equal inc-score
                                ao-id dec-score
                                :else identity))))))

(defn check-game-finished [{:keys [w h opened-count] :as game}]
  (if (= opened-count (/ (* w h) 2))
    (assoc game :state :finished)
    game))

(defn close-not-matched [game]
  (let [[[o-id1 _] [o-id2 _]] (:opened game)]
    (-> game
        (assoc-in [:state-changed] true)
        (update-in [:board o-id1] assoc :state :closed)
        (update-in [:board o-id2] assoc :state :closed)
        (update-in [:opened] empty)
        (assoc-in [:can-open] true))))

(defn card-click [game id card]
  (-> game
      (open-card id card)
      check-game-finished))

(defn card-click! [id card]
  (let [new-game (swap! game card-click id card)]
    (when (and (:state-changed new-game) (not (:can-open new-game)))
      (js/setTimeout #(swap! game close-not-matched) 1000))))

(defn card-component [id card]
  [:div.card {:class (if (= (:state card) :closed)
                       "closed"
                       (str "type" (:type-id card)))
              :card-id id
              :on-click #(card-click! id card)}])

(defn row-component [row]
  [:div.board-row
   (for [[id card] row]
     ^{:key id} [card-component id card])])

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
    [:div.board.col-md-5
     (for [[row row-index] (map vector cards-array (range 0 h))]
       ^{:key (str "r" row-index)}
       [row-component row])]))

(defn score-component [score]
  [:div (str "Score: " score)])

(defn final-score-component [score]
  [:div [:strong (str "Finished! Gratz! Final score: " score)]])

(defn playing-state-component [state]
  [:div "Playing"])

(defn game-state-component [state score]
  [:div (if (= :finished state)
          [final-score-component score]
          [playing-state-component state])])

(declare start)

(defn game-component []
  (let [cur-game @game]
    [:div.container.container-table
     [:div.row
      [:div.col-md-3.col-md-offset-2
       [:div "Restart with level:"]
       [:button.btn.btn-xs.btn-default
        {:type "button"
         :on-click #(start :easy)}
        "Easy"]
       [:button.btn.btn-xs.btn-default
        {:type "button"
         :on-click #(start :normal)}
        "Normal"]
       [:button.btn.btn-xs.btn-default
        {:type "button"
         :on-click #(start :hard)}
        "Hard"]
       [score-component (:score cur-game)]
       [game-state-component (:state cur-game) (:score cur-game)]
       [:p "Rules: " "Open two matched cards and they will stay opened. "
        "Otherwise it will be closed, but you memorize it's locations."]
       [:p "Author:" [:a {:href "https://bitbucket.org/turtle_bazon/"} "Azamat S. Kalimoulline"]]
       [:p [:a {:href "https://bitbucket.org/turtle_bazon/cljs-games/src/"} "sources"]]]
      [board-component]]]))

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
        (assoc-in [:level] level)
        (assoc-in [:state-changed] true)
        (assoc-in [:w] w)
        (assoc-in [:h] h)
        (assoc-in [:can-open] true)
        (assoc-in [:board] board)
        (assoc-in [:opened] [])
        (assoc-in [:opened-count] 0)
        (assoc-in [:score] 0)
        (assoc-in [:state] :playing))))

(defn start [level]
  (swap! game new-game level)
  (r/render-component [game-component] (.getElementById js/document "app")))

(set! (.-onload js/window) #(start :normal))
