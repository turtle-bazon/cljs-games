(defproject flood-it "1.0"
  :description "Flood it game"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [enfocus "2.1.1"]]
  :plugins [[lein-cljsbuild "1.1.2"]]
  :cljsbuild
  {:builds
   [{:source-paths ["src"]
     :compiler {:output-to "resources/public/js/main.js"
                :optimizations :whitespace
                :pretty-print true}}
    {:id "prod"
     :source-paths ["src"]
     :compiler {:output-to "resources/public/js/main.js"
                :optimizations :advanced
                :pretty-print false}}]})
