;; # 🎄 Advent of Code 2023 - Day 1
(ns p01
  (:require [clojure.string :as str]
            [clojure.test :as test]))

(def data (str/split-lines (slurp "../input/01.txt")))

;; It looks like for this puzzle we need to process lines of text that
;; have some digits embedded in them, so I'll create a dictionary mapping
;; the characters to their respective values.

(def digits {\1 1 \2 2 \3 3 \4 4 \5 5 \6 6 \7 7 \8 8 \9 9 \0 0})

;; We'll write a function to extract the calibration value from each line

(defn calibration-value [s]
  (->> s
       (filter digits)
       ((juxt first last))
       (map digits)
       ((fn [[x y]] (+ (* 10 x) y)))))

(def test-data (str/split-lines "1abc2
pqr3stu8vwx
a1b2c3d4e5f
treb7uchet"))

(defn part-1 [data]
  (transduce (map calibration-value) + data))

(test/deftest test-part-1
  (test/is (part-1 test-data) 142))

(def ans1 (part-1 data))

;; ## Part 2
;;
;; For part two, there are also words inside the stream that we need to catch.

(def test-data-2 (str/split-lines "two1nine
eightwothree
abcone2threexyz
xtwone3four
4nineeightseven2
zoneight234
7pqrstsixteen"))

(def digits-and-words
  {"1" 1 "2" 2 "3" 3 "4" 4 "5" 5 "6" 6 "7" 7 "8" 8 "9" 9 "0" 0
   "o" 1 "tw" 2 "thr" 3 "f" 4 "fiv" 5 "six" 6 "s" 7 "eigh" 8 "ni" 9})

(defn new-calibration-value [x]
  (->> x
       (re-seq #"\d|o(?=ne)|tw(?=o)|thr(?=ee)|f(?=our)|fiv(?=e)|six|s(?=even)|eigh(?=t)|ni(?=ne)")
       ((juxt first last))
       (map digits-and-words)
       ((fn [[x y]] (+ (* 10 x) y)))))

(defn part-2 [data]
  (transduce (map new-calibration-value) + data))

(test/deftest test-part-2
  (test/is (part-2 test-data-2) 281))

(def ans2 (part-2 data))

;;

(defn -test []
  (test/run-tests 'p01))

(defn -main []
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
