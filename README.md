# clojure-turtle

clojure-turtle is a Clojure library that implements the Logo programming language in a Clojure context.  [Quil](https://github.com/quil/quil/) is used for rendering.

Logo is a simple language that is useful in introducing programming to beginners, especially young ones.  Logo also happens to be a dialect of Lisp.  clojure-turtle tries to maintain those beneficial aspects of Logo while using Clojure and Clojure syntax.  The goal is to make learning programming and/or Clojure easier by disguising powerful concepts with fun!

## Artifacts

`clojure-turtle` artifacts are [released to Clojars](https://clojars.org/echeran/clojure-turtle).

If you are using Maven, add the following repository definition to your `pom.xml`:

``` xml
<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>
```

### The Most Recent Release

With Leiningen:

``` clj
[com.google/clojure-turtle "0.1.1"]
```

With Maven:

``` xml
<dependency>
  <groupId>com.google</groupId>
  <artifactId>clojure-turtle</artifactId>
  <version>0.1.1</version>
</dependency>
```

## Installation

First, [install Leiningen](https://github.com/technomancy/leiningen).

Second, run a REPL session that has clojure-turtle loaded inside it.  This can be done in a couple of ways:
* [Create a new Leiningen project](https://github.com/technomancy/leiningen/blob/stable/doc/TUTORIAL.md) and [add the clojure-turtle dependency](https://github.com/technomancy/leiningen/blob/stable/doc/TUTORIAL.md#artifact-ids-groups-and-versions) into `project.clj`.  Then run `lein repl`.
* Use git to clone the [clojure-turtle repository](https://github.com/google/clojure-turtle), move into the `clojure-turtle` working directory, then run `lein repl`.

## Usage

Load the `clojure-turtle.core` namespace.

```clojure
(use 'clojure-turtle.core)
; WARNING: repeat already refers to: #'clojure.core/repeat in namespace: user, being replaced by: #'clojure-turtle.core/repeat
;=> nil
```
The symbol `repeat` is overridden to behave more like the Logo function, but the Clojure core function is still available as `clojure.core/repeat`.

Now load a new window that shows our Quil sketch using the `new-window` form. The sketch is where our turtle lives and operates.

```clojure
user=> (new-window {:size [300 480]})
;=> #'user/example
```

![](https://github.com/google/clojure-turtle/blob/master/doc/img/frame00.png)


### forward, back, right, left

It's `forward`, `back`, `right`, and `left` as in Logo.  Go forward and back by a length (in pixels).  Right and left turn the turtle by an angle, in degrees.  It's Clojure syntax, so 'executing commands' (function calls) are done within parentheses.

```clojure
(forward 30)
;=> #<Atom@4c8829b7: {:y 29.99999999999997, :angle 90, :pen true, :x -1.3113417000558723E-6}>
```

![](https://github.com/google/clojure-turtle/blob/master/doc/img/frame01.png)


```clojure
(right 90)
; #<Atom@4c8829b7: {:y 29.99999999999997, :angle 0, :pen true, :x -1.3113417000558723E-6}>
```

![](https://github.com/google/clojure-turtle/blob/master/doc/img/frame02.png)

### repeat, all

`repeat` is like the Logo function, or like Clojure's `repeatedly`.  Going from the Logo syntax to clojure-turtle's syntax for `repeat`, commands that are being repeated are put within parentheses notation.  The square brackets that group the repeated commands are replaced with `(all ... )`.  The equivalent of the Logo `REPEAT 3 [FORWARD 30 RIGHT 90]` would be

```clojure
(repeat 3 (all (forward 30) (right 90)))
;=> #<Atom@4c8829b7: {:y -2.6226834249807383E-6, :angle 90, :pen true, :x -9.535951726036274E-7}>
```

![](https://github.com/google/clojure-turtle/blob/master/doc/img/frame03.png)


Let's see how we can simplify this.


```clojure
(def side (all (forward 30) (right 90)))
;=> #'user/side
(left 90)
;=> #<Atom@4c8829b7: {:y -2.6226834249807383E-6, :angle 180, :pen true, :x -9.535951726036274E-7}>
```

![](https://github.com/google/clojure-turtle/blob/master/doc/img/frame04.png)


```clojure
(repeat 4 side)
;=> #<Atom@4c8829b7: {:y -1.311341712550984E-5, :angle 180, :pen true, :x -4.76797586142362E-6}>
```

![](https://github.com/google/clojure-turtle/blob/master/doc/img/frame05.png)

As you just saw above, we can take the instructions that we pass into `repeat`, give them a single name, and refer to that name to get the same effect.

Let's simplify further.

```clojure
(def square (all (repeat 4 side)))
(left 90)
(square)
```

![](https://github.com/google/clojure-turtle/blob/master/doc/img/frame06.png)


So given a named set of instructions, we can invoke the instructions by putting the name in parentheses just like we do for functions like `forward`, `left`, and `repeat`

```clojure
(def square-and-turn (all (square) (left 90)))
(left 90)
(square-and-turn)

```

![](https://github.com/google/clojure-turtle/blob/master/doc/img/frame07.png)


```clojure
(left 45)
(repeat 4 square-and-turn)
```

![](https://github.com/google/clojure-turtle/blob/master/doc/img/frame08.png)


### penup, pendown, setxy, setheading

The turtle has a pen that it drags along where it goes, creating a drawing.  We can pick the pen up and put the pen down when we need to draw unconnected lines.  `setxy` also teleports the turtle without drawing.  `setheading` turns the turtle in an exact direction.

```clojure
(penup)
(forward 113)
(right 135)
```

![](https://github.com/google/clojure-turtle/blob/master/doc/img/frame09.png)

```clojure
(pendown)
(repeat 4 (all (forward 160) (right 90)))
```

![](https://github.com/google/clojure-turtle/blob/master/doc/img/frame10.png)


```clojure
(setxy -100 0)
```

![](https://github.com/google/clojure-turtle/blob/master/doc/img/frame11.png)


```clojure
(setheading 225)
```

![](https://github.com/google/clojure-turtle/blob/master/doc/img/frame12.png)

### clean, home

`clean` erases all drawing.  `home` brings the turtle to its original position and direction.

```clojure
(clean)
```

![](https://github.com/google/clojure-turtle/blob/master/doc/img/frame13.png)

```clojure
(home)
```

![](https://github.com/google/clojure-turtle/blob/master/doc/img/frame14.png)

### color

Color can be set for the turtle.  A color is specified by a vector of
size 1, 3, or 4.

A three-element color vector has 3 integers for the red,
green, and blue components of the color (in that order) in the range
0 - 255. (See
[this page](https://en.wikipedia.org/wiki/Web_colors#X11_color_names)
for examples of specifying color in terms of RGB values.)

```clojure
(def octagon (all (repeat 8 (all (forward 30) (right))))) 
(color [0 0 255])
(octagon)
```

![](https://github.com/google/clojure-turtle/blob/master/doc/img/frame15.png)

The turtle sprite (the triangle representing the turtle) will be drawn
in the same color as the turtle's pen.

```clojure
(repeat 12 (all (octagon) (right 30)))
```

![](https://github.com/google/clojure-turtle/blob/master/doc/img/frame16.png)

We can also use our color value to fill the interior of shapes that we
draw.  To draw shapes will a fill color, we first have to indicate
when we start and when we end drawing the shape.  For that, we use the
`start-fill` and `end-fill` commands.  Every line segment that the turtle
draws in between `start-fill` and `end-fill` is assumed to form the
perimeter of the shape.

Let us define `filled-octagon` as the combination of commands to draw a filled
octagon.  In between the `start-fill` and `end-fill` that demarcate our
fill shape, we will use our `octagon` function to draw the perimeter of the
octagon that we want filled.

```clojure
(def filled-octagon (all (start-fill) (octagon) (end-fill))) 
```

If a four-element color vector is given for a color, then the 4th value is known
as the "alpha" value.  In clojure-turtle, the alpha value is also an
integer that ranges from 0 to 255.  The value 0 represents full
transparency, and 255 represents full opacity.

```clojure
(color [255 255 0 100]) 
```

We will want to draw 4 octagons that
overlap, so we will create a `points` vector of 4 x,y-coordinates from
which we will start drawing
each octagon.

```clojure
(def points [[-11 -11] [-62 -11] [-36 14] [-36 -36]])
```

For now, let's retrieve only the first of the four points,
set our position to that first point, and then draw our first octagon
from there.

```clojure
(let [point-1 (first points)
      x (first point-1)
      y (second point-1)]
  (setxy x y)
  (filled-octagon))
  ```
  
![](https://github.com/google/clojure-turtle/blob/master/doc/img/frame17.png)

Next, we will draw our the remaining 3 octagons.  Since we will
perform similar actions in repetition, let's create a function to
store the behavior we want to repeat.

```clojure
(defn filled-octagon-from-point
  [point]
  (let [x (first point)
        y (second point)]
    (setxy x y)
    (filled-octagon)))
```

Given a point in the form `[x y]`, the function
`filled-octagon-from-point` will draw a filled octagon starting at
(x,y).  We label the positions (indices) of a vector starting from 0,
so the 1st element is as position 0, the 3rd element is at position 2,
and the 4th element is at position 3.

```clojure
;; octagon starting from 2nd point:
(filled-octagon-from-point (second points))
;; ... from 3rd point:
(filled-octagon-from-point (nth points 2))
;; ... from 4th point:
(filled-octagon-from-point (nth points 3))
```

![](https://github.com/google/clojure-turtle/blob/master/doc/img/frame18.png)

A color vector of size 1 creates a grayscale color ranging from black
to white.  The grayscale
color is equivalent to using a 3-element RGB color vector where the
values for red, green, and blue are the same.

```clojure
(color [0])
(home)
```

![](https://github.com/google/clojure-turtle/blob/master/doc/img/frame19.png)

### wait and animation

We can use `wait` to make the turtle pause.  The number passed to wait
indicates how many milliseconds the turtle should wait (1 millisecond
= 0.001 seconds = 1 / 1000 seconds).

```clojure
(clean)
(home)
(def stop-and-go (all (forward 30) (wait 2000) (right 90) (forward 30)))
(stop-and-go)
```

Computers today are fast.  If we use `wait` to slow them down, we can
watch the turtle move and perceive motion.

```clojure
(clean)
(home)

(defn slower-octagon
  []
  (repeat 8 (fn []
              (forward 30)
              (right 45)
              (wait 100))))
(repeat 12 (all (slower-octagon) (right 30)))
```

What happens when you combine `wait` with `clean`?  If we repeatedly
draw, wait, and clean images in a loop, we can create the effect of
motion!  See the [Animation page](https://github.com/google/clojure-turtle/wiki/Animation) for more
information and examples.

### What next?

clojure-turtle uses Quil, which uses [Processing](https://processing.org/). clojure-turtle also has the full power and fun of Clojure available to it, too.

What do you get when you enter the following?

```clojure
(defn square-by-length
  [side-length]
  (repeat 4 (all (forward side-length) (right 90))))

(square-by-length 10)
(square-by-length 20)
```

```clojure
(def lengths [40 50 60])
(map square-by-length lengths)
```

```clojure
(defn times-2
  [x]
  (* 2 x))

(right 90)
(map square-by-length (map times-2 lengths))

(right 90)
(->> lengths
     (map times-2)
     (map square-by-length))
```

```clojure
(defn polygon-side
  [num-sides side-length]
  (forward side-length)
  (right (/ 360 num-sides)))

(defn polygon
  [num-sides side-length]
  (repeat num-sides (all (polygon-side num-sides side-length))))

(clean)
(right 180)
(polygon 5 20)

(def side-counts [6 7 8 10 12])
(def lengths (reverse [30 40 50 60 70]))
(map polygon side-counts lengths)
```

```clojure
(defn rand-side
  []
  (forward (rand-int 50))
  (setheading (rand-int 360)))

(fn? rand-side)
(fn? side)

(clean)
(home)
(repeat 4 side)
(repeat 100 rand-side)
```

What possibilities exist when you incorporate the full power of Clojure?  What can you create?

## Using the ClojureScript Version

The same codebase in `clojure-turtle` can be compiled to JS and used in a JS runtime in addition to JVM bytecode.  A demo of the JS version can be executed by first running the command:

```clojure
lein figwheel
```

Where the output may look like:
```
$ lein figwheel
Figwheel: Starting server at http://localhost:3449
Figwheel: Watching build - dev
Compiling "demo/public/js/main.js" from ["src" "demo/src"]...
Successfully compiled "demo/public/js/main.js" in 16.311 seconds.
Launching ClojureScript REPL for build: dev
...
```

Then, in your browser, visit the URL in the terminal output from the command -- in this example, it is http://localhost:3449.  You will see a webpage load with the Quil canvas containing the turtle.  Back in your terminal, Figwheel will load a ClojureScript REPL that is connected to the webpage (more precisely, the browser REPL running in the webpage).  In the ClojureScript REPL, run:

```
cljs.user=> (ns clojure-turtle.core)
cljs.user=> (require '[clojure-turtle.macros :refer-macros [repeat all]])
```

Now, the above Logo/`clojure-turtle` commands can be issued in the CLJS REPL as described above, with the result visible in the Figwheel-connected browser page.

## Mailing List

Join the [clojure-turtle mailing list](https://groups.google.com/forum/#!forum/clojure-turtle) to post questions and receive announcements.

## How to Contribute

Interested in contributing code to the project?  We would love to have
your help!

Before you can contribute, you should first read the
[page on contributing](./CONTRIBUTING.md) and agree to the Contributor
License Agreement.  Signing the CLA can be done online and is fast.
This is a one-time process.

Thereafter, contributions can be initiated through a [pull
request](https://help.github.com/articles/using-pull-requests/).

## License

Distributed under the Apache 2 license.

### Disclaimer

This is not an official Google product (experimental or otherwise), it is just code that happens to be owned by Google.

### Dependencies

Quil is distributed under the Eclipse Public License either version 1.0 (or at your option) any later version.

The official Processing.org's jars, used as dependencies of Quil, are distributed under LGPL and their code can be found on http://processing.org/
