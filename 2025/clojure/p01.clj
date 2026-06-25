;; # Advent of Code 2025 - Day 1
(ns p01
  [:require [clojure.test :as test]
   [clojure.string :as str]])

(def data-string (str/trim (slurp "../input/01.txt")))
(def test-string "L68
L30
R48
L5
R60
L55
L1
L99
R14
L82")

(defn process [s]
  (map
   (fn [row] [(keyword (subs row 0 1))
              (parse-long (subs row 1))])
   (str/split s #"\n")))

(def data (process data-string))
(def test-data (process test-string))

(defn step [loc [dir amt]]
  (mod ((if (= dir :L) - +) loc amt) 100))

(defn part-1 [data]
  (count (filter zero? (reductions step 50 data))))

(test/deftest test-part-1
  (test/is (= 3 (part-1 test-data))))

(def ans-1 (part-1 data))

;; ## Part 2

(defn part-2 [data]
  (part-1
   (mapcat
    (fn [[dir amt]] (repeat amt [dir 1]))
    data)))

(test/deftest test-part-2
  (test/is (= 6 (part-2 test-data))))

(def ans-2 (part-2 data))

(defn -main []
  (println "Answer 1:" ans-1)
  (println "Answer 2:" ans-2))


