(ns advent07
  (:require
   [clojure.test :as test]
   [clojure.edn :as edn]))

(def test-string "16,1,2,0,4,2,7,1,2,14")
(def data-string (slurp "../input/07.txt"))

(defn read-vec [s]
  (edn/read-string (str "[" s "]")))

(def test-data (read-vec test-string))
(def data (read-vec data-string))

(defn median [x]
  (let [sorted (sort x)
        n (count sorted)
        half (quot n 2)]
    (nth sorted half)))

(defn abs [x] (max x (- x)))

(defn part-1 [data]
  (let [goal (median data)]
    (->> data
         (map (partial - goal))
         (map abs)
         (reduce +))))

(test/deftest part-1-test
  (test/is (= (median test-data) 2))
  (test/is (= (part-1 test-data) 37))
  (test/is (= (part-1 data) 354129)))

(time (def ans1 (part-1 data)))
(println)
(println "Answer 1:" ans1)

(defn triangular [n] (/ (* n (inc n)) 2))

(defn score [data goal]
  (->> data
        (map (partial - goal))
        (map abs)
        (map triangular)
        (reduce +)))

(defn part-2 [data]
  (let [left (reduce min data)
        right (reduce max data)]
    (->> (range left right)
         (map (partial score data))
         (reduce min))))


(test/deftest part-2-test
  (test/is (= (part-2 test-data) 168))
  (test/is (= (part-2 data) 98905973)))

(time (def ans2 (part-2 data)))
(println)
(println "Answer 2:" ans2)

(test/run-tests)
