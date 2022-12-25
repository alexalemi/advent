;; # 🎄 Advent of Code 2022 - Day 25
;; Merry Christmas!
(ns p25
  (:require [clojure.test :as test]
            [clojure.string :as str]))

(defn ->ints [s]
  (map parse-long (re-seq #"\d+" s)))

(def data-string (slurp "../input/25.txt"))

(defn indexed [xs] (map-indexed vector xs))

(defn exp [x n]
  (loop [acc 1 n n]
    (if (zero? n) acc
        (recur (* x acc) (dec n)))))

(defn snafu [digits]
  (reduce +
          (for [[i digit] (indexed (reverse digits))]
            (* (exp 5 i)
               (case digit
                 \2 2
                 \1 1
                 \0 0
                 \- -1
                 \= -2)))))

(def tot (reduce + (map snafu (str/split-lines data-string))))

(defn base5 [x]
  (loop [i 20 x x out []]
    (let [digit (quot x (exp 5 i))
          rem (- x (* (exp 5 i) digit))]
      (if (pos? rem)
        (recur (dec i) rem (conj out digit))
        (drop-while (fn [x] (= 0 x)) (conj out digit))))))

(let [foo [1 2]]
  (rest foo))

(defn digit->snafu [x]
  (let [z (concat (reverse (base5 x)) [0 0 0 0])]
    (loop [digits z out []]
      (if-let [digit (first digits)]
        (case digit
          0 (recur (rest digits) (conj out \0))
          1 (recur (rest digits) (conj out \1))
          2 (recur (rest digits) (conj out \2))
          3 (recur (conj (drop 1 (rest digits)) (inc (first (rest digits)))) (conj out \=))
          4 (recur (conj (drop 1 (rest digits)) (inc (first (rest digits)))) (conj out \-))
          5 (recur (conj (drop 1 (rest digits)) (inc (first (rest digits)))) (conj out \0))
          6 (recur (conj (drop 1 (rest digits)) (inc (first (rest digits)))) (conj out \1)))
        (apply str (drop-while (fn [x] (= \0 x)) (reverse out)))))))

(digit->snafu tot)

;; ## Part 1

(def ans1 :undefined)

;; ## Part 2
;; A freebie as always

(def ans2 "Merry Christmas!!!")

;; ## Main

(defn -test [& _]
  (test/run-tests 'p25))

(defn -main [& _]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
