;; # 🎄 Advent of Code - 2024 - Day 2
(ns p02
  [:require [clojure.test :as test]
            [clojure.string :as str]])
            
;; ## Load data

(defonce data-string (slurp "../input/02.txt"))
(def test-data-string "7 6 4 2 1
1 2 7 8 9
9 7 6 2 1
1 3 2 4 5
8 6 4 4 1
1 3 6 7 9")

(defn split [s] (map read-string (str/split s #"\s+")))

(def pairs (map split 
             (str/split-lines data-string)))

(defn parse [s]
  (->> (str/split-lines s)
       (map split)))

(def data (parse data-string))
(def test-data (parse test-data-string))

(defn spread [f] (fn [x] (apply f x)))

(defn all-increasing [l]
  (->> (partition 2 1 l)
       (map (spread <))
       (every? true?)))

(defn all-decreasing [l]
  (->> (partition 2 1 l)
       (map (spread >))
       (every? true?)))

(defn at-least-one-at-most-three [l]
  (->> (partition 2 1 l)
       (map (spread -))
       (map abs)
       ((fn [xs] 
          (and (>= (apply min xs) 1)
               (<= (apply max xs) 3))))))

(defn safe [s]
  (and (or (all-increasing s) (all-decreasing s)) 
       (at-least-one-at-most-three s)))

(defn part-1 [s]
  (count (filter true? (map safe s))))

(test/deftest test-part-1
  (test/is (= 2 (part-1 test-data)))
  (test/is (= 359 (part-1 data))))

(def ans1 (part-1 data))

(defn drop-1 [s]
  (for [n (range (count s))]  
    (concat (take n s) (drop (inc n) s))))

(defn safe-1 [s]
  (or (safe s) (some true? (map safe (drop-1 s)))))

(defn part-2 [s]
  (count (filter true? (map safe-1 s))))

(test/deftest test-part-2
  (test/is (= 4 (part-2 test-data)))
  (test/is (= 418 (part-2 data))))

(def ans2 (part-2 data))

(comment
  (test/run-tests))

(defn -main [& args]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))

