(ns p18
  (:require [clojure.string :as str]))

(def data-string (slurp "../input/18.txt"))
(def test-string ".#.#.#
...##.
#....#
..#...
#.#..#
####..")

(defn enumerate [col] (map list (range) col))


(defn process-board [board]
  {:time 0
   :size (count (str/split-lines board))
   :board (into #{} (remove nil? (for [[r line] (enumerate (str/split-lines board))
                                       [c chr] (enumerate line)]
                                   (when (= chr \#) [c r]))))})

(def board-data (process-board data-string))
(def test-board-data (process-board test-string))

(defn neighbors [[x y]]
  (list [(dec x) (inc y)]
        [x (inc y)]
        [(inc x) (inc y)]

        [(dec x) y]
        [(inc x) y]

        [(dec x) (dec y)]
        [x (dec y)]
        [(inc x) (dec y)]))

(defn num-neighbors [board loc] (count (remove nil? (map board (neighbors loc)))))

(defn inside? [n [x y]]
  (and (>= x 0)
       (>= y 0)
       (< x n)
       (< y n)))


(defn candidates [board-data]
  (let [{:keys [board size]} board-data]
   (filter (partial inside? size) (reduce (fn [s x] (into s x)) #{} (map neighbors board)))))


(defn alive? [board loc]
  (let [n (num-neighbors board loc)]
    (if (board loc)
        (or (= 2 n) (= 3 n))
        (= 3 n))))

(defn step [board-data]
  (-> board-data
      (assoc :board (into #{} (filter (partial alive? (:board board-data)) (candidates board-data))))
      (update :time inc)))


(defonce ans1 (count (:board (nth (iterate step board-data) 100))))
(println "Answer1:" ans1)

(comment
  (count (:board (nth (iterate step test-board-data) 4))))

(defn set-corners [board-data]
  (let [{:keys [board size]} board-data
        x (dec size)]
    (assoc board-data :board (into board [[0 0] [0 x] [x 0] [x x]]))))

(defonce ans2 (count (:board (nth (iterate (comp set-corners step) (set-corners board-data)) 100))))
(println "Answer2:" ans2)
