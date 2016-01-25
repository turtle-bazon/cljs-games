(ns divided-attention.play-state)

(defn state-create [state]
  (.log js/console "play state created"))

(def state-obj
  {:create state-create})
