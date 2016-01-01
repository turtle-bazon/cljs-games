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
  [:div.card {:class (if (= (:state card) :closed)
                       "closed"
                       (str "type" (:type-id card)))
              :card-id id
              :on-click #(open-card! id card)}])

(defn row-component [row]
  [:div.board-row
   (for [[id card] row]
     (do 
       ^{:key id} [card-component id card]))])

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
    ;; 1. Здесь deref, а значит вызывается при каждом изменении.
    ;; Поэтому ВСЕ div.row "рисуются" (как минимум выполняется for по картам), хотя не все из них изменились.
    ;; Вынес в row-copmonent, чтобы вызывалась когда надо.
    ;; 2. Ключом к row-component был row - а он каждый раз во-первых новый объект, во вторых row с изменной картой не равен прежнему row
    ;; даже с "глубоким" сравнением. А значит это новая строка, и карты для нее рисуются новые.
    [:div.board
     (for [row-index (range 0 (count cards-array))
           :let [row (nth cards-array row-index)]]
       ^{:key (str "r" row-index)}
       [row-component row])]))

(defn counter-component [game]
  ;; если сюда передать opened-count вместо game, то будет вызываться только при изменении opened-count
  ;; сейчас вызывается при каждом изменении game
  [:div (str "Opened: " (:opened-count game))])

(defn game-component []
  ;; Это вызывается при каждом изменении game, поэтому все возможные вычисления оставлять компонентам,
  ;; которые могут не вызваться. Тут норм.
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
