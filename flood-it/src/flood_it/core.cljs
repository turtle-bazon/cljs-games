(ns flood-it.core
  (:require [enfocus.core :as ef])
  (:require-macros [enfocus.macros :as em]))

(def colors ["yellow" "orange" "red" "green" "blue" "pink"])
(def field (atom nil))

(defn create-field
  [width height max-turnes-count table]
  {:width width :height height :turnes-left max-turnes-count :table table})

(defn get-field-width
  [field]
  (:width field))

(defn get-field-height
  [field]
  (:height field))

(defn get-field-turnes-left
  [field]
  (:turnes-left field))

(defn get-cell-at
  [field x y]
  ((:table field) [x y]))

(defn get-current-color
  [field]
  (get-in field [:table [0 0]]))

(defn set-info
  [info]
  (ef/at "#info" (ef/content info)))

(defn draw-cell
  [context cell x y width height]
  (set! (.-fillStyle context) (colors cell))
  (.fillRect context (* x width) (* y height) width height))

(defn draw-field
  [field]
  (let [surface (.getElementById js/document "canvas")
        context (.getContext surface "2d")
        cell-width (/ (.-width surface) (get-field-width field))
        cell-height (/ (.-height surface) (get-field-height field))]
    (set-info (str "turns left " (get-field-turnes-left field)))
    (doseq [y (range 0 (get-field-height field))
            x (range 0 (get-field-width field))
            :let [cell (get-cell-at field x y)]]
      (draw-cell context cell x y cell-width cell-height))))

(defn- update-field
  [field x y old-color new-color processed]
  (if (or (contains? processed [x y])
          (not (<= 0 x (dec (get-field-width field))))
          (not (<= 0 y (dec (get-field-height field)))))
    {:field field :processed processed}
    (let [cell (get-cell-at field x y)]
      (if (and (not= cell old-color)
               (not= cell new-color))
        {:field field :processed processed}
        (let [next-coords [[(dec x) y] [x (dec y)] [(inc x) y] [x (inc y)]]
              new-processed (conj processed [x y])]
          (reduce (fn [result coords]
                    (update-field (:field result) (get coords 0) (get coords 1)
                                  cell new-color (:processed result)))
                  (if (= cell old-color)
                    {:field (assoc-in field [:table [x y]] new-color)
                     :processed new-processed}
                    {:field field
                     :processed processed})
                  next-coords))))))

(defn get-game-state
  [field]
  (let [color (get-current-color field)]
    (if (every? #(= % color) (vals (:table field)))
      :win
      (if (> (get-field-turnes-left field) 0)
        :in-process
        :loss))))

(defn update-game-state
  [field]
  (let [state (get-game-state field)]
    (cond (= state :win) (set-info (str "Win! (turnes left " (get-field-turnes-left field) ")"))
          (= state :loss) (set-info "Loss :(")
          :else nil)))

(defn ^:export set-color
  [color]
  (when (= (get-game-state @field) :in-process)
    (reset! field (:field (update-field @field 0 0 (get-current-color @field) color #{})))
    (swap! field update-in [:turnes-left] dec)
    (draw-field @field)
    (update-game-state @field)))

(defn generate-field
  [width height max-turnes-count]
  (create-field
   width height max-turnes-count
   (into {}
         (for [y (range 0 height)
               x (range 0 width)]
           [[x y] (rand-int 6)]))))

(defn start []
  (reset! field (generate-field 14 14 25))
  (draw-field @field))

(set! (.-onload js/window) #(em/wait-for-load (start)))
