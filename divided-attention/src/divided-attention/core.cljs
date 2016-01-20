(ns divided-attention.core)

(defn start []
  (.log js/console "yes!"))

(set! (.-onload js/window) start)
