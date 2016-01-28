;; Copyright 2014 Google Inc. All Rights Reserved.

;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at

;;     http://www.apache.org/licenses/LICENSE-2.0

;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns clojure-turtle.core
  (:refer-clojure :exclude [repeat])
  (:require #?(:clj [quil.core :as q]
               :cljs [quil.core :as q :include-macros true])))

(defn new-turtle
  "Returns an entity that represents a turtle."
  []
  (atom {:x 0
         :y 0
         :angle 90
         :pen true}))

(def ^{:doc "The default turtle entity used when no turtle is specified for an operation."}
  turtle (new-turtle))

(def ^{:doc "The set of lines drawn in the canvas by the default turtle."}
  lines (atom []))

(defn alter-turtle
  "A helper function used in the implementation of basic operations to abstract
  out the interface of applying a function to a turtle entity."
  [turt f] 
  (swap! turt f)
  turt)

(defn right
  "Rotate the turtle turt clockwise by ang degrees."
  ([ang]
     (right turtle ang))
  ([turt ang]
     ;; the local fn add-angle will increment the angle but keep the
     ;; resulting angle in the range [0,360), in degrees.
     (letfn [(add-angle
               [{:keys [angle] :as t}]
               (let [new-angle (-> angle
                                   (- ang)
                                   (mod 360))]
                 (assoc t :angle new-angle)))]
       (alter-turtle turt add-angle))))

(defn left
  "Same as right, but turns the turtle counter-clockwise."
  ([ang]
     (right (* -1 ang)))
  ([turt ang]
     (right turt (* -1 ang))))

(defn translate
  "Move the turtle t horizontally by length dx and vertically by length dy."
  [{:keys [x y pen] :as t} dx dy]
  (let [new-x (+ x dx)
        new-y (+ y dy)
        line [[x y] [new-x new-y]]]
    ;; translate is used by forward/back to draw the next movement
    (when (and pen
               (not= [x y] [new-x new-y]))
      (swap! lines conj line))
    (assoc t
      :x new-x
      :y new-y)))

(def deg->radians q/radians)

(def radians->deg q/degrees)

(def atan q/atan)

