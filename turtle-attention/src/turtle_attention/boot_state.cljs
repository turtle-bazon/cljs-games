(ns turtle-attention.boot-state
  (:require
   [phzr.loader :as loader]
   [phzr.physics :as physics]
   [phzr.state-manager :as sm]))

(defn state-preload [game]
  (doto (:load game)
    (loader/spritesheet "berrybox" "assets/world/berrybox.png" 64 64)
    (loader/atlas "turtle" "assets/characters/turtle.png" "assets/characters/turtle.json")
    (loader/spritesheet "crab" "assets/characters/crab.png" 120 64)
    (loader/spritesheet "crocodile" "assets/characters/turtle.png" 100 64)))

(defn state-create [game]
  (physics/start-system (:physics game) (physics/const :arcade))
  (sm/start (:state game) "play" nil))

(def state-obj
  {:preload state-preload
   :create state-create})
