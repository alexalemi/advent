;; # 🎄 Advent of Code 2022 - Day 13
(ns p13
  (:require [clojure.string :as str]
            [clojure.test :as test]))

(def data-string (slurp "../input/13.txt"))

(def test-string "[1,1,3,1,1]
[1,1,5,1,1]

[[1],[2,3,4]]
[[1],4]

[9]
[[8,7,6]]

[[4,4],4,4]
[[4,4],4,4,4]

[7,7,7,7]
[7,7,7]

[]
[3]

[[[]]]
[[]]

[1,[2,[3,[4,[5,6,7]]]],8,9]
[1,[2,[3,[4,[5,6,0]]]],8,9]")

(defn process [s]
  (for [pair (str/split s #"\n\n")]
    (->> (str/split-lines pair)
         (map read-string))))

(def data (process data-string))
(def test-data (process test-string))

(defn resolve [x]
  (reduce
   (fn [_ val]
     (cond
       (true? val) (reduced true)
       (false? val) (reduced false)
       :else nil))
   nil
   x))

(defn compare [left right]
  (cond
    (and (number? left) (number? right)) (cond
                                           (< left right) true
                                           (> left right) false
                                           :else nil)
    (and (vector? left) (vector? right)) (let [result (resolve (map compare left right))]
                                           (cond
                                             (true? result) true
                                             (false? result) false
                                             (< (count left) (count right)) true
                                             (> (count left) (count right)) false
                                             :else nil))
    (number? left) (compare [left] right)
    (number? right) (compare left [right])))

(defn enumerate [x]
  (map vector (range) x))

; [true true false true false true false false]

;; ## Part 1

(defn sum-of-correct [data]
  (transduce
   (comp
    (filter (fn [[pk [left right]]] (compare left right)))
    (map first)
    (map inc))
   +
   (enumerate data)))

(test/deftest test-part-1
  (test/is (= 13 (sum-of-correct test-data))))

(def ans1 (sum-of-correct data))
; 6070

;; ## Part 2

(defn decoder-key [data]
  (let [sorted (sort compare (into (apply concat data) [[[2]] [[6]]]))]
    (* (inc (.indexOf sorted [[2]]))
       (inc (.indexOf sorted [[6]])))))

(test/deftest test-part-2
  (test/is (= 140 (decoder-key test-data))))

(def ans2 (decoder-key data))
; 20758

;; ## Main

(defn -test [& _]
  (test/run-tests 'p13))

(defn -main [& _]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
