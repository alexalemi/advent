;; # 🎄 Advent of Code 2023 - Day 21 - Step Counter
(ns p21
  (:require [clojure.string :as str]
            [clojure.set :as set]))

(def data-string (slurp "../input/21.txt"))
(def test-string "...........
.....###.#.
.###.##..#.
..#.#...#..
....#.#....
.##..S####.
.##..#...#.
.......##..
.##.#.####.
.##..##.##.
...........")

(defn enumerate [col]
  (map list (range) col))

(defn ->data [s]
  (let [cells (for [[row line] (enumerate (str/split-lines s))
                    [col c] (enumerate line)
                    :when (#{\# \S} c)]
                [[row col] c])]
    {:walls (into #{} (comp
                       (filter #(= (second %1) \#))
                       (map first))
                 cells)
     :start (ffirst (filter #(= (second %1) \S) cells))
     :extent [(count (str/split-lines s)) (count (first (str/split-lines s)))]}))



(defn up [[y x]] [(dec y) x])
(defn down [[y x]] [(inc y) x])
(defn left [[y x]] [y (dec x)])
(defn right [[y x]] [y (inc x)])

(defn neighbors [loc] [(up loc) (down loc) (left loc) (right loc)])

(def data (->data data-string))
(def test-data (->data test-string))

(defn expand-frontier
  ([walls frontier]
   (into #{} (comp
              (mapcat neighbors)
              (remove walls)) frontier))
  ([walls frontier n]
   (loop [frontier frontier
          n n]
    (if (zero? n) frontier
        (recur (into #{} (comp (mapcat neighbors) (remove walls)) frontier) (dec n))))))

(defn part-1 [{:keys [walls start]} n]
  (count (expand-frontier walls #{start} n)))

(assert (= (part-1 test-data 6) 16))
(defonce ans1 (part-1 data 64))
(assert (= ans1 3748))

;; # Part 2

(defn project [[Y X] [y x]]
  [(mod y Y) (mod x X)])

(defn repeated-wall? [{:keys [walls extent]} loc]
    (walls (project extent loc)))


#_(defn expand-infinite-frontier
    [data n]
    (let [wall?* (partial repeated-wall? data)]
      (loop [frontier #{(data :start)}
             seen-odd #{}
             seen-even #{}
             t 0]
       (if (= n t)
         (+ (count frontier) (if (even? t) (count seen-even) (count seen-odd)))
         (let [new-frontier (into #{} (comp (mapcat neighbors) (remove wall?*)) frontier)]
           (recur
            (into #{} (comp (remove seen-odd) (remove seen-even)) new-frontier)
            (if (odd? t)
              (into seen-odd frontier)
              seen-odd)
            (if (even? t)
              (into seen-even frontier)
              seen-even)
            (inc t)))))))

(defn into! [tset xs] (reduce conj! tset xs))

(defn expand-infinite-frontier
  [data n]
  (let [wall?* (partial repeated-wall? data)]
    (loop [frontier #{(data :start)}
           prev #{}
           seen-odd 0
           seen-even 0
           t 0]
     (if (= n t)
       (+ (count frontier) (if (even? t) seen-even seen-odd))
       (let [new-frontier (into #{} (comp (mapcat neighbors) (remove wall?*) (remove prev)) frontier)]
         (recur
          new-frontier
          frontier
          (if (even? t)
            (+ seen-odd (count prev))
            seen-odd)
          (if (odd? t)
            (+ seen-even (count prev))
            seen-even)
          (inc t)))))))

(comment
  (time (expand-infinite-frontier test-data 6))
  (time (expand-infinite-frontier test-data 10))
  (time (expand-infinite-frontier test-data 50))
  (time (expand-infinite-frontier test-data 100))
  (time (expand-infinite-frontier test-data 500))
  (time (expand-infinite-frontier test-data 1000))
  (time (expand-infinite-frontier test-data 5000))

  (println "old----")
  (println "new----"))


(comment
  (time (expand-infinite-frontier data 64)))

(println "Answer1:" ans1)
(time (def ans2 (expand-infinite-frontier data 26501365)))
(println "Answer2:" ans2)


(defn -main []
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))


