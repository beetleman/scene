(defproject scene "0.1.0-SNAPSHOT"
  :description "SCENE - Smart Contract Events listeNEr"
  :url "https://github.com/beetleman/scene"
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :dependencies [[bidi "2.1.2"]
                 [com.cemerick/piggieback "0.2.2"]
                 [com.taoensso/timbre "4.10.0"]
                 [funcool/promesa "1.9.0"]
                 [macchiato/core "0.2.2"]
                 [macchiato/env "0.0.6"]
                 [macchiato/hiccups "0.4.1"]
                 [mount "0.1.11"]
                 [org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [org.clojure/core.async "0.3.443"]
                 [org.clojure/test.check "0.10.0-alpha2"]
                 ;; for cider repl
                 [org.clojure/tools.nrepl "0.2.12" :exclusions [org.clojure/clojure]]]
  :min-lein-version "2.0.0"
  :local-repo "./.m2"
  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :plugins [[lein-doo "0.1.8"]
            [lein-ancient "0.6.12"]
            [macchiato/lein-npm "0.6.3"]
            [lein-figwheel "0.5.14"]
            [lein-cljsbuild "1.1.7"]
            ;; for cider repl
            [refactor-nrepl "2.4.0-SNAPSHOT"]
            [cider/cider-nrepl "0.17.0-SNAPSHOT"]]
  :npm {:dependencies       [[mongodb "3.0.1"]
                             [source-map-support "0.4.6"]
                             [web3 "0.20.3"]]
        :write-package-json true}
  :source-paths ["src" "target/classes"]
  :clean-targets ["target"]
  :target-path "target"
  :profiles
  {:dev
   {:npm          {:package {:main    "target/out/scene.js"
                             :scripts {:start "node target/out/scene.js"}}}
    :dependencies [[figwheel-sidecar "0.5.14"]]
    :cljsbuild
    {:builds {:dev
              {:source-paths ["env/dev" "src"]
               :figwheel     true
               :compiler     {:main                 scene.app
                              :output-to            "target/out/scene.js"
                              :output-dir           "target/out"
                              :target               :nodejs
                              :optimizations        :none
                              :pretty-print         true
                              :source-map           true
                              :source-map-timestamp false}}}}
    :figwheel
    {:http-server-root "public"
     :reload-clj-files {:clj false :cljc true}
     :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
    :source-paths ["env/dev"]
    :repl-options {:init-ns user}}
   :test
   {:cljsbuild
    {:builds
     {:test
      {:source-paths ["env/test" "src" "test"]
       :compiler     {:main          scene.runner
                      :output-to     "target/test/scene.js"
                      :target        :nodejs
                      :optimizations :none
                      :pretty-print  true
                      :source-map    true}}}}
    :doo {:build "test"}}
   :release
   {:npm {:package {:main    "scene.js"
                    :scripts {:start "node scene.js"}}
          :root    "release"}
    :cljsbuild
    {:builds
     {:release
      {:source-paths ["env/prod" "src"]
       :compiler     {:main          scene.app
                      :output-to     "release/scene.js"
                      :language-in   :ecmascript5
                      :target        :nodejs
                      :optimizations :simple
                      :pretty-print  false}}}}}}
  :repl-options
  {:host     "0.0.0.0"
   :port     7000
   :headless true}

  :aliases
  {"build"      ["do"
                 ["clean"]
                 ["npm" "install"]
                 ["figwheel" "dev"]]
   "repl-dev"   ["do"
                 ["clean"]
                 ["npm" "install"]
                 ["repl" ":headless"]]
   "release"    ["do"
                 ["clean"]
                 ["npm" "install"]
                 ["with-profile" "release" "npm" "init" "-y"]
                 ["with-profile" "release" "cljsbuild" "once"]]
   "test"       ["do"
                 ["npm" "install"]
                 ["with-profile" "test" "doo" "node" "once"]]
   "test-watch" ["do"
                 ["npm" "install"]
                 ["with-profile" "test" "doo" "node"]]})
