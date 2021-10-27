(ns advent01
  (:require
   [clojure.test :refer [deftest is run-tests]]  
   [clojure.string :refer [trim]]))

(def data (slurp "../input/01.txt"))

(defn part-1 [data] 
  (->> data
       (trim)
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
(println "Part1: " ans1)


(deftest test-part-1
  (is (= (part-1 "1122") 3))
  (is (= (part-1 "1111") 4))
  (is (= (part-1 "1234") 0))
  (is (= (part-1 "91212129") 9)))

;(run-tests)


