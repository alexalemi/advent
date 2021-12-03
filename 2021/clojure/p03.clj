(ns advent.day03
  (:require
   [clojure.test :as test]
   [clojure.string :as str]))

(def test-string "00100
11110
10110
10111
10101
01111
00111
11100
10000
11001
00010
01010")

(defn process [inp]
  (str/split-lines inp))

(def data (process (slurp "../input/03b.txt")))
(def test-data (process test-string))

(defn most-common-leading
  [data]
  (let [freq (frequencies (map first data))]
    (if (> (freq \0 0) (freq \1 0)) \0 \1)))

(defn to-number
  [x]
  (if (= x \0) 0 1))

(defn seq-to-bin
  ([s] (seq-to-bin s 0))
  ([s x]
   (if (empty? s)
     x
     (recur (rest s) (+ (* 2 x) (first s))))))

(defn gamma
  ([data] (gamma data {\0 0 \1 1} 0))
  ([data lookup] (gamma data lookup 0))
  ([data lookup x]
   (if (= (count (first data)) 0) x
     (recur
       (map rest data)
       lookup
       (+ (* 2 x) (lookup (most-common-leading data)))))))

(defn epsilon
  [data] (gamma data {\0 1 \1 0}))
 

(defn part-1 [data]
  (* (gamma data) (epsilon data)))

(time (def ans1 (part-1 data)))
(println)
(println "Part1:" ans1)


(test/deftest test-part-1
  (test/is (= (gamma test-data) 22))
  (test/is (= (epsilon test-data) 9))
  (test/is (= (part-1 test-data) 198)))

(defn from-binary [x bits]
  (if (empty? bits) x
    (recur (+ (* x 2) (first bits)) (rest bits))))

(defn oxygen
  ([data] (oxygen data most-common-leading 0))
  ([data selfun x]
   (if (= 1 (count data)) (from-binary x (map {\0 0 \1 1} (first data)))
      (let [next (selfun data)]
          (recur
            (map rest (filter #(= (first %) next) data))
            selfun
            (+ (* 2 x) ({\0 0 \1 1} next)))))))

(defn co2 [data] (oxygen data (comp #(if (= % \0) \1 \0) most-common-leading) 0))

(defn part-2
  [data]
  (* (oxygen data) (co2 data)))

(part-2 test-data)

(time (def ans2 (part-2 data)))
(println)
(println "Part2:" ans2)

(test/deftest test-part-2
  (test/is (= (part-2 test-data) 230)))

(test/run-tests)
