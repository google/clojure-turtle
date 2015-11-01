(defproject com.google/clojure-turtle "0.1.1"
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
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [quil "2.2.5"]]
  :jar-exclusions [#"\.cljx|\.swp|\.swo|\.DS_Store"]) 
