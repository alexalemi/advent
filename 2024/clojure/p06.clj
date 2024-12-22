;; # 🎄 Advent of Code - 2024 - Day 6

(ns p06
  (:require [clojure.string :as str]
            [clojure.test :as test]))

(def data-string (slurp "../input/06.txt"))
(def test-data-string "....#.....
.........#
..........
..#.......
.......#..
..........
.#..^.....
........#.
#.........
......#...")

(defn ->board [s]
  (into {}
        (for [[y line] (map-indexed vector (str/split-lines s))
              [x c] (map-indexed vector line)]
          [[x y] c])))

(defn process [s]
  (let [board (->board s)]
    {:walls (into #{}
                  (comp
                    (filter (fn [[_ v]] (= v \#)))
                    (map first))
                  board)
     :start (ffirst (filter (fn [[_ v]] (= v \^)) board))
     :locs (into #{} (map first) board)}))

(def data (process data-string)) 
(def test-data (process test-data-string))

(defn north [[x y]] [x (dec y)])
(defn south [[x y]] [x (inc y)])
(defn east [[x y]] [(inc x) y])
(defn west [[x y]] [(dec x) y])

(def dir-fn
  {:north north
   :south south
   :east east
   :west west})

(def rotate90
  {:north :east
   :east :south
   :south :west
   :west :north})

(defn new-state [data]
  {:loc (data :start) :dir :north :terminated false})

(defn step 
  "Implement a single step of simulation."
  [{walls :walls locs :locs} {:keys [loc dir terminated] :as state}]
  (if terminated
    ; If terminated, do nothing
    state
    (let [new-loc ((dir-fn dir) loc)]
      (cond
        ; if we hit a wall, rotate
        (walls new-loc) (update state :dir rotate90)
        ; if the location is valid, move
        (locs new-loc) (assoc state :loc new-loc)
        ; otherwise mark as terminated
        :else (assoc state :terminated true)))))

(defn simulate [data]
  (take-while 
    (complement :terminated)
    (iterate (partial step data) (new-state data))))

(defn part-1
  "Figure out how many locations are visited."
  [data]
  (transduce
    (comp
      (map :loc)
      (distinct)
      (map (constantly 1)))
    +
    0
    (simulate data)))

(def ans1 (part-1 data))

(test/deftest test-part-1
  (test/is (= 41 (part-1 test-data)))
  (test/is (= 5331 ans1)))

;; ## Part 2


(defn duplicates? [states]
  (= :loop (reduce (fn [acc x] 
                     (if (acc x) 
                       (reduced :loop) 
                       (conj acc x)))
                   #{}
                   states)))

(defn part-2
  "Find all of the locations that create a time loop."
  [data]
  (transduce
    (comp
      (map :loc)
      (distinct)
      (map (partial update-in data [:walls] conj))
      (map simulate)
      (filter duplicates?)
      (map (constantly 1)))
    +
    (simulate data)))

(defonce ans2 (part-2 data))

(test/deftest test-part-2
  (test/is (= 6 (part-2 test-data)))
  (test/is (= 1812 ans2)))


;; #Main

(comment
  (test/run-tests))

(defn -main [& _args]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))

