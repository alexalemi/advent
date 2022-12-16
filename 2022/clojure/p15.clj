;; # 🎄 Advent of Code 2022 - Day 15 - Beacon Exclusion Zone
;; There are a number of sensors and beacons underground,
;; each sensor can see only the nearest beacon.
(ns p15
  (:require [clojure.string :as str]
            [clojure.set :as set]
            [clojure.test :as test]
            [clojure.math.combinatorics :as combo]))

(def data-string (slurp "../input/15.txt"))
(def test-string "Sensor at x=2, y=18: closest beacon is at x=-2, y=15
Sensor at x=9, y=16: closest beacon is at x=10, y=16
Sensor at x=13, y=2: closest beacon is at x=15, y=3
Sensor at x=12, y=14: closest beacon is at x=10, y=16
Sensor at x=10, y=20: closest beacon is at x=10, y=16
Sensor at x=14, y=17: closest beacon is at x=10, y=16
Sensor at x=8, y=7: closest beacon is at x=2, y=10
Sensor at x=2, y=0: closest beacon is at x=2, y=10
Sensor at x=0, y=11: closest beacon is at x=2, y=10
Sensor at x=20, y=14: closest beacon is at x=25, y=17
Sensor at x=17, y=20: closest beacon is at x=21, y=22
Sensor at x=16, y=7: closest beacon is at x=15, y=3
Sensor at x=14, y=3: closest beacon is at x=15, y=3
Sensor at x=20, y=1: closest beacon is at x=15, y=3")

;; And we need the manhattan distance
(defn manhattan [[x1 y1] [x2 y2]]
  (+ (abs (- x1 x2)) (abs (- y1 y2))))

(defn parse [s]
  (for [line (str/split-lines s)]
    (let [[_ sensor-x sensor-y beacon-x beacon-y] (re-matches #"Sensor at x=(-?\d+), y=(-?\d+): closest beacon is at x=(-?\d+), y=(-?\d+)" line)
          [sensor-x sensor-y beacon-x beacon-y] (map parse-long [sensor-x sensor-y beacon-x beacon-y])]
      {:sensor [sensor-x sensor-y] :beacon [beacon-x beacon-y] :d (manhattan [sensor-x sensor-y] [beacon-x beacon-y])})))

(def data (parse data-string))
(def test-data (parse test-string))

;; # Part 1
;; For part-1 we are given some `y` coordinate and are asked how many spots
;; are excluded as possible missing beacon locations.

;; We can naively figure out the set of all excluded positions.
;; The naive way to do it is to simply create sets of all of the ranges and merge.
(defn naive-excluded-beacon-locations [data y]
  (count (apply set/union
                (for [{[sx sy] :sensor d :d} data]
                  (let [dx (- d (abs (- sy y)))]
                    (set (range (- sx dx) (+ sx dx))))))))

;; The less naive way of doing it is to try to merge the ranges
;; themselves without instantiating the intermediate sets.
(defn excluded-beacon-locations [data y]
  (let [ranges (for [{[sx sy] :sensor d :d} data]
                 (let [dx (- d (abs (- sy y)))]
                   [(- sx dx) (+ sx dx)]))
        ranges (->> ranges
                    (filter (fn [[xlo xhi]] (> xhi xlo)))
                    (sort-by first))]
    (dec (first
          (reduce
           (fn [[covered prev] [left right]]
             [(+ covered (- (max right prev) (max (dec left) prev))) (max prev right)])
           [0 ##-Inf]
           ranges)))))

(test/deftest test-part-1
  (test/is (= 26 (excluded-beacon-locations test-data 10))))

(defonce ans1 (time (excluded-beacon-locations data 2000000)))

;; ## Part 2
;; For part 2 we need to identify the one spot that is not excluded within some
;; rectangular region.  The region is large enough that if we tried to consider
;; every spot it would take too long, so I'll start by considering only those spots
;; that are at the edge of each beacons location

(defn boundary [{:keys [sensor beacon]}]
  (let [[x y] sensor
        d (inc (manhattan sensor beacon))]
    (into #{}
          (concat
           (for [i (range d)]
             [(+ x i) (+ y (- d i))])
           (for [i (range d)]
             [(+ x (- d i)) (- y i)])
           (for [i (range d)]
             [(- x i) (- y (- d i))])
           (for [i (range d)]
             [(- x (- d i)) (+ y i)])))))

(defn within-search-space [cap]
  (fn [[x y]] (and (<= 0 x cap) (<= 0 y cap))))

(defn excluded [data]
  (fn [loc]
    (some (fn [{:keys [sensor d]}]
            (<= (manhattan loc sensor) d))
          data)))

(defn tuning-freq [[x y]]
  (+ (* x 4000000) y))

;; This leads to a relatively simple implementation, and it works, but
;; its slower than I would like.

(defn naive-distress-signal [data cap]
  (tuning-freq
   (first
    (sequence
     (comp
      (mapcat boundary)
      (filter (within-search-space cap))
      (remove (excluded data)))
     data))))

;; To attempt to speed things up,
;; we'll use a modified version of the fast solution to part-1
;; and then check every height.

(defn find-x [data y cap]
  (let [ranges (for [{[sx sy] :sensor d :d} data]
                 (let [dx (- d (abs (- sy y)))]
                   [(- sx dx) (+ sx dx)]))
        ranges (->> ranges
                    (filter (fn [[xlo xhi]] (> xhi xlo)))
                    (sort-by first))]
    (reduce
     (fn [cand [left right]]
       (if (<= left cand)
         (if (>= right cand)
           (if (< right cap)
             (inc right)
             (reduced nil))
           cand)
         (reduced cand)))
     0
     ranges)))

(defn distress-signal [data cap]
  (tuning-freq
   (first
    (keep-indexed
     (fn [i y] (if-let [x (find-x data y cap)] [x y] nil))
     (range (inc cap))))))

;; This is faster but still not ideal.

(test/deftest test-part-2
  (test/is (= 56000011 (distress-signal test-data 20))))

(defonce ans2 (time (distress-signal data 4000000)))

;; ## Main

(defn -test [& _]
  (test/run-tests 'p15))

(defn -main [& _]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
