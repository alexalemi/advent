;; # 🎄 Advent Of Code 2022 - Day 3
;;
(ns p03
  (:require [clojure.string :as str]
            [clojure.set :as set]))

(def data-string (slurp "../input/03.txt"))

(defn lower-case? [s]
  (<= (int \a) (int s) (int \z)))

(defn score [x]
  (if (lower-case? x)
      (inc (- (int x) (int \a)))
      (+ 26 (inc (- (int x) (int \A))))))

(defn split-at-half [s]
  (map set (split-at (quot (count s) 2) s)))

(defn intersect [x]
  (apply set/intersection x))

;; ## Part 1

(def ans1
  (->> (str/split-lines data-string)
       (map (comp score first intersect split-at-half seq))
       (reduce +)))

;; ## Part 2

(def ans2
  (->> (str/split-lines data-string)
       (map (comp set seq))
       (partition 3)
       (map (comp score first intersect))
       (reduce +)))


(defn -main [& _]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
