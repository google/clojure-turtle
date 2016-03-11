(ns clojure-turtle.macros
  (:refer-clojure :exclude [repeat]))

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
