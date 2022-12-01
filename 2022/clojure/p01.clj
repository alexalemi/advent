; # 🎄 Advent of Code: 2022 Day 1
(ns p01
  (:require [clojure.string :as str]))

;; We have a bunch of calories eaten by each elf.
(def data-string (slurp "../input/01.txt"))

;; We want to process the data so that we have the sums of the numbers
;; for each of the elves.
(def data
  (mapv #(reduce + (mapv read-string (str/split-lines %)))
        (str/split data-string #"\n\n")))


;; For part1 we just need the max.
(def ans1 (reduce max data))

;; For part 2 we need the top 3.
(def ans2
  (->> data
      (sort >)
      (take 3)
      (reduce +)))
