;; # 🎄 Advent of Code 2023 - Day 1
(ns p01
  (:require [clojure.string :as str]
            [clojure.test :as test]))

; (def data-string (slurp "../input/01.txt"))

;(defn process-data [s]
;  (rest (str/split s #"\n\$ ")))

;(def data (process-data data-string))

;(def test-string "")

;(def test-data (process-data test-string))


(test/deftest test-part-1
 (test/is 1 1))

(def ans1 nil)

;; ## Part 2

(test/deftest test-part-2
  (test/is 1 1))

(def ans2 nil)

;;

(defn -test [_]
  (test/run-tests 'p07))

(defn -main [_]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))

