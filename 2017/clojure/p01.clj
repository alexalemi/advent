(ns advent01
  (:require
   [clojure.test :as test]
   [clojure.string :as string]))

(def data (string/trim (slurp "../input/01.txt")))

(defn part-1 [data] 
  (->> data
       (seq)
       (cons (last data))
       (partition 2 1)
       (map 
         (fn [[a b]] (if (= a b) (Character/digit a 10) 0)))
       (reduce +)))
        
(def ans1 (part-1 data))

(test/deftest test-part-1
  (test/is (= (part-1 "1122") 3))
  (test/is (= (part-1 "1111") 4))
  (test/is (= (part-1 "1234") 0))
  (test/is (= (part-1 "91212129") 9)))

(defn part-2 [data] 
  (let [n (count data)]
    (->> data
         (seq)
         (split-at (/ n 2))
         (apply interleave)
         (partition 2)
         (map 
          (fn [[a b]] (if (= a b) (* 2 (Character/digit a 10)) 0)))
         (reduce +))))

(def ans2 (part-2 data))

(test/deftest test-part-1
  (test/is (= (part-2 "1212") 6))
  (test/is (= (part-2 "1221") 0))
  (test/is (= (part-2 "123425") 4))
  (test/is (= (part-2 "123123") 12))
  (test/is (= (part-2 "12131415") 4)))

(test/run-tests)

(println)
(println "Part1:" ans1)
(println "Part2:" ans2)
