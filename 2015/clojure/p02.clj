;; # Advent of Code 2015 - Day 2
(ns p02
  [:require [clojure.test :as test]
            [clojure.string :as str]
            [jpeg.jpeg :as jpeg]])
           
(def data-string (slurp "../input/02.txt"))

(defn extract-nums [s]
  (map read-string (jpeg/match
                     `(* (<- (some :d)) "x" (<- (some :d)) "x"  (<- (some :d)))
                     s)))

(def data (map extract-nums (str/split-lines data-string)))

(defn wrapping-paper [[l w h]]
  (let [s1 (* l w)
        s2 (* w h)
        s3 (* h l)]
   (+ (* 2 s1) (* 2 s2) (* 2 s3) (min s1 s2 s3))))

(defn reducer [f s]
  (transduce
    (comp (map extract-nums) (map f))
    +
    0
    (str/split-lines s)))

(defn part-1 [s] (reducer wrapping-paper s))

(test/deftest test-part-1
  (test/are [x y] (= (part-1 x) y)
    "2x3x4\n1x1x10" (+ 58 43)))

(def ans1 (part-1 data-string))


(defn ribbon [[l w h]]
  (+ (min (+ l l w w) (+ w w h h) (+ l l h h))
     (* l w h)))


(defn part-2 [s] (reducer ribbon s))

(test/deftest test-part-2
  (test/are [x y] (= (part-2 x) y)
    "2x3x4\n1x1x10" (+ 34 14)))

(def ans2 (part-2 data-string))


(comment
  (test/run-all-tests))

(defn -main [& args]
  (println "Answer 1:" ans1)
  (println "Answer 2:" ans2))

