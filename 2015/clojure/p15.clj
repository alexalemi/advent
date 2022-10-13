(ns p15
  (:require [clojure.string :as str]))

(def data-string (slurp "../input/15.txt"))
(def test-string "Butterscotch: capacity -1, durability -2, flavor 6, texture 3, calories 8
Cinnamon: capacity 2, durability 3, flavor -2, texture -1, calories 3")

(defn to-num [x] (read-string x))


(defn process-line [line]
  (let [[_ name capacity durability flavor texture calories]
        (re-matches #"(\w+): capacity (-?\d+), durability (-?\d+), flavor (-?\d+), texture (-?\d+), calories (-?\d+)" line)]
   {:name name
    :capacity (to-num capacity)
    :durability (to-num durability)
    :flavor (to-num flavor)
    :texture (to-num texture)
    :calories (to-num calories)}))

(def data (map process-line (str/split-lines (str/trim data-string))))
(def test-data (map process-line (str/split-lines test-string)))
;; => ({:name "Butterscotch", :capacity -1, :durability -2, :flavor 6, :texture 3, :calories 8}
;;     {:name "Cinnamon", :capacity 2, :durability 3, :flavor -2, :texture -1, :calories 3}))

(defn scalar-mul [[a vec]]
  (mapv * (repeat a) vec))

(defn relu [x] (if (> x 0) x 0))

(defn score [data weights]
    (->> data
         (map (juxt :capacity :durability :flavor :texture))
         (map vector weights)
         (map scalar-mul)
         (apply mapv +)
         (map relu)
         (reduce *)))

(def two-vecs (for [x (range (inc 100))]
                (let [y (- 100 x)]
                  [x y])))

(def four-vecs (for [x (range (inc 100))
                     y (range (inc (- 100 x)))
                     z (range (inc (- 100 x y)))]
                  (let [w (- 100 x y z)]
                    [x y z w])))


(defonce ans1 (reduce max (map (partial score data) four-vecs)))
(println "Answer1:" ans1)

(defn calories [data weights]
    (->> data
        (map :calories)
        (map vector weights)
        (map (fn [[x y]] (* x y)))
        (reduce +)))

(defonce fivehundred-club (filter #(= 500 (calories data %)) four-vecs))
(defonce ans2 (reduce max (map (partial score data) fivehundred-club)))
(println "Answer2:" ans2)

(comment)
