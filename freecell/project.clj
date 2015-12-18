(defproject freecell "0.1.0-SNAPSHOT"
  :description "Freecell Solitaire"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [enfocus "2.1.1"]]
  :plugins [[lein-cljsbuild "1.1.2"]]
  :cljsbuild
  {:builds
   [{:source-paths ["src"],
     :compiler
     {:optimizations :whitespace,
      :output-to "resources/js/main.js",
      :pretty-print true}}]})
