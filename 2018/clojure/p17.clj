;; # 🎄 Advent of Code 2018 - Day 17
(ns p17
  (:require [clojure.string :as str]))

(defn s->ints [s]
  (mapv read-string (re-seq #"\d+" s)))

(defn process-one [s]
  (let [[one many] (str/split s #", ")]
    {(keyword (str (first one)))
     (first (s->ints one))
     (keyword (str (first many)))
     (s->ints many)}))


(def data
  (->> (slurp "../input/17.txt")
       (str/split-lines)
       (map process-one)))
