(ns day04
  (:require
   [clojure.test :as test]
   [clojure.edn :as edn]
   [clojure.string :as str]))

(def test-string "7,4,9,5,11,17,23,2,0,14,21,24,10,16,13,6,15,25,12,22,18,20,8,19,3,26,1

22 13 17 11  0
 8  2 23  4 24
21  9 14 16  7
 6 10  3 18  5
 1 12 20 15 19

 3 15  0  2 22
 9 18 13 17  5
19  8  7 25 23
20 11 10 24  4
14 21 16 12  6

14 21 17 24  4
10 16 15  9 19
18  8 23 26 20
22 11 13  6  5
 2  0 12  3  7")


(defn read-vec
  "Read off a vector of numbers"
  [line] (edn/read-string (str "[" line "]")))

(defn board-reducer
 "Read off a single board." 
 [boards line]
 (if 
    ; if the initial string is an empty one create new board
    (empty? line) (conj boards []) 
    (conj (pop boards) (into (peek boards) (read-vec line)))))

(defn process [s]
  (let [lines (str/split-lines s)]
    {:moves (read-vec (first lines))
     :boards (reduce board-reducer [] (rest lines))}))

(def data (process (slurp "../input/04.txt")))
(def test-data (process test-string))

(defn part-1 [data]
  0)

(time (def ans1 (part-1 data)))
(println)
(println "Part1:" ans1)

(test/deftest test-part-1
  (test/is (= (part-1 test-data) 198)))


(defn part-2
  [data]
  0)

(part-2 test-data)

(time (def ans2 (part-2 data)))
(println)
(println "Part2:" ans2)

(test/deftest test-part-2
  (test/is (= (part-2 test-data) 230)))

(test/run-tests)
