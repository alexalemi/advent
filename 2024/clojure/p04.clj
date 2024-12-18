;; # 🎄 Advent of Code - 2024 - Day 4

(ns p04
  [:require [clojure.test :as test]
            [clojure.string :as str]])

;; ## Data
;; The data is a field of letters, we'll turn this into a map.

(def data-string (slurp "../input/04.txt"))
(def test-data-string "MMMSXXMASM
MSAMXMSMSA
AMXSXMAAMM
MSAMASMSMX
XMASAMXAMM
XXAMMXXAMA
SMSMSASXSS
SAXAMASAAA
MAMMMXMMMM
MXMXAXMASX")

(defn process [s]
  (into {}
    (for [[y line] (map-indexed vector (str/split-lines s))
          [x c] (map-indexed vector line)]
       [[x y] c])))

(def data (process data-string))
(def test-data (process test-data-string))

;; ## Logic
;; To solve this, we'll create the directions
;; and write a match utility that checks to see
;; if we have an X-MAS at each location and direction.

(defn north [[x y]] [x (dec y)])
(defn south [[x y]] [x (inc y)])
(defn east [[x y]] [(inc x) y])
(defn west [[x y]] [(dec x) y])
(def north-west (comp north west))
(def north-east (comp north east))
(def south-west (comp south west))
(def south-east (comp south east))

(def directions 
  [north south east west 
   north-west north-east 
   south-west south-east])

(defn match 
  ([m loc dir] (every? true? (map = "XMAS" (map m (iterate dir loc)))))
  ([m loc] (count (filter (partial match m loc) directions)))
  ([m] (transduce
         (map (partial match m)) 
         +
         (keys m))))

(def part-1 match)
(def ans1 (part-1 data))

(test/deftest test-part-1
  (test/is (= 18 (part-1 test-data)))
  (test/is (= 2591 ans1)))

;; ## Part 2
;; Instead of finding XMAS in a row, now we are supposed to look
;; for crosses of MAS.

(defn match-x-mas 
  ([m loc] (and
             (= (m loc) \A)
             (or (and (= (m (north-east loc)) \M)
                      (= (m (south-west loc)) \S))
                 (and (= (m (south-west loc)) \M)
                      (= (m (north-east loc)) \S)))
             (or (and (= (m (north-west loc)) \M)
                      (= (m (south-east loc)) \S))
                 (and (= (m (south-east loc)) \M)
                      (= (m (north-west loc)) \S)))))
  ([m] (count (filter (partial match-x-mas m) (keys m)))))

(def part-2 match-x-mas)
(def ans2 (part-2 data))

(test/deftest test-part-2
  (test/is (= 9 (part-2 test-data)))
  (test/is (= 1880 ans2)))

;; ## Main

(comment
  (test/run-tests))

(defn -main [& args]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
