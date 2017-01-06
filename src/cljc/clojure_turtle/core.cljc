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
  #?(:clj
     (:require [quil.core :as q])
     :cljs
     (:require [quil.core :as q :include-macros true]
               [clojure-turtle.macros :refer-macros [repeat all]]))
  #?(:clj
     (:import java.util.Date)))

  

;;
;; constants
;;

(def ^{:doc "The default color to be used (ex: if color is not specified)"}
  DEFAULT-COLOR [0 0 0])

;;
;; records
;;

(defrecord Turtle [x y angle pen color fill commands start-from]
  ;; both for Clojure and ClojureScript, override the behavior of the
  ;; str fn / .toString method to "restore" the default .toString
  ;; behavior for the entire Turtle record data, instead of just
  ;; returning whatever pr-str returns
  Object
  (toString [turt] (str (select-keys turt (keys turt)))))

;;
;; record printing definitions
;;

(defn pr-str-turtle
  "This method determines what gets returned when passing a Turtle record instance to pr-str, which in turn affects what gets printed at the REPL"
  [turt]
  (pr-str (select-keys turt [:x :y :angle :pen :color :fill])))

#?(:clj (defmethod print-method Turtle [turt writer]
          (.write writer (pr-str-turtle turt)))
   :cljs (extend-type Turtle
           IPrintWithWriter
           (-pr-writer [turt writer _]
             (-write writer (pr-str-turtle turt)))))

;;
;; fns - turtle fns
;;

(defn new-turtle
  "Returns an entity that represents a turtle."
  []
  (atom (map->Turtle {:x 0
                      :y 0
                      :angle 90
                      :pen true
                      :color DEFAULT-COLOR
                      :fill false
                      :commands []
                      :start-from {:x 0
                                   :y 0}})))

(def ^{:doc "The default turtle entity used when no turtle is specified for an operation."}
  turtle (new-turtle))

(defn alter-turtle
  "A helper function used in the implementation of basic operations to abstract
  out the interface of applying a function to a turtle entity."
  [turt-state f]
  (swap! turt-state f)
  turt-state)

;;
;; fns - colors and visual effects
;;

(defn make-opaque
  "Take a color vector, as passed to the `color` fn, and return a color vector
  in the form [red blue green alpha], where all RGB and alpha values are integers
  in the range 0-255 inclusive. In order to make the color vector represent full
  opacity, the alpha value will be 255."
  [color-vec]
  (let [rgb-vec (case (count color-vec)
                  1 (clojure.core/repeat 3 (first color-vec))
                  3 color-vec
                  4 (take 3 color-vec))
        rgba-vec (concat rgb-vec [255])]
    rgba-vec))

(defn color
  "Set the turtle's color using [red green blue].
  RGB values are in the range 0 to 255, inclusive."
  ([c]
     (color turtle c))
  ([turt-state c]
     (letfn [(alter-fn [t] (-> t
                               (assoc :color c)
                               (update-in [:commands] conj [:color c])))]
       (alter-turtle turt-state alter-fn))))

;;
;; fns - basic Logo commands
;;

(defn right
  "Rotate the turtle turt clockwise by ang degrees."
  ([ang]
     (right turtle ang))
  ([turt-state ang]
     ;; the local fn add-angle will increment the angle but keep the
     ;; resulting angle in the range [0,360), in degrees.
     (letfn [(add-angle
               [{:keys [angle] :as t}]
               (let [new-angle (-> angle
                                   (- ang)
                                   (mod 360))]
                 (-> t
                     (assoc :angle new-angle)
                     (update-in [:commands] conj [:setheading new-angle]))))]
       (alter-turtle turt-state add-angle))))

(defn left
  "Same as right, but turns the turtle counter-clockwise."
  ([ang]
     (right (* -1 ang)))
  ([turt-state ang]
     (right turt-state (* -1 ang))))

(def deg->radians q/radians)

(def radians->deg q/degrees)

(def atan q/atan)

(defn forward
  "Move the turtle turt forward in the direction that it is facing by length len."
  ([len]
     (forward turtle len))
  ([turt-state len]
     ;; Convert the turtle's polar coordinates (angle + radius) into
     ;; Cartesian coordinates (x,y) for display purposes
     (let [rads (deg->radians (get @turt-state :angle))
           dx (* len (Math/cos rads))
           dy (* len (Math/sin rads))
           alter-fn (fn [t] (-> t
                               (update-in [:x] + dx)
                               (update-in [:y] + dy)
                               (update-in [:commands] conj [:translate [dx dy]])))] 
       (alter-turtle turt-state alter-fn))))

