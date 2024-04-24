;; Clojure v1.11.1
;; Author > Charles Beard
;; Date > 4/22/2024
;; this file contains the code for a game that is referenced by my textbook
;; it is a game where you capture pegs on a triangular board by moving over them to an empty space
;; Source: https://www.braveclojure.com/

(ns clojure-big.core
  (:require [clojure.set :as set]) ;; set is like a library, a non-default extension of the clojure library
  (:gen-class))

;; ==========> declarations
(declare successful-move prompt-move game-over query-rows) ;; allows for refernce before definition

;; ==========> helper functions

;; this function generates a lazy sequence of the triangular numbers (i.e. 1,3,6,10,etc.)
;; a lazy sequence is an extremely powerful tool of clojure's; it allows for the definition of
;; a set based on the method of its creation without actually calculating the values until they are accessed
;; this is incredibyl powerful for efficieny of programs and runtime
(defn tri*
  ([] (tri* 0 1)) ;; base case with no args given (this will always be how it is called)
  ([sum n]
   (let [new-sum (+ sum n)] ;; this is the rule to create the next value in the sequence
     (cons new-sum (lazy-seq (tri* new-sum (inc n))))))) ;; this puts the new val and old values together in the lazy-seq

(def tri (tri*)) ;; this binds the lazy-seq to the name tri for ease of access

