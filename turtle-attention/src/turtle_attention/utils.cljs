(ns turtle-attention.utils
  (:require
   [phzr.core :as pcore]))

(defn log [object]
  (.log js/console object))

(defn set-attr! [object attr-chain value]
  (pcore/pset! (get-in object (butlast attr-chain)) (last attr-chain) value))

(defn elapsed [game]
  (get-in game [:time :physics-elapsed-ms]))