(defn forward
  "Move the turtle turt forward in the direction that it is facing by length len."
  ([len]
     (forward turtle len))
  ([turt len]
     ;; Convert the turtle's polar coordinates (angle + radius) into
     ;; Cartesian coordinates (x,y) for display purposes
     (let [rads (deg->radians (get @turt :angle))
           dx (* len (Math/cos rads))
           dy (* len (Math/sin rads))
           alter-fn #(translate % dx dy)] 
       (alter-turtle turt alter-fn))))

(defn back
  "Same as forward, but move the turtle backwards, which is opposite of the direction it is facing."
  ([len]
     (forward (* -1 len)))
  ([turt len]
     (forward turt (* -1 len))))

(defn penup
  "Instruct the turtle to pick its pen up. Subsequent movements will not draw to screen until the pen is put down again."
  ([]
     (penup turtle))
  ([turt]
     (letfn [(alter-fn [t] (assoc t :pen false))]
       (alter-turtle turt alter-fn))))

(defn pendown
  "Instruct the turtle to put its pen down. Subsequent movements will draw to screen."
  ([]
     (pendown turtle))
  ([turt]
     (letfn [(alter-fn [t] (assoc t :pen true))]
       (alter-turtle turt alter-fn))))

(defn draw-turtle
  "A helper function that draws the triangle that represents the turtle onto the screen."
  ([]
     (draw-turtle turtle))
  ([turt]
     (let [short-leg 5
           long-leg 12
           hypoteneuse (Math/sqrt (+ (* short-leg short-leg)
                                     (* long-leg long-leg)))
           large-angle  (-> (/ long-leg short-leg)
                            atan
                            radians->deg)
           small-angle (- 90 large-angle)
           pen-down? (get-in @turt [:pen])
           turt-copy (atom (assoc @turt :pen false))
           turt-copy-points (atom [])]
       (letfn [(record-turt-point
                 [t]
                 (let [new-x (get @t :x)
                       new-y (get @t :y)
                       new-point [new-x new-y]]
                   (swap! turt-copy-points conj new-point))
                 t)]
         (do
           (-> turt-copy
               record-turt-point
               (right 90)
               (forward short-leg)
               record-turt-point
               (left (- 180 large-angle))
               (forward hypoteneuse)
               record-turt-point
               (left (- 180 (* 2 small-angle)))
               (forward hypoteneuse)
               record-turt-point
               (left (- 180 large-angle))
               (forward short-leg)
               record-turt-point
               (left 90)))
         (let [lines (partition 2 1 @turt-copy-points)]
           (dorun
            (map (fn [line] (apply q/line (flatten line))) lines)))))))

(defmacro all
  "This macro was created to substitute for the purpose served by the square brackets in Logo
  in a call to REPEAT.  This macro returns a no-argument function that, when invoked, executes
  the commands described in the body inside the macro call/form.
  (Haskell programmers refer to the type of function returned a 'thunk'.)"
  [& body]
  `(fn []
     (do
       ~@ body)))

(defmacro repeat
  "A macro to translate the purpose of the Logo REPEAT function."
  [n & body] 
  `(let [states# (repeatedly ~n ~@body)]
     (dorun
      states#)
     (last states#)))

(defn clean
  "Clear the lines state, which effectively clears the drawing canvas."
  ([]
     (clean lines))
  ([lines]
     (letfn [(alter-fn [ls] [])]
       (swap! lines alter-fn))))

(defn setxy
  "Set the position of turtle turt to x-coordinate x and y-coordinate y."
  ([x y]
     (setxy turtle x y))
  ([turt x y]
     (let [pen-down? (get @turt :pen)]
       (letfn [(alter-fn [t] 
                 (-> t
                     (assoc :x x)
                     (assoc :y y)))]
         (penup turt)
         (alter-turtle turt alter-fn)
         (when pen-down?
           (pendown turt))
         turt))))

(defn setheading
  "Set the direction which the turtle is facing, given in degrees, where 0 is to the right,
  90 is up, 180 is left, and 270 is down."
  ([ang]
     (setheading turtle ang))
  ([turt ang]
     (letfn [(alter-fn [t] (-> t
                               (assoc :angle ang)))]
       (alter-turtle turt alter-fn))))

(defn home
  "Set the turtle at coordinates (0,0), facing up (heading = 90 degrees)"
  ([]
     (home turtle))
  ([turt] 
     (setxy turt 0 0) 
     (setheading turt 90)))

(defn reset-rendering
  "A helper function for the Quil rendering function."
  []
  (q/background 200)                 ;; Set the background colour to
                                     ;; a nice shade of grey.
  (q/stroke-weight 1))

(defn setup
  "A helper function for the Quil rendering function."
  []
  (q/smooth)                          ;; Turn on anti-aliasing
  ;; Allow q/* functions to be used from the REPL
  #?(:cljs
     (js/setTimeout #(set! quil.sketch/*applet* (q/get-sketch-by-id "turtle-canvas")) 5))
  (reset-rendering))

(defn draw
  "The function passed to Quil for doing rendering."
  []
  ;; Use push-matrix to apply a transformation to the graphing plane.
  (q/push-matrix)
  ;; By default, positive x is to the right, positive y is down.
  ;; Here, we tell Quil to move the origin (0,0) to the center of the window.
  (q/translate (/ (q/width) 2) (/ (q/height) 2))
  (reset-rendering)
  ;; Apply another transformation to the canvas.
  (q/push-matrix)
  ;; Flip the coordinates horizontally -- converts programmers'
  ;; x-/y-axes into mathematicians' x-/y-axes
  (q/scale 1.0 -1.0)
  ;; Draw the lines of where the turtle has been.
  (doseq [l @lines]
    (let [[[x1 y1] [x2 y2]] l]
      (q/line x1 y1 x2 y2)))
  ;; Draw the turtle itself.
  (draw-turtle)
  (q/pop-matrix)
  (q/pop-matrix))

(defmacro if-cljs
  "Executes `then` clause iff generating ClojureScript code. Stolen from Prismatic code.
  Ref. http://goo.gl/DhhhSN, http://goo.gl/Bhdyna."
  [then else]
  (if (:ns &env) ; nil when compiling for Clojure, nnil for ClojureScript
    then else))

(defmacro new-window
  "Opens up a new window that shows the turtle rendering canvas.  In CLJS it will render
  to a new HTML5 canvas object. An optional config map can be provided, where the key
  :title indicates the window title (clj), the :size key indicates a vector of 2 values
  indicating the width and height of the window."
  [& [config]]
  `(if-cljs
    ~(let [default-config {:size [323 200]}
           {:keys [host size]} (merge default-config config)]
       `(do
          (quil.sketch/add-canvas "turtle-canvas")
          (q/defsketch ~'example
            :host "turtle-canvas"
            :setup setup
            :draw draw
            :size ~size)))
    ~(let [default-config {:title "Watch the turtle go!"
                           :size [323 200]}
           {:keys [title size]} (merge default-config config)]
       `(q/defsketch ~'example
          :title ~title
          :setup setup
          :draw draw
          :size ~size))))
