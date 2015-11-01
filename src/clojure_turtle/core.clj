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
  (:require [quil.core :as q])
  (:use clojure.pprint))

(defn new-turtle
  []
  (atom {:x 0
         :y 0
         :angle 90
         :pen true}))

(def turtle (new-turtle))

(def lines (atom []))

(defn alter-turtle
  [turt f] 
  (swap! turt f)
  turt)

(defn right
  ([ang]
     (right turtle ang))
  ([turt ang]
     (letfn [(add-angle
               [{:keys [angle] :as t}]
               (let [new-angle (-> angle
                                   (- ang)
                                   (mod 360))]
                 (assoc t :angle new-angle)))]
       (alter-turtle turt add-angle))))

(defn left
  ([ang]
     (right (* -1 ang)))
  ([turt ang]
     (right turt (* -1 ang))))

(defn translate
  [{:keys [x y pen] :as t} dx dy]
  (let [new-x (+ x dx)
        new-y (+ y dy)
        line [[x y] [new-x new-y]]]
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
  ([len]
     (forward turtle len))
  ([turt len]
     (let [rads (deg->radians (get @turt :angle))
           dx (* len (Math/cos rads))
           dy (* len (Math/sin rads))
           alter-fn #(translate % dx dy)] 
       (alter-turtle turt alter-fn))))

(defn back
  ([len]
     (forward (* -1 len)))
  ([turt len]
     (forward turt (* -1 len))))

(defn penup
  ([]
     (penup turtle))
  ([turt]
     (letfn [(alter-fn [t] (assoc t :pen false))]
       (alter-turtle turt alter-fn))))

(defn pendown
  ([]
     (pendown turtle))
  ([turt]
     (letfn [(alter-fn [t] (assoc t :pen true))]
       (alter-turtle turt alter-fn))))

(defn draw-turtle
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
  [& body]
  `(fn []
     (do
       ~@ body)))

(defmacro repeat
  [n & body] 
  `(let [states# (repeatedly ~n ~@body)]
     (dorun
      states#)
     (last states#)))

(defn clean
  ([]
     (clean lines))
  ([lines]
     (letfn [(alter-fn [ls] [])]
       (swap! lines alter-fn))))

(defn setxy
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
  ([ang]
     (setheading turtle ang))
  ([turt ang]
     (letfn [(alter-fn [t] (-> t
                               (assoc :angle ang)))]
       (alter-turtle turt alter-fn))))

(defn home
  ([]
     (home turtle))
  ([turt] 
     (setxy turt 0 0) 
     (setheading turt 90)))

(defn reset-rendering
  []
  (.clear (q/current-graphics))
  (q/background 200)                 ;; Set the background colour to
                                     ;; a nice shade of grey.
    (q/stroke-weight 1))

(defn setup []
  (q/smooth)                          ;; Turn on anti-aliasing
  ;; (q/frame-rate 1)                    ;; Set framerate to 1 FPS
  (reset-rendering))

(defn draw []
  (q/with-translation [(/ (q/width) 2) (/ (q/height) 2)]
    (reset-rendering)
    
    (q/push-matrix)
    (q/apply-matrix 1  0 0
                    0 -1 0)
    (doseq [l @lines]
      (let [[[x1 y1] [x2 y2]] l]
        (q/line x1 y1 x2 y2))) 
    (draw-turtle) 
    (q/pop-matrix)))

(defmacro new-window
  [& [config]]
  (let [default-config {:title "Watch the turtle go!"
                        :size [323 200]}
        {:keys [title size]} (merge default-config config)]
    `(q/defsketch ~'example
       :title ~title
       :setup setup
       :draw draw
       :size ~size)))
