;; # Advent of Code 2025 - Day 2

(ns p02
  (:require [clojure.test :as test] 
            [clojure.string :as str]))

(def data-string (slurp "../input/02.txt"))
(def test-string "11-22,95-115,998-1012,1188511880-1188511890,222220-222224,
1698522-1698528,446443-446449,38593856-38593862,565653-565659,
824824821-824824827,2121212118-2121212124")

(defn process [s]
 (map
   (fn [row] (map parse-long (str/split (str/trim row) #"-")))
   (str/split s #",")))

(def test-data (process test-string))
(def data (process data-string))

(defn repeats-block? [s x]
  (let [l (count s)]
    (and (zero? (mod l x))
      (= s (apply str (repeat (/ l x) (subs s 0 x)))))))

(defn invalid? [n]
  (let [s (str n)
        l (count s)]
   (and (even? l)
        (repeats-block? s (quot l 2)))))

(defn solve [pred data]
  (->> data
       (mapcat (fn [[lo hi]] (range lo (inc hi))))
       (filter pred)
       (reduce +)))

(def part-1 (partial solve invalid?))

(test/deftest test-part-1
  (test/is (= 1227775554 (part-1 test-data))))

(def ans-1 (part-1 data))


(defn invalid?-2 [n]
  (let [s (str n)]
    (some (partial repeats-block? s) 
          (range 1 (inc (quot (count s) 2))))))
    

(def part-2 (partial solve invalid?-2))

(test/deftest test-part-2
  (test/is (= 4174379265 (part-2 test-data))))

(def ans-2 (part-2 data))

(defn -main []
  (println "Answer 1: " ans-1)
  (println "Answer 2: " ans-2))




