;; # 🎄 Advent of Code - 2024 - Day 3

(ns p03
  [:require [clojure.test :as test]
            [clojure.string :as str]])

(def data (slurp "../input/03.txt"))
(def test-data "xmul(2,4)%&mul[3,7]!@^do_not_mul(5,5)+mul(32,64]then(mul(11,8)mul(8,5))")

;; In this puzzle the input data is a string with a bunch of nonsense,
;; we need to extract the instances of `mul ()` and carry out the multiplications
;; 
;; We'll use regex to pull out the `mul (x,y)` patterns, and just a simple
;; transducer to compute the final answer.

(defn digits [col]
  (mapv read-string col))

(defn part-1 [s]
  (transduce
    (comp
      (map rest)
      (map digits)
      (map (partial apply *)))
    +
    (re-seq #"mul\((\d+),(\d+)\)" s)))

(def ans1 (part-1 data))

(test/deftest test-part-1
  (test/is (= 161 (part-1 test-data)))
  (test/is (= 174561379 ans1)))

;; ## Part 2

;; For part 2, now there are `do()` and `don't()` instructions that either
;; enable or disable the future instructions.

(def test-data-2 "xmul(2,4)&mul[3,7]!^don't()_mul(5,5)+mul(32,64](mul(11,8)undo()?mul(8,5))")

;; We'll do this by first splitting on the `do ()`'s so that each segment will be active
;; initially, then we'll only take it up to the first `don't ()` and reuse our `part-1`.

;; We'll formulate this as another transducer.

(defn do-segments [s] (str/split s #"do\(\)"))
(defn upto-don't [s] (first (str/split s #"don't\(\)")))


(defn part-2 [data]
  (transduce
    (comp
      (map upto-don't)
      (map part-1))
    +
    (do-segments data)))


(def ans2 (part-2 data))

(test/deftest test-part-2
  (test/is (= 48 (part-2 test-data)))
  (test/is (= 106921067 ans2)))

;; ## Main

(comment
  (test/run-tests))

(defn -main [& args]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))

