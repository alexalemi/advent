;; # 🎄 Advent of Code 2018 - Day 25 - Four-Dimensional Adventure
;; Clustering algorithm.
(ns p25
  (:require [clojure.set :as set]
            [clojure.test :as test]))

(def data-string (slurp "../input/25.txt"))

(def test-strings [" 0,0,0,0
 3,0,0,0
 0,3,0,0
 0,0,3,0
 0,0,0,3
 0,0,0,6
 9,0,0,0
12,0,0,0"
                   "-1,2,2,0
0,0,2,-2
0,0,0,-2
-1,2,0,0
-2,-2,-2,2
3,0,2,-1
-1,3,2,2
-1,0,-1,0
0,2,1,-2
3,0,0,0"
                   "1,-1,0,1
2,0,-1,0
3,2,-1,0
0,0,3,1
0,0,-1,-1
2,3,-2,0
-2,2,0,0
2,-2,0,-1
1,-1,0,-1
3,2,0,2"
                   "1,-1,-1,-2
-2,-2,0,1
0,2,1,3
-2,3,-2,1
0,2,3,-2
-1,-1,1,-2
0,-2,-1,0
-2,2,3,-1
1,2,2,0
-1,-2,0,-2"])

(defn ->ints [s]
  (map parse-long (re-seq #"-?\d+" s)))

(defn process [s]
  (partition 4 (->ints s)))

(def data (process data-string))

(def test-data (map process test-strings))

;; ## Logic
;; Alright, for this challenge we have to implement a clustering algorithm based on manhattan distance.
;; I'm going to try to implement this as a reduction operation.

(defn manhattan [pos1 pos2]
  (reduce + (map abs (map - pos1 pos2))))

(defn inside? [point cluster]
  (<= (apply min (map (partial manhattan point) cluster)) 3))

(defn absorb [clusters point]
  (let [{inside true outside false} (group-by (partial inside? point) clusters)]
    (conj outside
          (apply set/union #{point} inside))))

(defn cluster [points]
  (reduce absorb #{} points))

(defn num-clusters [points]
  (count (cluster points)))

;; ## Part 1

(test/deftest test-part-1
  (test/is (= [2 4 3 8] (map num-clusters test-data))))

(def ans1 (time (num-clusters data)))

;; ## Part 2

(def ans2 "Merry Christmas!!!")

;; ## Main

(defn -test [& _]
  (test/run-tests 'p25))

(defn -main [& _]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
