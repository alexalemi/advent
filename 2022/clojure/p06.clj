;; # 🎄 Advent of Code 2022 - Day 6
(ns p06
  (:require [clojure.test :as test]))

(def data-string (slurp "../input/06.txt"))

;; ## Part 1

(defn first-marker [length s]
  (->> (partition length 1 s)
       (keep-indexed
        (fn [i x]
          (when (apply distinct? x)
                (+ length i))))
       first))

(test/deftest test-part-1
  (test/are [loc s] (= loc (first-marker 4 s))
   5 "bvwbjplbgvbhsrlpgdmjqwftvncz"
   6 "nppdvjthqldpwncqszvftbrmjlhg"
   10 "nznrnfrfntjfmvfwmzdfjlvtqnbhcprsg"
   11 "zcfzfwzzqfrljwzlrfnpqdbhtmscgvjw"))

(def ans1 (first-marker 4 data-string))

;; ## Part 2

(test/deftest test-part-2
  (test/are [loc s] (= loc (first-marker 14 s))
    19 "mjqjpqmgbljsphdztnvjfqwrcgsmlb"
    23 "bvwbjplbgvbhsrlpgdmjqwftvncz"
    23 "nppdvjthqldpwncqszvftbrmjlhg"
    29 "nznrnfrfntjfmvfwmzdfjlvtqnbhcprsg"
    26 "zcfzfwzzqfrljwzlrfnpqdbhtmscgvjw"))

(def ans2 (first-marker 14 data-string))


;; ## Main

(defn -test [_]
  (test/run-all-tests))

(defn -main [_]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
