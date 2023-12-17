;; # Advent of Code Day 17
(ns p17
  (:require [clojure.string :as str]
            [util :as util]
            [clojure.math :as math]))

(def data-string (slurp "../input/17.txt"))
(def test-string "2413432311323
3215453535623
3255245654254
3446585845452
4546657867536
1438598798454
4457876987766
3637877979653
4654967986887
4564679986453
1224686865563
2546548887735
4322674655533")

(defn enumerate [col]
  (map list (range) col))

(def numbers
  {\0 0 \1 1 \2 2 \3 3 \4 4
   \5 5 \6 6 \7 7 \8 8 \9 9})

(defn ->data [s]
  (into {}
    (for [[row line] (enumerate (str/split-lines s))
          [col c] (enumerate line)]
      [[row col] (numbers c)])))

(def data (->data data-string))
(def test-data (->data test-string))

(defn max-of
  ([] -1)
  ([col] (apply max col)))

(defn extent [board]
  [(reduce max (map first (keys board)))
   (reduce max (map second (keys board)))])

(def start [[0 0] nil 3])
(defn goal? [board [[x y]]]
    (let [[Y X] (extent board)]
        (and (= y Y )(= x X))))
(defn up [[y x]]
    [(dec y) x])
(defn down [[y x]]
    [(inc y) x])
(defn right [[y x]]
    [y (inc x)])
(defn left [[y x]]
    [y (dec x)])

(def LIVES 2)

(defn maybe-add [cond xs x]
    (if cond (conj xs x) xs))

(defn inside? [board [[y x]]]
    (let [[Y X] (extent board)]
        (and (<= 0 x X) (<= 0 y Y))))

(defn raw-neighbors [[loc dir]]
    [[loc dir]])

(def dir->func
  {:right right
   :left left
   :up up
   :down down})

(defn raw-neighbors [[loc dir]]
    (let [dirs (case dir
                  nil [:right :down]
                  :up [:right :left]
                  :down [:right :left]
                  :left [:up :down]
                  :right [:up :down])]
      (apply concat
       (for [dir dirs]
        (map (fn [x] [x dir]) (take 3 (rest (iterate (dir->func dir) loc))))))))

(defn manhattan [[y1 x1] [y2 x2]]
  (+ (abs (- y1 y2)) (abs (- x2 x1))))

(defn heuristic [board [loc]]
  (let [[Y X] (extent board)]
    (manhattan [Y X] loc)))

(defn cost [board [start-loc _]]
  (fn [[loc dir]]
    (transduce
     (map board)
     +
     (let [d (manhattan start-loc loc)]
        (take d (rest (iterate (dir->func dir) start-loc)))))))

(defn neighbors [data x]
  (filter (partial inside? data) (raw-neighbors x)))

(defn part-1 [data]
    (let [start start
          goal? (partial goal? data)
          cost (partial cost data)
          neighbors (partial neighbors data)
          heuristic (partial heuristic data)]
      (->> (util/a-star start goal? cost neighbors heuristic)
           (partition 2 1)
           (map (fn [[x y]] ((cost x) y)))
           (reduce +))))

(assert (= 102 (part-1 test-data)))
(defonce ans1 (part-1 data))


(defn -main [& _]
  (println "Answer1: " ans1))
  ; (println "Answer2: " ans2))
