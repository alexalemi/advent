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
  ([data] (gamma data []))
  ([data final]
   (if (= (count (first data)) 0) (map to-number final)
     (recur
       (map rest data)
       (conj final (most-common-leading data))))))


(defn opposite
   [x] (if (= x \0) \1 \0))

(defn opposites
  [seq]
  (map #(- 1 %) seq))


(defn part-1 [data]
  (let [g-bin (gamma data)
        e-bin (opposites g-bin)]
   (* (seq-to-bin g-bin) (seq-to-bin e-bin))))

(time (def ans1 (part-1 data)))
(println)
(println "Part1:" ans1)


(test/deftest test-part-1
  (test/is (= (part-1 test-data) 198)))

(defn oxygen
  ([data] (oxygen data []))
  ([data final]
   (if (= 1 (count data)) (map to-number (into final (first data)))
     (let [next (most-common-leading data)]
       (recur
         (map rest (filter #(= (first %) next) data))
         (conj final next))))))

(defn co2
  ([data] (co2 data []))
  ([data final]
   (if (= 1 (count data)) (map to-number (into final (first data)))
     (let [next (opposite (most-common-leading data))]
       (recur
         (map rest (filter #(= (first %) next) data))
         (conj final next))))))

(defn part-2
  [data]
  (let [o (seq-to-bin (oxygen data))
        c (seq-to-bin (co2 data))]
    (* o c)))

(part-2 test-data)

(time (def ans2 (part-2 data)))
(println)
(println "Part2:" ans2)

(test/deftest test-part-2
  (test/is (= (part-2 test-data) 230)))

(test/run-tests)
