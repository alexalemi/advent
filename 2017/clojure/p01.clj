(ns advent01
  (:require
   [clojure.test :refer [deftest is run-tests]]  
   [clojure.string :refer [trim]]))

(def data (trim (slurp "../input/01.txt")))

(defn part-1 [data] 
  (->> data
       (seq)
       (cons (last data))
       (partition 2 1)
       (map 
         (fn [pair] 
           (if (= (first pair) (second pair))
               (Character/digit (first pair) 10)
               0)))
       (reduce +)))
        
(def ans1 (part-1 data))

(deftest test-part-1
  (is (= (part-1 "1122") 3))
  (is (= (part-1 "1111") 4))
  (is (= (part-1 "1234") 0))
  (is (= (part-1 "91212129") 9)))

(defn part-2 [data] 
  (let [n (count data)]
    (->> data
         (seq)
         (split-at (/ n 2))
         (apply interleave)
         (partition 2)
         (map 
          (fn [pair] 
            (if (= (first pair) (second pair))
                (* 2 (Character/digit (first pair) 10))
                0)))
         (reduce +))))

(def ans2 (part-2 data))

(deftest test-part-1
  (is (= (part-2 "1212") 6))
  (is (= (part-2 "1221") 0))
  (is (= (part-2 "123425") 4))
  (is (= (part-2 "123123") 12))
  (is (= (part-2 "12131415") 4)))

(run-tests)

(println)
(println "Part1:" ans1)
(println "Part2:" ans2)
