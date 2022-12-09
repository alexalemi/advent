;; # 🎄 Advent of Code 2022 - Day 9 - Rope Bridge

(ns p09
  (:require [clojure.string :as str]
            [clojure.test :as test]))

;; Today we are tasked with simulating a rope moving around
;; on an infinite grid.  We are given a set of instructions
;; for how to move the head of the rope, our first task
;; will be to process these into a useable form:

(def data-string (slurp "../input/09.txt"))
(def test-string "R 4
U 4
L 3
D 1
R 4
D 1
L 5
R 2")

(defn parse [s]
  (for [line (str/split-lines s)]
    (let [[dir x] (str/split line #" ")]
      [(keyword dir) (parse-long x)])))

(def data (parse data-string))
(def test-data (parse test-string))

(defn expand
  "Lazily expand a set of instructions to individual steps."
  [instructions]
  (eduction
   (mapcat (fn [[direction n]] (take n (repeat direction))))
   instructions))

;; ## Logic
;; Our positions will be two element vectors so we'll need to implement a move

(defn target [direction [x y]]
  (case direction
    :U [x (inc y)]
    :D [x (dec y)]
    :L [(dec x) y]
    :R [(inc x) y]))

;; We'll represent out rope by a vector of these positions, [head tail].

(defn inf-norm [[x1 y1] [x2 y2]]
  (max (abs (- x2 x1)) (abs (- y2 y1))))

(defn move [[head tail] direction]
  (let [new-head (target direction head)
        new-tail (if (> (inf-norm new-head tail) 1) head tail)]
    [new-head new-tail]))

(defn total-visited [instructions initial]
  (->> (expand instructions)
       (reductions move initial)
       (map last)
       (into #{})
       count))

(def short-rope [[0 0] [0 0]])

(test/deftest test-part-1
  (test/is (= 13 (total-visited test-data short-rope))))

(def ans1 (total-visited data short-rope))

;; ## Part 2
;;
;; For part 2 we want to make the rope longer. In principle this should be easy,
;; but I had made too simple an assumption in part 1.  Since the head of the snake
;; only moved :U :D :L and :R, we could always have the tail just move to the previous
;; head position, however for part 2, further down the snake pieces might move diagonally,
;; at which point we need to implement the rules with more precision.
;;
;; So to express the update rule again, I'll say that if we have to move,
;; we have the x and y coordinates update to the average of the old position
;; and the target, biased such that we always round towards the target.

(defn avg [a b]
  (/ (+ a b) 2))

(defn where-to [[x y] [x0 y0]]
  (let [dx (abs (- x x0))
        dy (abs (- y y0))]
    [(if (> dx 1) (avg x x0) x0)
     (if (> dy 1) (avg y y0) y0)]))

;; We also have to modify our move function so that it can accomodate longer
;; snakes.

(defn move [[head & tail] direction]
  (let [new-head (target direction head)]
    (loop [prev new-head
           tail tail
           new [new-head]]
      (if-let [knot (first tail)]
        (let [new-knot (if (> (inf-norm prev knot) 1)
                         (where-to knot prev)
                         knot)]
          (if (= new-knot knot)
            (into new tail)
            (recur new-knot (rest tail) (conj new new-knot))))
        new))))

(def long-rope (into [] (repeat 10 [0 0])))

(def test-data-2 (parse "R 5
U 8
L 8
D 3
R 17
D 10
L 25
U 20"))

(test/deftest test-part-2
  (test/is (= 1 (total-visited test-data long-rope)))
  (test/is (= 36 (total-visited test-data-2 long-rope))))

(def ans2 (total-visited data long-rope))

;; ## main

(defn -test [_]
  (test/run-tests))

(defn -main [_]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
