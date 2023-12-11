;; # 🎄 Advent of Code 2023 - Day 11
(ns p11
  (:require [clojure.string :as str]))

(def data-string (slurp "../input/11.txt"))
(def test-string "...#......)
.......#..
#.........
..........
......#...
.#........
.........#
..........
.......#..
#...#.....")

(defn enumerate [coll]
  (map vector (range) coll))

(defn ->data [s]
  (into #{} 
        (for [[row line] (enumerate (str/split-lines s))
              [col c] (enumerate line)
              :when (= c \#)]
          [row col])))

(def test-data (->data test-string))
(def data (->data data-string))

(defn extent [xs]
  [(apply max (map first xs))
   (apply max (map second xs))])

(defn missing [data]
  (let [[Y X] (extent data)
        rows (into #{} (map first) data)
        cols (into #{} (map second) data)]
    {:rows (into #{} (filter (complement rows) (range Y)))
     :cols (into #{} (filter (complement cols) (range X)))}))
    
(defn manhattan-distance [[y1 x1] [y2 x2]]
  (+ (abs (- y1 y2))
     (abs (- x1 x2))))

(defn distance [missing factor a b]
  (let [[y1 x1] a
        [y2 x2] b
        {cols :cols rows :rows} missing
        range* (fn [a b] (range (min a b) (inc (max a b))))]
    (+ (manhattan-distance a b)
       (* (dec factor) (count (filter rows (range* y1 y2))))
       (* (dec factor) (count (filter cols (range* x1 x2)))))))

(let [data test-data])
(defn sum-of-distances [data factor]
 (let [empties (missing data)
       pts (into [] data)
       n (count pts)
       dist (partial distance empties factor)]
   (reduce +
     (for [i (range 1 n) 
           j (range  0 i)]
       (dist (pts i) (pts j))))))

(defn part-1 [data]
  (sum-of-distances data 2))

(assert (= (part-1 test-data) 374))

(def ans1 (part-1 data))

(assert (= ans1 9648398))

;; ## Part 2
;;
;; Now we need to figure out how many copies of each card we win

(defn part-2 [data]
  (sum-of-distances data 1000000))

(assert (= (sum-of-distances test-data 10) 1030))
(assert (= (sum-of-distances test-data 100) 8410))
(def ans2 (part-2 data))
(assert (= ans2 618800410814))


(defn -main []
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