(defn back
  "Same as forward, but move the turtle backwards, which is opposite of the direction it is facing."
  ([len]
     (forward (* -1 len)))
  ([turt-state len]
     (forward turt-state (* -1 len))))

(defn penup
  "Instruct the turtle to pick its pen up. Subsequent movements will not draw to screen until the pen is put down again."
  ([]
     (penup turtle))
  ([turt-state]
     (letfn [(alter-fn [t] (-> t
                               (assoc :pen false)
                               (update-in [:commands] conj [:pen false])))]
       (alter-turtle turt-state alter-fn))))

(defn pendown
  "Instruct the turtle to put its pen down. Subsequent movements will draw to screen."
  ([]
     (pendown turtle))
  ([turt-state]
     (letfn [(alter-fn [t] (-> t
                               (assoc :pen true)
                               (update-in [:commands] conj [:pen true])))]
       (alter-turtle turt-state alter-fn))))

(defn start-fill
  "Make the turtle fill the area created by his subsequent moves, until end-fill is called."
  ([]
     (start-fill turtle))
  ([turt-state]
     (letfn [(alter-fn [t]
               (-> t
                   (assoc :fill true)
                   (update-in [:commands] conj [:start-fill])))] 
       (alter-turtle turt-state alter-fn))))

(defn end-fill
  "Stop filling the area of turtle moves. Must be called start-fill."
  ([]
     (end-fill turtle))
  ([turt-state]
     (letfn [(alter-fn [t]
               (-> t
                   (assoc :fill false)
                   (update-in [:commands] conj [:end-fill])))]
       (alter-turtle turt-state alter-fn))))

