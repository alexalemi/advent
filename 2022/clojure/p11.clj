;; # 🎄 Advent of Code 2022 - Day 11 - Monkey in the Middle
(ns p11
  (:require [clojure.string :as str]
            [clojure.edn :as edn]
            [clojure.test :as test]))

(def data-string (str/split (slurp "../input/11.txt") #"\n\n"))

(def test-string (str/split "Monkey 0:
  Starting items: 79, 98
  Operation: new = old * 19
  Test: divisible by 23
    If true: throw to monkey 2
    If false: throw to monkey 3

Monkey 1:
  Starting items: 54, 65, 75, 74
  Operation: new = old + 6
  Test: divisible by 19
    If true: throw to monkey 2
    If false: throw to monkey 0

Monkey 2:
  Starting items: 79, 60, 97
  Operation: new = old * old
  Test: divisible by 13
    If true: throw to monkey 1
    If false: throw to monkey 3

Monkey 3:
  Starting items: 74
  Operation: new = old + 3
  Test: divisible by 17
    If true: throw to monkey 0
    If false: throw to monkey 1" #"\n\n"))

(defn divisible? [n]
  (fn [x] (= 0 (mod x n))))

(defn add [n]
  (fn [x] (+' x n)))

(defn mul [n]
  (fn [x] (*' x n)))

(defn square [x] (*' x x))

(def test-data
  {:items [[79 98]
           [54 65 75 74]
           [79 60 97]
           [74]]
   :monkey-specs
   [{:id 0
     :operation (mul 19)
     :test (divisible? 23)
     :true-monkey 2
     :false-monkey 3}
    {:id 1
     :operation (add 6)
     :test (divisible? 19)
     :true-monkey 2
     :false-monkey 0}
    {:id 2
     :operation square
     :test (divisible? 13)
     :true-monkey 1
     :false-monkey 3}
    {:id 3
     :operation (add 3)
     :test (divisible? 17)
     :true-monkey 0
     :false-monkey 1}]})

(def ^:dynamic *manage-worry* (fn [x] (quot x 3)))

(defn handle-item
  "Build out a handler from a monkey-spec"
  [{:keys [operation test true-monkey false-monkey]}]
  (fn [items item]
    (let [item (operation item)
          item (*manage-worry* item)]
      (if (test item)
        (update items true-monkey conj item)
        (update items false-monkey conj item)))))

(defn activate-monkey
  [[items counts] monkey-spec]
  (let [id (:id monkey-spec)]
    [(reduce (handle-item monkey-spec) (assoc items id []) (get items id))
     (update counts id +' (count (items id)))]))

(defn round
  "A round consists of each monkey acting on their items."
  [monkey-specs [items counts]]
  (reduce activate-monkey [items counts] monkey-specs))

;; ## Part 1

(defn part-1 [data]
  (binding [*manage-worry* (fn [x] (quot x 3))]
    (->> (iterate (partial round (:monkey-specs data)) [(:items data) (into [] (repeat (count (:items data)) 0))])
         (drop 1)
         (take 20)
         (last)
         (second)
         (sort >)
         (take 2)
         (apply *'))))

(test/deftest test-part-1
  (test/is (= (part-1 test-data) 10605)))

(def data (eval (edn/read-string (slurp "11.edn"))))

(def ans1 (part-1 data))
;; = 69918

;; ## Part 2

(defn part-2 [data]
  (binding [*manage-worry* (fn [x] (mod x (* 2 3 5 7 11 13 17 19 23)))]
    (->> (iterate (partial round (:monkey-specs data)) [(:items data) (into [] (repeat (count (:items data)) 0))])
         (drop 1)
         (take 10000)
         (last)
         (second)
         (sort >)
         (take 2)
         (apply *'))))

(test/deftest test-part-2
  (test/is (= 2713310158 (part-2 test-data))))

(def ans2 (part-2 data))
;; = 19573408701

;; ## Main

(defn -test [& _]
  (test/run-tests 'p11))

(defn -main [& _]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
