;; # 🎄 Advent of Code - 2024 - Day 5

(ns p05
  (:require [clojure.string :as str]
            [clojure.test :as test]))

(def data-string (slurp "../input/05.txt"))
(def test-data-string "47|53
97|13
97|61
97|47
75|29
61|13
75|53
29|13
97|29
53|29
61|53
97|53
61|29
47|13
75|47
97|75
47|61
75|61
47|29
75|13
53|13

75,47,61,53,29
97,61,53,29,13
75,29,13
75,97,47,61,53
61,13,29
97,13,75,29,47")

(defn process [s]
 (let [[rules updates] (str/split s #"\n\n")
       rules (into #{} (->> rules
                         (str/split-lines)
                         (map (fn [s] (map read-string (str/split s #"\|"))))))
       updates (->> updates
                   (str/split-lines)
                   (map (fn [s] (map read-string (str/split s #",")))))]
   {:rules rules :updates updates}))

(def data (process data-string))
(def test-data (process test-data-string))

(defn sorter 
  ([data x] (sort (fn [x y] (contains? (data :rules) [x y])) x))
  ([data] (partial sorter data)))
  
(defn valid? 
  ([data x] (= x (sorter data x)))
  ([data] (partial valid? data)))

(defn middle [col] (first (drop (quot (count col) 2) col)))

(defn part-1 [data]
  (let [is-valid? (valid? data)]
    (transduce
      (comp
        (filter is-valid?)
        (map middle))
      +
      (data :updates))))

(def ans1 (part-1 data))

(test/deftest test-part-1
  (test/is (= 143 (part-1 test-data)))
  (test/is (= 4957 ans1)))

;; ## Part 2

(defn part-2 [data]
  (let [sorter (sorter data)
        is-valid? (valid? data)]
    (transduce
      (comp
        (filter (complement is-valid?))
        (map sorter)
        (map middle))
      +
      (data :updates))))


(def ans2 (part-2 data))

(test/deftest test-part-2
  (test/is (= 123 (part-2 test-data)))
  (test/is (= 6938 ans2)))

;; ## Main

(comment
  (test/run-tests))

(defn -main [& args]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
