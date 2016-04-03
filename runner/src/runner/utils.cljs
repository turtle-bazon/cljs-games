(ns runner.utils
  (:require
   [phzr.core :as pcore]))

(defn set-attr! [object attr-chain value]
  (pcore/pset! (get-in object (butlast attr-chain)) (last attr-chain) value))

(defn log [obj]
  (.log js/console obj))

(def mobile? (if (aget js/window "mobile") true false))

(defn exit-app []
  (.exitApp js/exitAppExtension "" (fn [] false)))
