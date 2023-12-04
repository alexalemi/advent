;; # 🎄 Advent of Code 2023 - Day 4
(ns p04
  (:require [clojure.string :as str]
            [clojure.set :as set]))

(def data-string (slurp "../input/04.txt"))

(def test-string "Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53
Card 2: 13 32 20 16 61 | 61 30 68 82 17 32 24 19
Card 3:  1 21 53 59 44 | 69 82 63 72 16 21 14  1
Card 4: 41 92 73 84 69 | 59 84 76 51 58  5 54 83
Card 5: 87 83 26 28 32 | 88 30 70 12 93 22 82 36
Card 6: 31 18 13 56 72 | 74 77 10 23 35 67 36 11")

(defn parse-nums [s]
  (into #{} (map parse-long (str/split s #"\s+"))))

(defn process-card [s]
 (let [[_ game-id target card] (re-find #"Card\s+(\d+): ([ \d]+)\| ([ \d]+)" s)]
    [(parse-long game-id) {:goal (parse-nums (str/trim target))
                           :card (parse-nums (str/trim card))}]))

(defn process-data [s]
  (into {} (map process-card) (str/split-lines s)))

(def data (process-data data-string))
(def test-data (process-data test-string))

(defn pow-2 [n] (bit-shift-left 1 n))

(defn matches [{:keys [goal card]}]
  (count (set/intersection goal card)))

(defn points [card]
  (let [m (matches card)]
    (if (> m 0) (pow-2 (dec m)) 0)))

(defn part-1 [data]
  (reduce + (map points (vals data))))

(assert (= (part-1 test-data) 13))

(def ans1 (part-1 data))

(assert (= ans1 22193))

;; ## Part 2
;;
;; Now we need to figure out how many copies of each card we win

(comment
  (take 4 (drop 2 (range))))

(defn process-copies [data]
  (let [data (update-vals data matches)]
    (letfn [(winning-ids [id] (take (data id) (drop (inc id) (range))))]
      (loop [copies (update-vals data (constantly 1))
             id 1]
         (if-let [matches (data id)]
            (recur (merge-with + copies (zipmap (winning-ids id) (repeat (copies id)))) (inc id))
            copies)))))

(defn part-2 [data]
  (reduce + (vals (process-copies data))))

(assert (= (part-2 test-data) 30))

(def ans2 (part-2 data))

(assert (= ans2 5625994))

(defn -main []
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
