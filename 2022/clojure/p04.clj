;; # 🎄 Advent of Code 2022 - Day 4
;;
(ns p04
  (:require [clojure.string :as str]))

(def data-string (slurp "../input/04.txt"))

(def data (for [line (str/split-lines data-string)]
            (for [elf (str/split line #",")]
               (map parse-long (str/split elf #"-")))))

;; ## Part 1

(defn fully-contains? [[[l1 r1] [l2 r2]]]
  (or (and (<= l1 l2 r1) (<= l1 r2 r1))
      (and (<= l2 l1 r2) (<= l2 r1 r2))))

(def ans1 (count (filter fully-contains? data)))


;; ## Part 2

(defn any-overlap? [[[l1 r1] [l2 r2]]]
  (or (<= l1 l2 r1) (<= l1 r2 r1)))

(def ans2 (count (filter any-overlap? data)))


(defn -main [& _]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
