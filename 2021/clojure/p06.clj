(ns day06
  (:require
   [clojure.test :as test]))

(def data-string (slurp "../input/06.txt"))
(def test-string "3,4,3,1,2")

(defn process
  "Read off a vector of numbers"
  [line] (frequencies (read-string (str "[" line "]"))))

(def data (process data-string))
(def test-data (process test-string))

(defn step
  "Do a single day of laternfish evolution."
  ([data] (step data {}))
  ([queue new]
   (if (empty? queue) new
       (let [[age count] (first queue)
             safe-+ (fnil + 0)]
         (if (= age 0)
           (recur (rest queue) (update (update new 6 safe-+ count) 8 safe-+ count))
           (recur (rest queue) (update new (dec age) safe-+ count)))))))

(defn count-after [data days]
  (reduce + (vals (nth (iterate step data) days))))

(defn part-1 [data]
  (count-after data 80))

(time (def ans1 (part-1 data)))
(println)
(println "Part1:" ans1)

(test/deftest test-part-1
  (test/is (= (step test-data) {2 2 3 1 0 1 1 1}))
  (test/is (= (part-1 test-data) 5934))
  (test/is (= (part-1 data) 379114)))

(defn part-2 [data]
  (count-after data 256))

(time (def ans2 (part-2 data)))
(println)
(println "Part2:" ans2)

(test/deftest test-part-2
  (test/is (= (part-2 test-data) 26984457539))
  (test/is (= (part-2 data) 1702631502303)))

(test/run-tests)
