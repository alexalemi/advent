(ns advent02
  (:require
   [clojure.edn :as edn]
   [clojure.test :as test]
   [clojure.string :as string]))


(def data
  (map #(map edn/read-string (string/split % #"\t"))
       (string/split-lines (slurp "../input/02.txt"))))

(defn checksum
  "Compute the checksum of a line."
  [line]
  (- (apply max line) (apply min line)))


(defn part-1
  "Compute the result of part1."
  [data]
  (reduce + (map checksum data)))


(def ans1 (part-1 data))


(test/deftest test-part-1
  (let [test-data [[5 1 9 5] [7 5 3] [2 4 6 8]]]
    (test/is (= (map checksum test-data) [8 4 6]))
    (test/is (part-1 test-data) 18)))


(defn find-division [[a b]]
  (cond
    (= (mod a b) 0) (/ a b)
    (= (mod b a) 0) (/ b a)
    :else nil))


(defn dividers
  "Find the two numbers that are divisors."
  [line]
  (some find-division 
    (let [line-index (zipmap line (range))]
        (for [[x i] line-index
              [y j] line-index
              :when (< i j)]
          [x y]))))
             


(defn part-2 [data]
  (reduce + (map dividers data)))


(test/deftest test-part-2
  (let [test-data [[5 9 2 8] [9 4 7 3] [3 8 6 5]]]
    (test/is (= (map dividers test-data) [4 3 2]))
    (test/is (= (part-2 test-data) 9))))


(def ans2 (part-2 data))


(test/run-tests)

(println)
(println "Answer1:", ans1)
(println "Answer2:", ans2)

