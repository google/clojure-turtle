(ns clojure-turtle.demo
  (:require [clojure-turtle.core :as turtle :include-macros true]))

(.log js/console "Creating turtle")

(turtle/new-window {:size [320 300]})
