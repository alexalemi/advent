;; # 🎄 Advent of Code 2022 - Day 5

(ns p05
  (:require [clojure.string :as str]))

(def data-string (slurp "../input/05.txt"))
(def test-string "    [D]
[N] [C]
[Z] [M] [P]
 1   2   3

move 1 from 2 to 1
move 3 from 1 to 3
move 2 from 2 to 1
move 1 from 1 to 2")

(defn s->lookup [s]
  (map-indexed vector s))

(def numbers (set "123456789"))

(defn normalize [s]
  (str/trim (str s)))

(defn process-move [s]
  (let [[_ x fm to] (re-matches #"move (\d+) from (\d+) to (\d+)" s)
        [x fm to] (map parse-long [x fm to])]
    [x fm to]))

(defn process [data-string]
  (let [[stacks moves] (map str/split-lines (str/split data-string #"\n\n"))
        cols (map first (filter (comp numbers second) (map-indexed vector (last stacks))))
        stacks (butlast stacks)
        stacks (apply map list (for [stack stacks]
                                  (map #(normalize (get stack %)) cols)))
        stacks (into [] (for [stack stacks]
                          (drop-while empty? stack)))
        moves (map process-move moves)]
    {:stacks stacks :moves moves}))

(def data (process data-string))
(def test-data (process test-string))

(defn move [stacks [x fm to]]
  (let [fm (dec fm)
        to (dec to)
        [top rest] (split-at x (get stacks fm))]
    (assert (<= x (count (get stacks fm))) "Too many!")
    (-> stacks
        (assoc fm rest)
        (update to into top))))


(defn run [{:keys [stacks moves]}]
  (reduce move stacks moves))

;; Let's test the testcase

(let [{:keys [stacks moves]} test-data]
 (reductions move stacks moves))


;; ## Part 1

(def ans1 (apply str (map first (run data))))

;; ## Part 2

(defn move-1001 [stacks [x fm to]]
  (let [fm (dec fm)
        to (dec to)
        [top rest] (split-at x (get stacks fm))]
    (assert (<= x (count (get stacks fm))) "Too many!")
    (-> stacks
        (assoc fm rest)
        (update to into (reverse top)))))

;; Let's test on test-data

(reduce move-1001 (:stacks test-data) (:moves test-data))


(def ans2 (apply str (map first (reduce move-1001 (:stacks data) (:moves data)))))


(defn -main [_]
  (println "Answer1: " ans1)
  (println "Answer2: " ans2))
