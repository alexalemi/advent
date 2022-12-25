;; # 🎄 Advent of Code 2022 - Day 24 - Blizzard Basin
(ns p24
  (:require [clojure.string :as str]
            [clojure.test :as test]
            [util :as util]))

(def data-string (slurp "../input/24.txt"))
(def test-string "#.######
#>>.<^<#
#.<..<<#
#>v.><>#
#<^v^^>#
######.#")

(defn indexed [xs] (map-indexed vector xs))

(defn trim [xs] (butlast (rest xs)))

(defn process [s]
  (let [lines (str/split-lines s)]
    (-> (reduce
         (fn [m [[x y] c]]
           (case c
             \< (update m :left conj [x y])
             \> (update m :right conj [x y])
             \^ (update m :up conj [x y])
             \v (update m :down conj [x y])
             \# (update m :wall conj [x y])
             \. m))
         {:left nil :right nil :up nil :down nil}
         (for [[y line] (indexed (trim lines))
               [x c] (indexed (trim line))]
           [[x y] c]))
        (assoc :start [(ffirst (filter (fn [[x c]] (= c \.)) (indexed (trim (first lines))))) -1])
        (assoc :loc [(ffirst (filter (fn [[x c]] (= c \.)) (indexed (trim (first lines))))) -1])
        (assoc :goal [(ffirst (filter (fn [[x c]] (= c \.)) (indexed (trim (last lines))))) (dec (dec (count lines)))])
        (assoc :bounds [(- (count (first lines)) 2) (- (count lines) 2)]))))

(defn move-left [[x y]] [(dec x) y])
(defn move-right [[x y]] [(inc x) y])
(defn move-down [[x y]] [x (inc y)])
(defn move-up [[x y]] [x (dec y)])

(def raw-neighbors (juxt identity move-left move-right move-down move-up))

(def data (process data-string))
(def test-data (process test-string))

(defn normalize [[xhi yhi] [x y]]
  [(mod x xhi) (mod y yhi)])

(defn round [state]
  (let [{:keys [bounds up down left right]} state
        project (partial normalize bounds)]
    (-> state
        (assoc :up (map (comp project move-up) up))
        (assoc :down (map (comp project move-down) down))
        (assoc :left (map (comp project move-left) left))
        (assoc :right (map (comp project move-right) right)))))

(defn in-bounds? [[xhi yhi] [x y]]
  (and (< -1 x xhi) (< -1 y yhi)))

(defn manhattan [[x1 y1] [x2 y2]]
  (+ (abs (- x2 x1)) (abs (- y2 y1))))

(defn goal? [state]
  (= (:loc state) (:goal state)))

(def cost (constantly (constantly 1)))

(defn heuristic [state]
  (manhattan (:loc state) (:goal state)))

(defn make-at-round [state]
  (let [cycle (apply util/lcm (:bounds state))
        at-round (fn [mem-at-round time]
                   (let [at-round (fn [x] (mem-at-round mem-at-round x))
                         time (mod time cycle)]
                     (if (= time 0) state
                         (round (at-round (dec time))))))
        mem-at-round (memoize at-round)]
    (partial mem-at-round mem-at-round)))

(def at-round (make-at-round test-data))

(defn state-neighbors [next-state loc]
  (let [{:keys [up down left right bounds]} next-state
        occupied? (reduce into #{} [up down left right])
        in-bounds? (some-fn #{(:start next-state) (:goal next-state)} (partial in-bounds? bounds))]
    (into [] (comp (filter in-bounds?) (remove occupied?)) (raw-neighbors loc))))

;; ## Part 1

(defn part-1
  ([state] (part-1 state (:start state) (:goal state) 0))
  ([state start goal start-time]
   (let [at-round (make-at-round state)
         start [start-time start]
         neighbors (fn [[time loc]] (for [neigh (state-neighbors (at-round (inc time)) loc)]
                                      [(inc time) neigh]))
         goal? (fn [[_ loc]] (= goal loc))
         cost (constantly (constantly 1))
         heuristic (fn [[_ loc]] (manhattan loc (:goal state)))]
     (dec (count (util/a-star start goal? cost neighbors heuristic))))))

(test/deftest test-part-1
  (test/is (= 18 (part-1 test-data))))

(def ans1 (time (part-1 data)))

;; ## Part 2

(defn part-2 [state]
  (let [first-trip (part-1 state (:start state) (:goal state) 0)
        second-trip (part-1 state (:goal state) (:start state) first-trip)
        third-trip (part-1 state (:start state) (:goal state) (+ first-trip second-trip))]
    (+ first-trip second-trip third-trip)))

(test/deftest test-part-2
  (test/is (= 54 (part-2 test-data))))

(def ans2 (time (part-2 data)))

;; ## Main

(defn -test [& _]
  (test/run-tests 'p24))

(defn -main [&_]
  (println "Answer1: " ans1)
  (println "Answer2: " ans2))
