;; Clojure v1.11.1
;; Author > Charles Beard
;; Date > 4/22/2024
;; this file demonstrates some of clojures other features
;; Source: https://www.braveclojure.com/

(ns clojure-small-2.core)

;; macros are a way of teaching the interpreter how to behave when given certain input
;; they are extraordinaryily poerful as a metaprogramming tool because they allow you to
;; redefine the language witin your code
(defmacro switch ;; in this example, the second and third elements of an exression are swapped
  [exp] ;; input expression
  (list (first exp) (nth exp 2) (nth exp 1))) ;; reordering

;; (switch (- 1 2)) –> turns (- 1 2) into (- 2 1)
;; => 1

;; this one reverses arg order
(defmacro reverse-thing
  [exp]
  (reverse exp))

;; (reverse-thing ("i" "h" str))
;; => "hi"

;; (reverse-thing (1 2 3 4 +))
;; => 10

(def mr_m {:name      "Paul"
             :age       "81"
             :residence "Abbey Road"})

;; this function is broken because i ran out of time, but hopefully you can visually get the idea;
;; it's about destructuring
(defn introduce person
  [name (:name person)
   age (:age person)
   residence (:residence person)]
  (println "Hello my name is" name "and I am" age "years old and I live at" residence))

;; destructuring is a form of argument formatting that allows for easy access to
;; function input that is in a data structure
(defn my-crazy-function
  [[first-thing second-thing third-thing]] ;; this function will always take 3 args
  (let [hmm (and first-thing second-thing third-thing)]
    (if hmm
      (println "Yes, I like" first-thing "and" second-thing "and" third-thing)
      (println "I am greatly displeased, and profoundly disappointed."))))

;; (my-crazy-function [:yes :happy :fun])
;; => Yes, I like :yes and :happy and :fun

;; (my-crazy-function [:yes :happy nil])
;; => I am greatly displeased, and profoundly disappointed.

;; concurrent programming is the allocation of tasks to threads, in clojure that means jvm threads
(defn the-future
  [n]
  (let [new-num (future (Thread/sleep 2000) ;; future says do sothing later, sleep says how much later (ms)
                        (+ 1 n))]
    (println "The results are in:" @new-num))) ;; the output is dereferenced with @
;; dereferencing is necessary bc the future function returns an object

;; (the-future 1)
;; => 2 (after 2 seconds have ellapsed)

;; delay allows you to get the value you're delaying whenever you want/need it
(def soon-to-be-sum (delay (+ 1 2 3 4))) ;; value is only calculated once called

;; (force soon-to-be-sum)
;; => 10

;; @soon-to-be-sum
;; => 10

;; promise is similar to delay, but you don't have to immediately say what you want later
(def will-there-be-dogs? (promise)) ;; define a promise with a name

;; (deliver will-there-be-dogs? true)
;; @will-there-be-dogs?
;; => true
;; (deref will-there-be-dogs?)
;; => true

;; pmap distributes tasks of the map to individual threads, dramatically improving runtime
(pmap inc [1 2 3 4])
