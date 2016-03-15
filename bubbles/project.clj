(defproject bubbles "0.1.0-SNAPSHOT"
  :description "Bubbles game"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [phzr "0.1.0-SNAPSHOT" :classifier "bazon"]]
  :plugins [[lein-cljsbuild "1.1.2"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.nrepl "0.2.12"]
                                  [org.clojure/tools.reader "0.10.0"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [figwheel-sidecar "0.5.0-3"]]
                   :plugins [[cider/cider-nrepl "0.10.2"]
                             [refactor-nrepl "2.0.0"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]
                                  :timeout 120000}}}
  :figwheel {:server-ip "0.0.0.0"
             :css-dirs ["resources/public/css"]}
  :cljsbuild {:builds
              [{:id "dev"
                :figwheel {:websocket-host :js-client-host}
                :source-paths ["src"]
                :compiler {:main bubbles.core
                           :asset-path "js/out"
                           :output-to "resources/public/js/main.js"}}
               {:id "prod"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/main.js"
                           :main bubbles.core
                           :optimizations :advanced
                           :pretty-print false
                           :externs ["externs/bubbles.js"]}}]}
  :min-lein-version "2.5.3")

