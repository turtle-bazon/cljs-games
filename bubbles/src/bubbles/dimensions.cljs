(ns bubbles.dimensions
  (:require
   [bubbles.utils :as utils :refer [log mobile?]]))

(def background (if mobile?
                  {:width 569 :height 854}
                  {:width 854 :height 569}))
(def start-button {:width 128 :height 128})
(def about-button {:width 128 :height 128})
(def restart-button {:width 128 :height 128})
(def exit-button {:width 64 :height 64})
(def fullscreen-button {:width 50 :height 50})
(def bubble {:width 96 :height 96})
