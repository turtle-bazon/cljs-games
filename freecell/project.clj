(defproject freecell "0.1.0-SNAPSHOT"
  :description "Freecell Solitaire"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [reagent "0.5.1"]]
  :plugins [[lein-cljsbuild "1.1.2"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.nrepl "0.2.12"]
                                  [org.clojure/tools.reader "0.10.0"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [figwheel-sidecar "0.5.0-3"]]
                   :plugins [[lein-figwheel "0.5.0-3"]
                             [cider/cider-nrepl "0.10.1"]
                             [refactor-nrepl "1.2.0"]]}}
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :clean-targets ^{:protect false} ["resources/public/js" :target]
  :cljsbuild {:builds 
              [{:id "dev"
                :source-paths ["src"]
                :figwheel true
                :compiler {:main freecell.core
                           :asset-path "js/out"
                           :output-to "resources/public/js/main.js"
                           :output-dir "resources/public/js/out"}}
               {:id "prod"
                :source-paths sources
                :compiler {:output-to "resources/public/js/main.js"
                           :optimizations :advanced
                           :pretty-print false}}]}
  :figwheel {:css-dirs ["resources/public/css"]}
  :min-lein-version "2.1.2")
