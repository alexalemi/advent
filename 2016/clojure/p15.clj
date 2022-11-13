(ns p15
  (:require [clojure.string :as str]))

(def data-string (slurp "../input/15.txt"))
(def test-string "Disc #1 has 5 positions; at time=0, it is at position 4.
Disc #2 has 2 positions; at time=0, it is at position 1.")

(defn to-int [x] (read-string x))

(defn parse-line [line]
  (let [[_ id size pos] (re-matches #"Disc #(\d+) has (\d+) positions; at time=0, it is at position (\d+)." line)]
    {:id (to-int id) 
     :size (to-int size) 
     :pos (to-int pos)}))
  
(def data (mapv parse-line (str/split-lines data-string)))
(def test-data (mapv parse-line (str/split-lines test-string)))


(defn falls-through? [t {:keys [id pos size]}]
  (= 0 (mod (+ t id pos) size)))

(defn first-time [data]
  (first (filter #(every? (partial falls-through? %) data) (range))))

(defonce ans1 (first-time data))
(println "Answer1:" ans1)


(defonce ans2 (first-time (conj data {:id (inc (count data)) :pos 0 :size 11})))
(println "Answer2:" ans2)
