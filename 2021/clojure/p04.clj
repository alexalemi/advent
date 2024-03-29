(ns day04
  (:require
   [clojure.test :as test]
   [clojure.set :as set]
   [clojure.string :as str]))

(def data-string (slurp "../input/04.txt"))
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
  [line] (read-string (str "[" line "]")))

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
     ; :boards (mapv board-to-map (reduce board-reducer [] (rest lines)))
     :boards (reduce board-reducer [] (rest lines))
     :seen #{}}))

(def data (process data-string))
(def test-data (process test-string))


(def N 5)
(defn ravel [x y] (+ y (* N x)))
(defn unravel [pk] [(quot pk N) (mod pk N)])


(defn index-of [vec v]
  (some identity (map-indexed (fn [i x] (if (= x v) i nil)) vec)))

(defn find-loc
  "Find the location of a value on the board, if any"
  [board x]
  (if-let [pk (index-of board x)]
    (unravel pk) nil))


(defn tally
  [board seen draw]
  (* draw (reduce + (set/difference (set board) seen))))


(defn score
  "Check to see if the board is a winner given the current draw."
  [seen draw board]
  (if-let [[x y] (find-loc board draw)]
    (let [aseen (conj seen draw)
          get-board (partial get board)]
      (cond
        (every? aseen (map get-board (for [v (range 5)] (ravel x v)))) (tally board aseen draw)
        (every? aseen (map get-board (for [v (range 5)] (ravel v y)))) (tally board aseen draw)))
    nil))

(defn turn
  [data]
  (let [{:keys [boards, moves, seen]} data
        draw (first moves)
        checker (partial score seen draw)
        points (some checker boards)]
    {:boards (filterv (complement checker) boards)
     :moves (rest moves)
     :seen (conj seen draw)
     :score points}))

(defn part-1 [data]
  (:score (first (drop-while (complement :score) (iterate turn data)))))

(time (def ans1 (part-1 data)))
(println)
(println "Part1:" ans1)

(test/deftest test-part-1
  (test/is (not (score #{} 7 (first (:boards test-data)))))
  (test/is (boolean (score #{22 13 17 11} 0 (first (:boards test-data)))))
  (test/is (boolean (score #{22 13 11 0} 17 (first (:boards test-data)))))
  (test/is (boolean (score #{13 2 9 10} 12 (first (:boards test-data)))))
  (test/is (= (part-1 test-data) 4512))
  (test/is (= (part-1 data) 41503)))

(defn part-2
  [data]
  (:score
   (first (drop-while
           (and (complement :score) (comp not-empty :boards))
           (iterate turn data)))))

(time (def ans2 (part-2 data)))
(println)
(println "Part2:" ans2)

(test/deftest test-part-2
  (test/is (= (part-2 test-data) 1924))
  (test/is (= (part-2 data) 3178)))

(test/run-tests)
