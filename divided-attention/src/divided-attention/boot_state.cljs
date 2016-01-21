(ns divided-attention.boot-state
  (:require
   [phzr.state-manager :as sm]))

(defn state-create [state]
  (.log js/console "boot state created")
  (sm/start (:state state) "play" nil))

(def state-obj
  {:create state-create})
