(ns day05
  (:require
   [clojure.test :as test]
   [clojure.edn :as edn]
   [clojure.string :as str]))

(def data-string (slurp "../input/05.txt"))
(def test-string "0,9 -> 5,9
8,0 -> 0,8
9,4 -> 3,4
2,2 -> 2,1
7,0 -> 7,4
6,4 -> 2,0
0,9 -> 2,9
3,4 -> 1,4
0,0 -> 8,8
5,5 -> 8,2")

(defn read-vec
  "Read off a vector of numbers"
  [line] (edn/read-string (str "[" line "]")))

(defn process-line [line]
  (zipmap [:from :upto]
          (map read-vec (str/split line #"->"))))

(defn process [s]
  (let [lines (str/split-lines s)]
    (mapv process-line lines)))

(def data (process data-string))
(def test-data (process test-string))

(defn same-coord? [segment which]
  (= ((:from segment) which) ((:upto segment) which)))
(defn horizontal? [segment]
  (same-coord? segment 1))
(defn vertical? [segment]
  (same-coord? segment 0))

(defn shrink-segment [segment]
  (let [{:keys [from upto]} segment
        [x1 y1] from
        [x2 y2] upto
        dx (compare x2 x1)
        dy (compare y2 y1)]
    (if (= from upto) nil
        (assoc segment :from [(+ x1 dx) (+ y1 dy)]))))

(defn positions
  "Get all of the positions in the line."
  [segment]
  (map :from (take-while some? (iterate shrink-segment segment))))

(defn safe-update
  [world loc]
  (assoc world loc (inc (get world loc 0))))

(defn draw-line
  [world line]
  (reduce safe-update world (positions line)))

(defn part-1 [data]
  (let [lines (filter (some-fn horizontal? vertical?) data)
        world (reduce draw-line {} lines)]
    (count (filter #(> (val %) 1) world))))

(time (def ans1 (part-1 data)))
(println)
(println "Part1:" ans1)

(test/deftest test-part-1
  (test/is (= (part-1 test-data) 5))
  (test/is (= (part-1 data) 5147)))

(defn part-2 [lines]
  (let [world (reduce draw-line {} lines)]
    (count (filter #(> (val %) 1) world))))

(time (def ans2 (part-2 data)))
(println)
(println "Part2:" ans2)

(test/deftest test-part-2
  (test/is (= (part-2 test-data) 12))
  (test/is (= (part-2 data) 16925)))

(test/run-tests)
