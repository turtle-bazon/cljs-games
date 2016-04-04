(ns bubbles.utils
  (:require
   [phzr.core :as pcore]
   [phzr.game-object-factory :as object-factory]))

(defn set-attr! [object attr-chain value]
  (pcore/pset! (get-in object (butlast attr-chain)) (last attr-chain) value))

(defn log [obj]
  (.log js/console obj))

(def mobile? (if (aget js/window "mobile") true false))

(defn exit-app []
  (.exitApp js/exitAppExtension "" (fn [] false)))

(defn create-exit-button [game]
  (object-factory/button (:add game)
                         10 0
                         "exit-button"
                         #(exit-app)
                         nil
                         0 1 0 1))
