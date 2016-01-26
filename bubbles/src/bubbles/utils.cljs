(ns bubbles.utils
  (:require
   [phzr.core :as pcore]))

(defn set-attr! [object attr-chain value]
  (pcore/pset! (get-in object (butlast attr-chain)) (last attr-chain) value))
