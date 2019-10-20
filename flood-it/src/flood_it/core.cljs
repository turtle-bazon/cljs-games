(ns flood-it.core
  (:require [enfocus.core :as ef])
  (:require-macros [enfocus.macros :as em]))

(def colors ["yellow" "orange" "red" "green" "blue" "pink"])
(def field (atom nil))

(defn cell-at [field x y]
  ((:table field) [x y]))

(defn current-color [field]
  (get-in field [:table [0 0]]))

(defn set-info [info]
  (ef/at "#info" (ef/content info)))

(defn draw-cell [context cell x y width height]
  (set! (.-fillStyle context) (colors cell))
  (.fillRect context (* x width) (* y height) width height))

(defn draw-field [field]
  (let [surface (.getElementById js/document "canvas")
        context (.getContext surface "2d")
        cell-width (/ (.-width surface) (:width field))
        cell-height (/ (.-height surface) (:height field))]
    (set-info (str "turns left " (:turns-left field)))
    (.clearRect context 0 0 (.-width surface) (.-height surface))
    (doseq [y (range 0 (:height field))
            x (range 0 (:width field))
            :let [cell (cell-at field x y)]]
      (draw-cell context cell x y cell-width cell-height))))

(defn- update-field [field x y old-color new-color processed]
  (if (or (contains? processed [x y])
          (not (<= 0 x (dec (:width field))))
          (not (<= 0 y (dec (:height field)))))
    {:field field :processed processed}
    (let [cell (cell-at field x y)]
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

(defn get-game-state [field]
  (let [color (current-color field)]
    (if (every? #(= % color) (vals (:table field)))
      :win
      (if (> (:turns-left field) 0)
        :in-process
        :loss))))

(defn update-game-state [field]
  (let [state (get-game-state field)]
    (cond (= state :win) (set-info (str "Win! (turns left " (:turns-left field) ")"))
          (= state :loss) (set-info "Loss :(")
          :else nil)))

(defn ^:export set-color [color]
  (when (and (= (get-game-state @field) :in-process)
             (not= color (current-color @field)))
    (reset! field (:field (update-field @field 0 0 (current-color @field) color #{})))
    (swap! field update-in [:turns-left] dec)
    (draw-field @field)
    (update-game-state @field)))

(defn generate-field [width height max-turns-count]
  {:width      width
   :height     height
   :turns-left max-turns-count
   :table      (into {}
                     (for [y (range 0 height)
                           x (range 0 width)]
                       [[x y] (rand-int 6)]))})

(defn ^:export start []
  (reset! field (generate-field 14 14 25))
  (draw-field @field))

(set! (.-onload js/window) #(em/wait-for-load (start)))
