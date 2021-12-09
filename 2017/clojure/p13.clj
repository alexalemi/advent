(ns advent13
  (:require
   [clojure.test :as test]
   [clojure.string :as str]))

(def test-string "0: 3
1: 2
4: 4
6: 4")
(def data-string (slurp "../input/13.txt"))

(defn process-line [line]
  (as-> line x
    (let [[left right] (map read-string (str/split x #":"))]
      [left right])))

(defn process [s]
  (->> (str/split-lines s)
       (map process-line)
       (into {})))

(def test-data (process test-string))
(def data (process data-string))

(defn severity [range depth]
  (if (= 0 (mod range (* (dec depth) 2)))
    (* range depth)
    nil))

(defn part-1 [data]
  (->> data
       (map (fn [[k v]] (severity k v)))
       (filter some?)
       (reduce +)))

(test/deftest test-part-1
  (test/is (= (part-1 test-data) 24))
  (test/is (= (part-1 data) 788)))

(time (def ans1 (part-1 data)))
(println)
(println "Answer 1:" ans1)

(defn not-caught [data offset]
  (reduce
   (fn [_ [k v]]
     (if (nil? (severity (+ k offset) v)) true (reduced false)))
   true data))

(defn part-2 [data]
  (first (filter (partial not-caught data) (range))))

(test/deftest test-part-2
  (test/is (= (part-2 test-data) 10))
  (test/is (= (part-2 data) 3905748)))

(time (def ans2 (part-2 data)))
(println)
(println "Answer 2:" ans2)

(test/run-tests)
