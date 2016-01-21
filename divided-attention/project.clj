(defproject divided-attention "0.1.0-SNAPSHOT"
  :description "Divided Attention Game"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [phzr "0.1.0-SNAPSHOT"]]
  :plugins [[lein-cljsbuild "1.1.2"]]
  :source-paths ["src"]
  :profiles {:dev {:dependencies [[org.clojure/tools.nrepl "0.2.12"]
                                  [org.clojure/tools.reader "0.10.0"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [figwheel-sidecar "0.5.0-3"]]
                   :plugins [[lein-figwheel "0.5.0-3"]
                             [cider/cider-nrepl "0.10.0"]
                             [refactor-nrepl "1.2.0"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}}
  :figwheel {:server-ip "0.0.0.0"}
  :cljsbuild {:builds
              [{:id "dev"
                :figwheel {:websocket-host :js-client-host}
                :source-paths ["src"]
                :compiler {:main divided-attention.core
                           :asset-path "js/out"
                           :output-to "resources/public/js/main.js"
                           :source-map-timestamp true}}
               {:id "prod"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/main.js"
                           :main divided-attention.core
                           :optimizations :advanced
                           :pretty-print false}}]}
  :min-lein-version "2.5.3")
