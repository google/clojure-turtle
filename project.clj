(defproject com.google/clojure-turtle "0.3.0-SNAPSHOT"
  :description "A Clojure library that implements the Logo programming language in a Clojure context"
  :url "https://github.com/google/clojure-turtle"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :scm {:name "git"
        :url "https://github.com/google/clojure-turtle"}
  :repositories [["releases" {:url "https://clojars.org/repo/"}]]
  :deploy-repositories [["clojars" {:creds :gpg}]]
  :pom-addition [:developers [:developer
                               [:name "Elango Cheran"]
                               [:email "elango@google.com"]
                               [:timezone "-8"]]]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [quil "2.2.6"]]

  :profiles {:dev {:plugins [[lein-figwheel "0.5.0-6"]
                             [lein-cljsbuild "1.1.2"]]
                   :resource-paths ["demo/public"]
                   :cljsbuild
                   {:builds
                    [{:id "dev"
                      :source-paths ["src" "demo/src"]
                      :figwheel {}
                      :compiler {:main "clojure-turtle.demo"
                                 :source-map true
                                 :source-map-timestamp true
                                 :optimizations :none
                                 :output-to "demo/public/js/main.js"
                                 :output-dir "demo/public/js/out"
                                 :asset-path "js/out"}}]}}}

  :figwheel {:http-server-root ""
             :repl true}

  :jar-exclusions [#"\.cljx|\.swp|\.swo|\.DS_Store"]) 
