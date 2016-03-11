(ns bubbles.utils
  (:require
   [phzr.core :as pcore]))

(defn set-attr! [object attr-chain value]
  (pcore/pset! (get-in object (butlast attr-chain)) (last attr-chain) value))

(defn log [obj]
  (.log js/console obj))

(def mobile? (aget js/window "mobile") true false)
