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
[clojure-turtle "0.1.0"]
```

With Maven:

``` xml
<dependency>
  <groupId>clojure-turtle</groupId>
  <artifactId>clojure-turtle</artifactId>
  <version>0.1.0</version>
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
user=> (new-window)
;=> #'user/example
```

![](https://github.com/google/clojure-turtle/blob/master/doc/img/frame00.png)


### forward, back, right, left

It's `forward`, `back`, `right`, and `left` as in Logo.  Go forward and back by a length (in pixels).  Right and left turn the turtle by an angle, in degrees.  It's Clojure syntax, so 'executing commands' (function calls) are done within parenthases.

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

`repeat` is like the Logo function, or like Clojure's `repeatedly`.  Going from the Logo syntax to clojure-turtle's syntax for `repeat`, commands that are being repeated are put within parenthases notation.  The square brackets that group the repeated commands are replaced with `(all ... )`.  The equivalent of the Logo `REPEAT 3 [FORWARD 30 RIGHT 90]` would be

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


So given a named set of instructions, we can invoke the instructions by putting the name in parenthases just like we do for functions like `forward`, `left`, and `repeat`

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

### What next?

clojure-turtle uses Quil, which uses [Processing](https://processing.org/). clojure-turtle also has the full power and fun of Clojure available to it, too.

What do you get when you enter the following?

```clojure
(defn any-square
  [s]
  (repeat 4 (all (forward s) (right 90))))

(any-square 10)
(any-square 20)
```

```clojure
(def lengths [40 50 60])
(map any-square lengths)
```

```clojure
(defn times-2
  [x]
  (* 2 x))

(right 90)
(map any-square (map times-2 lengths))
(right 90)
(->> lengths
     (map times-2)
     (map any-square))
```

```clojure
(defn polygon-side
  [n s]
  (forward s)
  (right (/ 360 n)))

(defn any-polygon
  [n s]
  (repeat n (all (polygon-side n s))))

(clean)
(right 180)
(any-polygon 5 20)
(def sides [6 7 8 10 12])
(def lengths (reverse [30 40 50 60 70]))
(map any-polygon sides lengths)
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

## Mailing List

Join the [clojure-turtle mailing list](https://groups.google.com/forum/#!forum/clojure-turtle) to post questions and receive announcements.

## License

Distributed under the Apache 2 license.

### Disclaimer

This is not an official Google product (experimental or otherwise), it is just code that happens to be owned by Google.

### Dependencies

Quil is distributed under the Eclipse Public License either version 1.0 (or at your option) any later verison.

The official Processing.org's jars, used as dependencies of Quil, are distributed under LGPL and their code can be found on http://processing.org/
