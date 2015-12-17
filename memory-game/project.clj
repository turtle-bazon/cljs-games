(defproject memory-game "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://exampl.com/FIXME"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]]
  :plugins [[lein-cljsbuild "1.1.0"]]
  :cljsbuild {:builds
              [{:source-paths ["src"]
                :compiler {:output-to "resources/public/js/main.js"
                           :optimizations :whitespace
                           :pretty-print true}}]}
  :min-lein-version "2.1.2")
