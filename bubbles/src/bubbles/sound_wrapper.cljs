(ns bubbles.sound-wrapper
  (:require
   [phzr.game-object-factory :as object-factory]
   [phzr.loader :as loader]
   [phzr.sound :as sound]
   [bubbles.utils :as utils :refer [cordova?]]))

(def sounds-atom (atom {}))

(defn get-sound-url [path]
  (if (= (.toLowerCase (.-platform js/device)) "android")
    (str "/android_asset/www/" path)
    path))

(defn load-sound-cordova [game key path]
  (swap! sounds-atom assoc key (get-sound-url path)))

(defn load-sound-phaser [game key path]
  (loader/audio game key path))

(defn load-sound [game key path]
  ((if (cordova?)
     load-sound-cordova
     load-sound-phaser)
   game key path))


(defn get-sound-cordova [game key]
  (js/Media. (get @sounds-atom key)))

(defn get-sound-phaser [game key]
  (object-factory/audio (:add game) key))

(defn get-sound [game key]
  ((if (cordova?)
     get-sound-cordova
     get-sound-phaser)
   game key))


(defn play-cordova [sound]
  (.play sound))

(defn play-phaser [sound]
  (sound/play sound))

(defn play [sound]
  ((if (cordova?)
     play-cordova
     play-phaser)
   sound))

(defn loop-sound-cordova [sound interval]
  (.play sound)
  (let [restart (fn []
                  (.stop sound)
                  (.play sound))]
    (.setInterval js/window restart interval)))

(defn loop-sound-phaser [sound interval]
  (sound/loop-full sound))

(defn loop-sound [sound interval]
  ((if (cordova?)
     loop-sound-cordova
     loop-sound-phaser)
   sound interval))
