;; # 🎄 Advent of Code 2022 - Day 8 - Treetop Tree House
(ns p08
  (:require [clojure.string :as str]
            [clojure.test :as test]))

;; It looks like today we have a big field of trees and we have to
;; determine which of the trees are visible from the outside.
;; First order of business is parsing the input, which
;; I'll turn into a map of locations to heights.

(def data-string (slurp "../input/08.txt"))
(def test-string "30373
25512
65332
33549
35390")

(defn enumerate [coll]
  (map vector (range) coll))

(defn ->map [s]
  (into {}
        (for [[y line] (enumerate (str/split-lines s))
              [x c] (enumerate line)]
          [[x y] (parse-long (str c))])))

(def data (->map data-string))
(def test-data (->map test-string))

;; ## Part 1
;;
;; Having formulated the map, now my plan is to do four different
;; sweeps over this map, to figure out the rolling max of tree heights
;; from the top, left, right and bottom.

;; First we need the bounds of the map

(defn bounds [m]
  (reduce
   (fn [[X Y] [x y]] [(max X x) (max Y y)])
   [0 0]
   (keys m)))

;; The way we'll do this is that we'll try to build a map of 4 numbers
;; at each location, the four numbers being the maximum tree height in
;; any direction.  To build this map, we'll fashion a reduction that
;; will reduce over a sequence of all of the "probes" that start
;; at the edges.

;; For that we need to be able to signal directions to move the probes.

(defn move [direction [x y]]
  (case direction
    :up [x (dec y)]
    :down [x (inc y)]
    :left [(dec x) y]
    :right [(inc x) y]))

;; And our principle reduction will have to update our
;; `heights` map for all of the locations visited by the probe
;; that starts in the `start` location and moves in `direction`.

(defn add-constraints [m heights [direction start]]
  (let [step (partial move direction)]
    (loop [heights heights
           loc start
           height -1]
      (if-let [tree (m loc)]
        (recur
         (update heights loc conj height)
         (step loc)
         (max height tree))
        ; else
        heights))))

;; The max-height-map then is just a reduction over all of the probes for the map.

(defn max-height-map [m]
  (let [[X Y] (bounds m)
        propogator (partial add-constraints m)
        heights {}]
    (reduce propogator heights
            (concat
             (for [x (range (inc X))]
               [:down [x 0]])
             (for [y (range (inc Y))]
               [:right [0 y]])
             (for [x (range (inc X))]
               [:up [x Y]])
             (for [y (range (inc Y))]
               [:left [X y]])))))

;; Now we can see how many trees are visible by simply filterings ones that aren't larger than the smallest
;; in their max-height-map

(defn trees-visible-from-outside [m]
  (let [max-heights (max-height-map m)]
    (count
     (filter
      (fn [[loc height]]
        (some (fn [x] (< x height)) (max-heights loc)))
      m))))

(test/deftest test-part-1
  (test/is (= 21 (trees-visible-from-outside test-data))))

(def ans1 (trees-visible-from-outside data))

;; ## Part 2
;; For part 2 we now want to know how many trees are visible from each location.  This is sorta like the
;; opposite problem.  Similarly, the trees visible at each location is a list of four numbers,
;; the trees we can see in each of the directions.  Question is, can we build that up in
;; much the same way we did the previous `max-height-map`?
;;
;; Instead, I'm just going to do the straightforward thing of scoring all of the trees.

(defn trees-visible
  ([m start] (map (partial trees-visible m start) [:up :down :left :right]))
  ([m start direction]
   (let [height (m start)
         step (partial move direction)]
     (loop [loc (step start)
            trees 1]
       (if-let [h (m loc)]
         (if (< h height)
           (recur (step loc) (inc trees))
           trees)
         (dec trees))))))

(defn scenic-score [m loc] (reduce * (trees-visible m loc)))

(defn best-scenic-score [m]
  (transduce
   (map (partial scenic-score m))
   max
   0
   (keys m)))

(test/deftest test-part-2
  (test/is (= 8 (best-scenic-score test-data))))

(def ans2 (best-scenic-score data))

;; ## main

(defn -test [_]
  (test/run-tests))

(defn -main [_]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
