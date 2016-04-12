&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Animation in clojure-turtle is primarily handled using the `wait` command, which can allow drawings to unfold in stages. Coupled with the `clean` command, one can define distinct frames, drawing an image, waiting, then cleaning and starting over.

##How to use Wait
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Wait takes a number and sleeps the program for that many milliseconds. 1000 Milliseconds is one second, so the following code will sleep for one second.

```clojure
(wait 1000)
```

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;When animating computer graphics frame-rates that are multiples of 30 are good, because they sync up with the refresh-rate of most monitors. 60 fps is the standard benchmark value for many video games, so we will work with that value for the rest of this example. (1000 milliseconds / second) / (60 frames / second) = 16.666 milliseconds per frame, so a wait time of 17 will produce about 60 frames per second. Note that if you are used to animating in 30 fps a wait time of 33.333 will produce that frame rate.

##How to make a simple object move
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Suppose we want to make a star move around in a circle. To do this we first need a function that draws our star. This way we can move the turtle around and simply call (Star) once each frame. The following code draws a five-pointed star with the vertical edge in the direction of the turtle:

```clojure
(def star (all (repeat 5 (all (forward 100) (left 144))))
```

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;It just so happens that the internal angles of a five-pointed star are all 144 degrees, so a simple 5-stage loop generates the desired shape. The end-result should look something like this:

(image will go here once I know how, for now heres a [link](https://drive.google.com/file/d/0BxEsaFmDRvM1WlZHWHZLU0JiUDQ/view?usp=sharing)) 

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Now that we have or object we need to make it move. To do this we will need something like the following pseudo-code:

```clojure
(repeat n (all (draw) (wait) (move-commands) (clean) )
```

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;We assume that the computers internal logic happens instantaneously, so the only thing that visibly makes it to the screen is what is drawn at the moment `(wait)` is called. Nothing that happens in the move commands will ever make it to the screen, because it is cleaned and then drawn over instantaneously, before the program pauses long enough to make it visible.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Let's start with the move commands. We said we wanted the star to move in a circle, so lets use a good approximation of a circle:

```clojure
(forward 10) (left 10)
```

This is not actually a circle but it has many small sides so it is close enough for our purposes. Each side is of length 10, which means that our star will be moving at a rate of 10px/frame * 60 frame/sec = 600 pixles per second. That is fairly fast, and if you want to make it move at a more reasonable speed something like 5px/frame or even just 1 might be more better. Since this is a circle, just changing the side-length will alter the dimensions of the circular path, but we won't worry about that now.

The wait command we already worked out would be (wait 17), and (clean) is just (clean), so we have basically everything. All we have to do is plug in our draw function that we defined earlier and we get:

For n, use the number of frames you want the animation to last for. In my example I used 1000 frames, which is 1000 frames / (60 frames/second) = 16.666 seconds of animation. 

```clojure
(repeat 1000 (all (star) (wait 17) (forward 10) (left 10) (clean) )
```

Run the command and it should look something like this:

[![](https://pixabay.com/static/uploads/photo/2015/10/01/21/39/background-image-967820_960_720.jpg)](https://www.youtube.com/watch?v=HQRPSCNzCTg)