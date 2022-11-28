(ns p24
  (:require [clojure.string :as str]
            [clojure.math.combinatorics :as combo]
            [clojure.set :as set]))

;; # Day 24 Sounds like a sort of packing problem, we have to split all of the
;; packages into three equal weight groups, but we want as few packages as
;; possible in the passenger compartment.
;;
;; If there are ties, we want the group
;; with the smallest "quantum entanglement" which is the product of their
;; weights.  Do this for the first group only.

(def data (mapv read-string (str/split-lines (slurp "../input/24.txt"))))
(def total (reduce + data))
  ;; => [1 2 3 5 7 13 17 19 23 29 31 37 41 43 53 59 61 67 71 73 79 83 89 97
  ;;     101 103 107 109 113]


(defn subsets [vals]
  (mapcat #(combo/combinations vals %) (range 1 (inc (count vals)))))

(defn valid-subset? [vals]
  (let [left (into [] (set/difference (into #{} data) vals))
        total (reduce + left)]
    (first (filter #(= (reduce + %) (/ total 2)) (subsets left)))))


(defn quantum-entanglement [vals]
  (reduce * vals))

(defonce ans1 (first (filter valid-subset? (filter #(= (reduce + %) (/ total 3)) (subsets data)))))
(println "Answer1:" (quantum-entanglement ans1))

(comment
  (def foo (first (filter valid-subset? (filter #(= (reduce + %) (/ total 3)) (subsets data)))))
  (println (quantum-entanglement foo)))

(defn valid-subset-2? [vals]
  (let [left (into [] (set/difference (into #{} data) vals))
        total (reduce + left)]
    (first (filter valid-subset? (filter #(= (reduce + %) (/ total 3)) (subsets left))))))

(defonce ans2 (first (filter valid-subset-2? (filter #(= (reduce + %) (/ total 4)) (subsets data)))))
(println "Answer2:" (quantum-entanglement ans2))
