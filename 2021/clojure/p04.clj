(ns day04
  (:require
   [clojure.test :as test]
   [clojure.string :as str]))

(def test-string "")

(defn process [inp]
  (str/split-lines inp))

(def data (process (slurp "../input/03b.txt")))
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
