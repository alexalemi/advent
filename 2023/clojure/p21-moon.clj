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
           seen-odd #{}
           seen-even #{(data :start)}
           t 0]
     (if (= n t)
       (if (even? t) (count seen-even) (count seen-odd))
       (let [new-frontier (into #{} (comp (mapcat neighbors) (remove wall?*) (remove seen-odd) (remove seen-even)) frontier)]
         (recur
          (into #{} new-frontier)
          (if (even? t)
            (into seen-odd new-frontier)
            seen-odd)
          (if (odd? t)
            (into seen-even new-frontier)
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

(time (defonce ans2 (expand-infinite-frontier data 26501365)))

(comment
  (into #{} [[1 2] [-3 4] [2 3]])
  (count (into (i/int-set) (range 1e6))))

(comment
   (let [foo (transient #{})]
     (reduce conj! foo #{:x :b})))

(comment
    (let [data test-data
          {:keys [walls start extent]} data]
      (mod -1 11)))

(defn -main []
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))


(println "Answer1:" ans1)
(println "Answer2:" ans2)
