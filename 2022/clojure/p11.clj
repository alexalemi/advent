;; # 🎄 Advent of Code 2022 - Day 11 - Monkey in the Middle
(ns p11
  (:require [clojure.string :as str]))

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

(defn divisible? [x n]
  (= 0 (mod x n)))

(def test-data
  {:items [[79 98]
           [54 65 75 74]
           [79 60 97]
           [74]]
   :monkey-specs
   [{:id 0
     :operation (fn [x] (* x 19))
     :test (fn [x] (divisible? x 23))
     :true-monkey 2
     :false-monkey 3}
    {:id 1
     :operation (fn [x] (+ x 6))
     :test (fn [x] (divisible? x 19))
     :true-monkey 2
     :false-monkey 0}
    {:id 2
     :operation (fn [x] (* x x))
     :test (fn [x] (divisible? x 13))
     :true-monkey 1
     :false-monkey 3}
    {:id 3
     :operation (fn [x] (+ x 3))
     :test (fn [x] (divisible? x 17))
     :true-monkey 0
     :false-monkey 1}]})

(defn handle-item
  "Build out a handler from a monkey-spec"
  [{:keys [operation test true-monkey false-monkey]}]
  (fn [items item]
    (let [item (operation item)
          item (quot item 3)] ;gets bored])
      (if (test item)
        (update items true-monkey conj item)
        (update items false-monkey conj item)))))

(defn activate-monkey
  [[items counts] monkey-spec]
  (let [id (:id monkey-spec)]
    [(reduce (handle-item monkey-spec) (assoc items id []) (get items id))
     (update counts id + (count (items id)))]))

(defn round
  "A round consists of each monkey acting on their items."
  [monkey-specs [items counts]]
  (reduce activate-monkey [items counts] monkey-specs))

(def history (take 20 (drop 1 (iterate (partial round (:monkey-specs test-data)) [(:items test-data) [0 0 0 0]]))))
