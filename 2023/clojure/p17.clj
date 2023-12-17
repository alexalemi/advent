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

(def start {:loc [0 0] :life 3 :dir nil})
(defn goal? [board {[x y] :loc}]
    (let [[Y X] (extent board)]
        (and (= y Y )(= x X))))
(defn cost [board _]
    (fn [{loc :loc}] (board loc)))

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

(defn inside? [board {[y x] :loc}]
    (let [[Y X] (extent board)]
        (and (<= 0 x X) (<= 0 y Y))))

(defn raw-neighbors [{[x y] :as loc :loc dir :dir life :life}]
  (case dir
    :up (maybe-add
         (pos? life)
         [{:loc (right loc) :dir :right :life LIVES}
          {:loc (left loc) :dir :left :life LIVES}]
         {:loc (up loc) :dir :up :life (dec life)})
    :down (maybe-add
           (pos? life)
           [{:loc (right loc) :dir :right :life LIVES}
            {:loc (left loc) :dir :left :life LIVES}]
           {:loc (down loc) :dir :down :life (dec life)})
    :left (maybe-add
           (pos? life)
           [{:loc (up loc) :dir :up :life LIVES}
            {:loc (down loc) :dir :down :life LIVES}]
           {:loc (left loc) :dir :left :life (dec life)})
    :right (maybe-add
            (pos? life)
            [{:loc (up loc) :dir :up :life LIVES}
             {:loc (down loc) :dir :down :life LIVES}]
            {:loc (right loc) :dir :right :life (dec life)})
    [{:loc (up loc) :dir :up :life LIVES}
     {:loc (down loc) :dir :down :life LIVES}
     {:loc (left loc) :dir :left :life LIVES}
     {:loc (right loc) :dir :right :life LIVES}]))

(defn heuristic [board {[y x] :loc}]
  (let [[Y X] (extent board)]
      (+ (- Y y) (- X x))))

(defn neighbors [data x]
  (filter (partial inside? data) (raw-neighbors x)))

(defn part-1 [data]
    (let [start start
          goal? (partial goal? data)
          cost (partial cost data)
          neighbors (partial neighbors data)
          heuristic (partial heuristic data)]
      (transduce
       (comp
        (map :loc)
        (map data))
       +
       (rest (util/a-star start goal? cost neighbors heuristic)))))

(assert (= 102 (part-1 test-data)))
(defonce ans1 (part-1 data))



(defn -main [& _]
  (println "Answer1: " ans1))
  ; (println "Answer2: " ans2))
