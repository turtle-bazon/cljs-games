(ns bubbles.utils
  (:require
   [phzr.core :as pcore]
   [phzr.game-object-factory :as object-factory]))

(defn set-attr! [object attr-chain value]
  (pcore/pset! (get-in object (butlast attr-chain)) (last attr-chain) value))

(defn log [obj]
  (.log js/console obj))

(def mobile? (if (aget js/window "mobile") true false))

(def environment
  (if (aget js/window "mobile")
    {:display :mobile
     :use :app}
    {:display :mobile
     :use :browser}))

(comment {:display :desktop
          :use :browser})
