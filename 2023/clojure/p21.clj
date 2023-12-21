;; # 🎄 Advent of Code 2023 - Day 21 - Step Counter
(ns p21
  (:require [clojure.string :as str]))

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
  (time (= 16 (expand-infinite-frontier test-data 6)))
  (time (= 50 (expand-infinite-frontier test-data 10)))
  (time (= 1594 (expand-infinite-frontier test-data 50)))
  (time (= 6536 (expand-infinite-frontier test-data 100)))
  (time (= 167004 (expand-infinite-frontier test-data 500)))
  (time (= 668697 (expand-infinite-frontier test-data 1000)))
  (time (= 16733044 (expand-infinite-frontier test-data 5000))))

;; If we go and look at the generated maps, its pretty

(comment
  (quot 131 2))

(defn part-2 [data steps]
  ;; The answer is a quadratic, so we need to compute the coefficients of the different pieces.
  ;; )
  (let [{:keys [walls extent start]} data
        [Y X] extent
        [y0 x0] start
        offset y0
        size Y
        iters (quot (- steps offset) size)]
    (assert (= X Y) "Grid is not square!")
    (assert (= y0 x0) "Doesn't start in center!")
    (assert (= (quot Y 2) y0)), "Doesn't start in center!"
    (assert (= steps (+ offset (* iters size))) "Input isn't a nice just filled time!")

    ;; f n = x (a x + b) + c
    ;; f0 = c
    ;; f2 = 4 a + 2 b + c
    ;; f2 - f0 = 4a + 2b
    ;; f4 = 16a + 4b + c
    ;; f4 - 2 f2 + f0 = 8a
    ;;  => a = (f4 - 2 f2 + f0)/8
    ;; 
    ;;  => b = (f2 - 4a - c)/2

    (letfn [(step [n] (+ offset (* n size)))]
        (let [f0 (expand-infinite-frontier data (step 0))
              f2 (expand-infinite-frontier data (step 2))
              f4 (expand-infinite-frontier data (step 4))
              c f0
              a (quot (+ f4 (* -2 f2) f0) 8)
              b (quot (+ f2 (* -4 a) (- c)) 2)]
          (+ (* (+ (* a iters) b) iters) c)))))



(defonce ans2 (part-2 data 26501365))
(assert (= ans2 616951804315987))


(defn -main []
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
