;; # 🎄 Advent Of Code 2022 - Day 3
;;
(ns p03
  (:require [clojure.string :as str]
            [clojure.set :as set]))

(def data-string (slurp "../input/03.txt"))

(defn lower-case? [s]
  (let [c (str s)]
   (= c (str/lower-case c))))

(defn score [x]
  (if (lower-case? x)
      (- (int x) 96)
      (- (int x) 64 -26)))


(defn split-at-half [s]
  (let [size (count s)
        half (quot size 2)]
     [(into #{} (take half s))
      (into #{} (drop half s))]))

(defn setify [s] (into #{} s))

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
       (map (comp setify seq))
       (partition 3)
       (map (comp score first intersect))
       (reduce +)))


(defn -main [& _]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