#?(:clj
   (defmacro all
     "This macro was created to substitute for the purpose served by the square brackets in Logo
  in a call to REPEAT.  This macro returns a no-argument function that, when invoked, executes
  the commands described in the body inside the macro call/form.
  (Haskell programmers refer to the type of function returned a 'thunk'.)"
     [& body]
     `(fn []
        (do
          ~@ body))))

#?(:clj
   (defmacro repeat
     "A macro to translate the purpose of the Logo REPEAT function."
     [n & body] 
     `(let [states# (repeatedly ~n ~@body)]
        (dorun
         states#)
        (last states#))))

(defn wait
  "Sleeps for ms miliseconds. Can be used in a repeat to show commands execute in real time"
  [ms]
  (letfn [(get-time []
            #?(:clj (.getTime (Date.))
               :cljs (.getTime (js/Date.))))]
    (let [initial-time (get-time)]
      (while (< (get-time) (+ initial-time ms))))))

(defn clean
  "Clear the lines state, which effectively clears the drawing canvas."
  ([]
     (clean turtle))
  ([turt-state]
     (letfn [(alter-fn
               [t]
               (let [curr-pos-map (select-keys t [:x :y])]
                 (-> t
                     (assoc :commands [])
                     (assoc :start-from curr-pos-map))))]
       (alter-turtle turt-state alter-fn))))

(defn setxy
  "Set the position of turtle turt to x-coordinate x and y-coordinate y."
  ([x y]
     (setxy turtle x y))
  ([turt-state x y]
     (let [pen-down? (get @turt-state :pen)]
       (letfn [(alter-fn [t] 
                 (-> t
                     (assoc :x x)
                     (assoc :y y)
                     (update-in [:commands] conj [:setxy [x y]])))]
         (alter-turtle turt-state alter-fn)))))

(defn setheading
  "Set the direction which the turtle is facing, given in degrees, where 0 is to the right,
  90 is up, 180 is left, and 270 is down."
  ([ang]
     (setheading turtle ang))
  ([turt-state ang]
     (letfn [(alter-fn [t] (-> t
                               (assoc :angle ang)
                               (update-in [:commands] conj [:setheading ang])))]
       (alter-turtle turt-state alter-fn))))

(defn home
  "Set the turtle at coordinates (0,0), facing up (heading = 90 degrees)"
  ([]
     (home turtle))
  ([turt-state]
     (setxy turt-state 0 0) 
     (setheading turt-state 90)))

;;
;; fns - (Quil-based) rendering and graphics
;;

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

(defn get-turtle-sprite
  "A helper function that draws the triangle that represents the turtle onto the screen."
  ([]
     (get-turtle-sprite turtle))
  ([turt]
     (let [
           ;; set up a copy of the turtle to draw the triangle that
           ;; will represent / show the turtle on the graphics canvas
           short-leg 5
           long-leg 12
           hypoteneuse (Math/sqrt (+ (* short-leg short-leg)
                                     (* long-leg long-leg)))
           large-angle  (-> (/ long-leg short-leg)
                            atan
                            radians->deg)
           small-angle (- 90 large-angle)
           turt-copy-state  (-> (atom turt)
                                pendown
                                clean)
           current-color (:color turt)
           opaque-color (make-opaque current-color)]
       ;; Use the turtle copy to step through the commands required
       ;; to draw the triangle that represents the turtle.   the
       ;; turtle copy will be used for the commands stored within it.
       (do
         (-> turt-copy-state
             (setxy (:x turt) (:y turt)) ;teleport to the current position (turtle's centre)
             (penup)
             (back (/ long-leg 3)) ;move backwards to the centre of the turtle's base
             (pendown)
             (color opaque-color)
             (right 90)
             (forward short-leg)
             (left (- 180 large-angle))
             (forward hypoteneuse)
             (left (- 180 (* 2 small-angle)))
             (forward hypoteneuse)
             (left (- 180 large-angle))
             (forward short-leg)
             (left 90)))
       ;; now return the turtle copy
       turt-copy-state)))

(defn draw-turtle-commands
  "Takes a seq of turtle commands and converts them into Quil commands to draw
  onto the canvas"
  [turt]
  (let [new-turtle @(new-turtle)
        start-from-pos (get turt :start-from)
        new-turtle-with-start (-> new-turtle
                                  (assoc :x (:x start-from-pos))
                                  (assoc :y (:y start-from-pos)))]
    (loop [t new-turtle-with-start
           commands (:commands turt)]
      (if (empty? commands)
        t
        (let [next-cmd (first commands)
              cmd-name (first next-cmd)
              cmd-vals (rest next-cmd)
              rest-cmds (rest commands)]
          (case cmd-name
            :color (let [c (first cmd-vals)]
                     (apply q/stroke c)
                     (apply q/fill c)
                     (recur (assoc t :color c) rest-cmds))
            :setxy (let [[x y] (first cmd-vals)]
                     (recur (assoc t :x x :y y) rest-cmds))
            :setheading (recur (assoc t :angle (first cmd-vals)) rest-cmds)
            :translate (let [x (:x t)
                             y (:y t)
                             [dx dy] (first cmd-vals)
                             new-x (+ x dx)
                             new-y (+ y dy)]
                         (when (:pen t)
                           (q/line x y new-x new-y)
                           (when (:fill t)
                             (q/vertex x y)
                             (q/vertex new-x new-y)))
                         (recur (assoc t :x new-x :y new-y) rest-cmds))
            :pen (recur (assoc t :pen (first cmd-vals)) rest-cmds)
            :start-fill (do (when-not (:fill t)
                              (q/begin-shape))
                            (recur (assoc t :fill true) rest-cmds))
            :end-fill (do (when (:fill t)
                            (q/end-shape))
                          (recur (assoc t :fill false) rest-cmds))
            t))))))

(defn draw-turtle
  "The function passed to Quil for doing rendering."
  [turt-state]
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
  ;; Set the default colors for line stroke and shape fill
  (apply q/stroke DEFAULT-COLOR)
  (apply q/fill DEFAULT-COLOR)
  ;; Draw the lines of where the turtle has been.
  (draw-turtle-commands @turt-state)
  ;; Draw the sprite (triangle) representing the turtle itself.
  (let [sprite (get-turtle-sprite @turt-state)] 
    (draw-turtle-commands @sprite))
  ;; Undo the graphing plane transformation in Quil/Processing
  (q/pop-matrix)
  (q/pop-matrix))

(defn draw
  "The function passed to Quil for doing rendering."
  []
  (draw-turtle turtle))

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
