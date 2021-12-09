(ns advent01
  (:require
   [clojure.test :as test]
   [clojure.string :as string]))


(defn process-string [inp]
  (->> inp
       string/split-lines
       (map read-string)
       (into [])))

(def data (process-string (slurp "../input/01.txt")))

(defn part-1 [data]
  (->> data
       (partition 2 1)
       (filter #(> (last %) (first %)))
       count))

(time (def ans1 (part-1 data)))

(def test-data (process-string "199
200
208
210
200
207
240
269
260
263"))


(test/deftest test-part-1
  (test/is (= (part-1 test-data) 7)))


(defn part-2 [data]
  (->> data
       (partition 3 1)
       (map #(reduce + %))
       part-1))

(time (def ans2 (part-2 data)))

(test/deftest test-part-2
  (test/is (= (part-2 test-data) 5)))

(test/run-tests)

(println)
(println "Part1:" ans1)
(println "Part2:" ans2)
