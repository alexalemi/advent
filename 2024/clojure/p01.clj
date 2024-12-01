;; # 🎄 Advent of Code - 2024 - Day 1
(ns p01
  [:require [clojure.test :as test]
            [clojure.string :as str]])
            
;; ## Load data

(defonce data-string (slurp "../input/01.txt"))
(def test-data "3   4
4   3
2   5
1   3
3   9
3   3")

(defn split [s] (map read-string (str/split s #"\s+")))

(def pairs (map split 
             (str/split-lines data-string)))


(defn part-1 [s]
  (->> (str/split-lines s)
       (map split)
       (apply map list)
       (map sort)
       (apply map -)
       (map abs)
       (reduce +)))
      

(test/deftest test-part-1
  (test/is (= 11 (part-1 test-data))))

(def ans1 (part-1 data-string))

(defn part-2 [s]
  (let [[first-list second-list]
        (->> (str/split-lines s)
          (map split)
          (apply map list)
          (map sort))
        counts (frequencies second-list)]
    (reduce + (map (fn [x] (* x (get counts x 0))) first-list))))


(test/deftest test-part-2
  (test/is (= 31 (part-2 test-data))))

(def ans2 (part-2 data-string))


(comment
  (test/run-tests))

(defn -main [& args]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))

