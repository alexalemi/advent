;; # 🎄 Advent of Code 2022 - Day 12 - Hill Climbing Algorithm
(ns p12
  (:require [clojure.string :as str]
            [clojure.test :as test]
            [util :as util]))

(def data-string (slurp "../input/12.txt"))

(def test-string "Sabqponm
abcryxxl
accszExk
acctuvwj
abdefghi")

(defn enumerate [x] (map vector (range) x))

(defn height [c]
  (- (int c) (int \a)))

(defn process [s]
  (reduce
   (fn [data [loc c]]
     (case c
       \S (-> data
              (assoc :start loc)
              (update :board assoc loc (height \a)))
       \E (-> data
              (assoc :goal loc)
              (update :board assoc loc (height \z)))
       (update data :board assoc loc (height c))))
   {}
   (for [[y row] (enumerate (str/split-lines s))
         [x col] (enumerate row)]
     [[x y] col])))

(def data (process data-string))
(def test-data (process test-string))

;; ## Part 1

(defn raw-neighbors [[x y]]
  [[(inc x) y]
   [(dec x) y]
   [x (inc y)]
   [x (dec y)]])

(defn manhattan [[x1 y1] [x2 y2]]
  (+ (abs (- x1 x2))
     (abs (- y1 y2))))

(defn neighbors [board loc]
  (let [h0 (board loc)]
    (filter (fn [x] (<= (- (get board x ##Inf) h0) 1)) (raw-neighbors loc))))

(defn heuristic [goal loc]
  (partial manhattan goal))

(defn shortest-path [data]
  (dec
   (count
    (util/a-star
     (:start data)
     #{(:goal data)}
     (constantly (constantly 1))
     (partial neighbors (:board data))
     (partial manhattan (:goal data))))))

(test/deftest test-part-1
  (test/is (= 31 (shortest-path test-data))))

(def ans1 (shortest-path data))

;; ## Part 2

(defn cost [board]
  (fn [_]
    (fn [to]
      (if (= (board to) 0) 0 1))))

(defn shortest-path-from-a [data]
  (count
   (drop-while
    (fn [x] (= 0 ((:board data) x)))
    (util/a-star
     (:start data)
     #{(:goal data)}
     (cost (:board data))
     (partial neighbors (:board data))
     (partial manhattan (:goal data))))))

(test/deftest test-part-2
  (test/is (= 29 (shortest-path-from-a test-data))))

(def ans2 (shortest-path-from-a data))

;; ## Main

(defn -test [& _]
  (test/run-tests 'p12))

(defn -main [& _]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
