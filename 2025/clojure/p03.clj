;; # Advent of Code 2025 - Day 3

(ns p03
  (:require [clojure.test :as test]
            [clojure.string :as str]))

(def data-string (slurp "../input/03.txt"))
(def test-string "987654321111111
811111111111119
234234234234278
818181911112111")

(def test-data (str/split test-string #"\n"))
(def data (str/split data-string #"\n"))

(defn digits->number [ds]
  (parse-long (apply str ds)))

(defn max-joltage
  "Find the n digits in a seq that give the largest value, keeping order."
  [n xs]
  (let [drop (- (count xs) n)]
    (loop [xs xs drop drop stack []]
      (if (empty? xs)
        (digits->number (take n stack))
        (let [c (first xs)]
          (if (and (seq stack) (pos? drop) (neg? (compare (peek stack) c)))
            (recur xs (dec drop) (pop stack))       ; evict smaller top, keep c
            (recur (rest xs) drop (conj stack c))))))))

(defn sum-of-joltages [n rows]
  (reduce + (map (partial max-joltage n) rows)))

(def part-1 (partial sum-of-joltages 2))

(test/deftest test-part-1
  (test/is (= 357 (part-1 test-data))))

(def ans-1 (part-1 data))

;; ## Part 2

(def part-2 (partial sum-of-joltages 12))

(test/deftest test-part-2
  (test/is (= 3121910778619 (part-2 test-data))))

(def ans-2 (part-2 data))

(defn -main []
  (println "Answer 1:" ans-1)
  (println "Answer 2:" ans-2))