;; this functions returs a boolean value representing whether or not the argument given is a triangular number
(defn triangular?
  [n] ;; the parameter we're checking
  (= n (last (take-while #(>= n %) tri)))) ;; continuously takes triangular numbers until finds or exceeds arg value

;; N.B. the syntax #(stuff) is a succinct way of defining anonymous functions in clojure, with the % sign acting as the input

;; this function returns the triangular number at the end of the row indicated by the argument
(defn row-tri
  [n]
  (last (take n tri)))

;; this function takes a location on the board as a parameter and returns the row in which that value lives
(defn row-num
  [loc] ;; the board position
  (inc (count (take-while #(> loc %) tri)))) ;; checks between which triangular numbers the argument lies to calculate row

;; ==========> board setup/config functions

;; this function estabilshes a connection between two locations on the board
(defn connect
  [board max-pos pos neighbor destination] ;; args
  (if (<= max-pos destination) ;; check if the destination is valid (i.e. on the board somewhere)
    (reduce (fn [new-board [p1 p2]] ;; reduce recursively reverses the connection, so the two locations refer to one another in the final state
              (assoc-in new-board [p1 :connections p2] neighbor)) ;; creates the connection
            board
            [[pos destination] [destination pos]]) ;; parameters for the reduction
    board)) ;; return!

;; the following three funtions serve to determine which locations to connect, and then connects them
;; (btw, 3 functions, three sides on a triangle... see the logic??!)
;; notice that these functions do not take into account validity of location bc connect already does

;; for a destination in the same row
(defn connect-right ;; notice that connect-left is not necessary because of the recursive reversal done in connect
  [board max-pos pos]
  (let [neighbor (inc pos) ;; the peg between pos and destination
        destination (inc neighbor)] ;; also notice that in clojure let functions the way that let* does in racket
    (if-not (or (triangular? neighbor) (triangular? pos))
      (connect board max-pos pos neighbor destination)
      board)))

;; for a destination below andd to the left
(defn connect-down-left
  [board max-pos pos]
  (let [row (row-num pos) ;; finds the rown in which pos is found and binds it to row
        neighbor (+ row pos)
        destination (+ 1 row neighbor)]
    (connect board max-pos pos neighbor destination)))

;; for a destination below and to the right
(defn connect-down-right
  [board max-pos pos]
  (let [row (row-num pos)
        neighbor (+ 1 row pos) ;; the extra addition here...
        destination (+ 2 row neighbor)] ;; and here accounts for shift from left to right
    (connect board max-pos pos neighbor destination)))

;; this function adds a peg to a position and calculates and stores its potential connections
(defn add-pos
  [board max-pos pos]
  (let [pegged-board (assoc-in board [pos :pegged] true)] ;; adds the pegged attribute to the location on the board
    (reduce (fn [new-board connection-creation-fn] ;; recursively sets connections (with an anonymous fn)
              (connection-creation-fn new-board max-pos pos))
            pegged-board
            [connect-right connect-down-left connect-down-right]))) ;; across all three functions we made for determining connections

;; this function generates the board with rows rows
(defn new-board
  [rows]
  (let [initial-board {:rows rows} ;; binds num of rows to rows
        max-pos (row-tri rows)] ;; binds max-pos to the highest possible value on the board based on num of rows
    (reduce (fn [board pos] (add-pos board max-pos pos)) ;; recursively adds every position up to max-pos to the board
            initial-board
            (range 1 (inc max-pos)))))

;; ==========> gameplay helper functions

;; returns bool indicating whether the location currently has a peg
(defn pegged?
  [board pos]
  (get-in board [pos :pegged]))

;; removes the pegged status from the arg location
(defn remove-peg
  [board pos]
  (assoc-in board [pos :pegged] false))

;; adds the pegged status to the arg location
(defn place-peg
  [board pos]
  (assoc-in board [pos :pegged] true))

;; uses place and remove to move the location of a peg
(defn move-peg
  [board p1 p2]
  (place-peg (remove-peg board p1) p2))

;; adds valid connections to the connection list of pos following rules of the game:
;; i.e. the jumped location is pegged and the destination is not
(defn valid-moves
  [board pos]
  (into {} ;; new empty map to hold connection vals
        (filter (fn [[destination jumped]]
                  (and (not (pegged? board destination)) ;; destination not pegged
                       (pegged? board jumped))) ;; peg in jumped location
                (get-in board [pos :connections])))) ;; add to connections

;; returns nil if jump from p1 to p2 is not valid, or the jumped location if it is 
(defn valid-move?
  [board p1 p2]
  (get (valid-moves board p1) p2)) ;; uing valid moves from above

;; moves a peg if the indicated move is valid
(defn make-move
  [board p1 p2]
  (if-let [jumped (valid-move? board p1 p2)] ;; see note below on if-let
    (move-peg (remove-peg board jumped) p1 p2)))

;; N.B. clojure uses truthy and falsey values, where basically everything is truthy except for false and nil
;; conditionals return the truthy or falsey values that they compare rather than true or false
;; if-let is a version of let that binds the result of a conditional to a name if that result is a truthy value

;; checks to see whether the game is over, i.e. if there are/are not any possible moves available
(defn can-move?
  [board]
  (some (comp not-empty (partial valid-moves board))
        (map first (filter #(get (second %) :pegged) board))))

;; ==========> functions for board output

;; definitions related to ascii conversion for output
(def alpha-start 97) ;; a
(def alpha-end 123) ;; z
(def letters (map (comp str char) (range alpha-start alpha-end))) ;; maps chars over ascii val range
(def pos-chars 3)

;; defining colors for fancier output
(def ansi-styles
  {:red   "[31m"
   :green "[32m"
   :blue  "[34m"
   :reset "[0m"})

;; code for setting colors using the styles defined above
(defn ansi
  [style]
  (str \u001b (style ansi-styles)))

;; applying color to text
(defn colorize
  [text color]
  (str (ansi color) text (ansi :reset)))

;; turns an int pos into its displayable version containing a letter accompanied by a 0 or a -
(defn render-pos
  [board pos]
  (str (nth letters (dec pos)) ;; get the letter form of the location
       (if (get-in board [pos :pegged]) ;; decides accompanying symbol based on whether position is pegged or not
          (colorize "0" :red);; if pegged
          (colorize "-" :blue)))) ;; if empty

;; returns all the positions in row row-num
(defn row-positions
  [row-num]
  (range (inc (or (row-tri (dec row-num)) 0)) ;; list of positions in the row row-num
         (inc (row-tri row-num))))

;; returns the amount of padding spaces necessary to properly print the triangle 
(defn row-padding
  [row-num rows]
  (let [pad-length (/ (* (- rows row-num) pos-chars) 2)] ;; arithmetic based on total num of rows and length of current row
    (apply str (take pad-length (repeat " "))))) ;; a bunch of spaces :)

;; composes positions, padding and spaces into the structure of the full row
(defn render-row
  [board row-num]
  (str (row-padding row-num (:rows board)) ;; the padding
       (clojure.string/join " " (map (partial render-pos board) ;; the now space-separated positions in the row
                                     (row-positions row-num)))))

;; prints out all rows in order
(defn print-board
  [board]
  (doseq [row-num (range 1 (inc (:rows board)))] ;; prints each row sequntially
          (println (render-row board row-num))))

;; ==========> functions for user interaction

;; convert a letter to the corresponding location value
(defn letter->pos
  [letter]
  (inc (- (int (first letter)) alpha-start))) ;; 

;; recieves user input and then parses it for consistency
(defn get-input
  ([] (get-input nil)) ;; arity-overloaded base-case
  ([default]
   (let [input (clojure.string/trim (read-line))] ;; reads input and trims off whitespace
     (if (empty? input)
       default ;; nil if no input
       (clojure.string/lower-case input))))) ;; lowercase-ifies it

;; cleans up a string with spaces into a list of non-space, alphabetic characters
(defn characters-as-strings
  [chars]
  (re-seq #"[a-zA-Z]" chars)) ;; this is the syntax of regular expressions in clojure

;; the result of inputting an invalid move
(defn user-entered-invalid-move
  [board]
  (println "\nThat move was invalid!\n")
  (prompt-move board)) ;; you can try again, I suppose...

;; the result of inputting a valid move
(defn user-entered-valid-move
  [board]
  (if (can-move? board)
    (prompt-move board) ;; keep on playing
    (game-over board))) ;; no moves remain to play

;; prompts the user for a move and either does or does not perform it based on validity
(defn prompt-move
  [board]
  (println "The Board:") ;; printing...
  (print-board board) 
  (println "move from where to where (input in form xy):")
  (let [input (map letter->pos (characters-as-strings (get-input)))] ;; interprets 2-char input
    (if-let [new-board (make-move board (first input) (second input))] ;; make-move uses an if-let too, whoa
      (user-entered-valid-move new-board) ;; valid if the if-let returns something truthy
      (user-entered-invalid-move board)))) ;; otherwise false(y)

;; prompts the user which peg they'd like to start by removing
(defn prompt-empty-peg
  [board]
  (println "The Board:") ;; printing... 
  (println "Select a peg to remove:")
  (prompt-move (remove-peg board (letter->pos (get-input "e"))))) ;; removes the indicated peg

;; prompts the user how many rows the board should have
(defn prompt-rows
  []
  (println "How many rows would you like?")
  (let [rows (Integer. (get-input 5)) ;; providing with a default value
        board (new-board rows)] ;; creates a new game board
    (prompt-empty-peg board))) ;; remove one peg

;; this runs when the game ends; prompts play again option and stats
(defn game-over
  [board]
  (let [remaining-pegs (count (filter :pegged (vals board)))] ;; counts pegged positions in board
    (println "You finished the game with" remaining-pegs "pegs remaining!")
    (print-board board)
    (println "Would you like to play again (y/n)?")
    (let [input (get-input "y")] ;; allow the user to input y/n to play again
      (if (= "y" input)
        (prompt-rows) ;; play again
        (do
          (println "Thanks for playing!")
          (System/exit 0)))))) ;; see ya

;; prompt rows is how you start a game
(defn -main
  []
  (prompt-rows))

