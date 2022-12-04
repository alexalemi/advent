(ns p17
  (:require [clojure.string :as str]))

(def data-string (slurp "../input/17.txt"))
(def data (mapv read-string (str/split-lines data-string)))

(def test-data [20 15 10 5 5])
(def TARGET 150)

(defn power [s]
  (loop [[f & r] (seq s) p '(())]
    (if f (recur r (concat p (map (partial cons f) p)))
        p)))

(defn total [s] (reduce + s))


(defn num-combos [target set]
  (count (filter #(= target %) (map total (power set)))))

(comment
  (num-combos 25 test-data))

(defonce ans1 (num-combos TARGET data))
(println "Answer1:" ans1)

(defn num-min-combos [target set]
   (let [good-sets (filter (fn [[t _]] (= t target)) (map (juxt total count) (power set)))
         min-count (reduce min (map second good-sets))]
     (count (filter (fn [[_ c]] (= c min-count)) good-sets))))

(comment
  (num-min-combos 25 test-data))


(defonce ans2 (num-min-combos TARGET data))
(println "Answer2:" ans2)
