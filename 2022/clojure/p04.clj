;; # 🎄 Advent of Code 2022 - Day 4
;;
(ns p04
  (:require [clojure.string :as str]))

(def data-string (slurp "../input/04.txt"))

(def data (for [line (str/split-lines data-string)]
            (for [elf (str/split line #",")]
               (map parse-long (str/split elf #"-")))))

;; ## Part 1

(defn fully-contains? [[[a b] [c d]]]
  (or (<= a c d b) (<= c a b d)))

(def ans1 (count (filter fully-contains? data)))


;; ## Part 2

(defn any-overlap? [[[a b] [c d]]]
  (or (<= a c b) (<= c a d)))

(def ans2 (count (filter any-overlap? data)))


(defn -main [& _]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
