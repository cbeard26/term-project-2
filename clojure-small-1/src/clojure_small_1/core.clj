;; Clojure v1.11.1
;; Author > Charles Beard
;; Date > 4/22/2024
;; this file contains a collection of short functions that demonstrate some of clojure's features
;; Source: https://www.braveclojure.com/


(ns clojure-small-1.core) ;; using the project namespace

;; this function demonstrate's clojure's support of arity overloading in functions
(defn arity-overload
  ;; Functions can have a docstring here to give details
  ([first second] ;; argument options for the first function body
    (println "That is a" first second))
  ([first] ;; argument options for the second function body
    (arity-overload first "dog"))) ;; a function body can recursively call another function body

;; (arity-overload "red" "pug")
;; => That is a red pug
;; (arity-overload "red")
;; => That is a red dog

;; this function demonstrates how clojure allows for interesting manipulation of function parameters
(defn param-power
  [first second third & rest] ;; the param following & will be list of all additional parameters
    (println "1." first "\n2." second "\n3." third)
    (if (= 1 (count rest)) ;; if statement in clojure is similar to racket, this is the predicate
      (println "One left! –>" (nth rest 0)) ;; the consequent, using nth to find in list by indexing!
      (println "Nope!"))) ;; and the alternative

;; these examples show that the function catches the fourth arg only when it is the last one

;; (param-power ["one", "two", "three", "four"])
;; => 1. one
;; => 2. two
;; => 3. three
;; => One left! -> four

;; (param-power ["one", "two", "three", "four", "five"])
;; => 1. one
;; => 2. two
;; => 3. three
;; => Nope!

;; this function shows one way of recursively calculating the sum of an array in clojure
(defn sum
  ([list] ;; arity overloading negates the ned for a helper function in this instance
    (sum list 0)) ;; base case function body calls main function body and initializaes accumulator to 0
  ([list accumulating_sum] ;; now with two arguments
    (if (empty? list) ;; if the list is empty we've added all the numbers to the accumulator...
      accumulating_sum ;; so we just return the accumulator
      (sum (rest list) (+ accumulating_sum (first list)))))) ;; otherwise add the first to the sum of the rest and recursion happens

;; (sum [1,2,3])
;; => 6
;; (sum [])
;; => 0

