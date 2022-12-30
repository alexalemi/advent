;; # 🎄 Advent of Code 2018 - Day 23 - Experimental Emergency Teleportation
(ns p23
  (:require [clojure.test :as test]))

(def data-string (slurp "../input/23.txt"))

(defn ->ints [s]
  (map parse-long (re-seq #"-?\d+" s)))

(defn parse [s]
  (into
   []
   (map (fn [[x y z r]] {:loc [x y z] :radius r}))
   (partition 4 (->ints s))))

(def test-string "pos=<0,0,0>, r=4
pos=<1,0,0>, r=1
pos=<4,0,0>, r=3
pos=<0,2,0>, r=1
pos=<0,5,0>, r=3
pos=<0,0,3>, r=1
pos=<1,1,1>, r=1
pos=<1,1,2>, r=1
pos=<1,3,1>, r=1")

(def test-data (parse test-string))
(def data (parse data-string))

(defn manhattan [loc1 loc2]
  (reduce + (map abs (map - loc1 loc2))))

;; ## Part 1

(defn part-1 [bots]
  (let [{loc :loc radius :radius} (apply max-key :radius bots)]
    (count (into []
                 (comp
                  (map (comp (partial manhattan loc) :loc))
                  (filter (fn [x] (<= x radius))))
                 bots))))

(test/deftest test-part-1
  (test/is (= 7 (part-1 test-data))))

(def ans1 (time (part-1 data)))

;; ## Part 2
;;
;; For part 2 we are supposed to find the point that is in range of the greatest number of nanobots.  If there are multiple ones, choose the one
;; closest to the origin `[0 0 0]`.
;;
;; I think for this problem, we should try to implement an octree design.
;; That takes a cube and splits it up into 8 smaller cubes.  For each cube,
;; we should score it by accessing whether it is in range of each of the bots.

;; We'll represent a cube by two opposite corners.

(defn point-in-cube
  "Say whether a point is inside a cube or not."
  [[[xlo ylo zlo] [xhi yhi zhi]] [x y z]]
  (and (<= xlo x xhi) (<= ylo y yhi) (<= zlo z zhi)))

(defn cube-corners
  "Give a list of all corners of a cube."
  [[[xlo ylo zlo] [xhi yhi zhi]]]
  [[xlo ylo zlo]
   [xlo ylo zhi]
   [xlo yhi zlo]
   [xhi ylo zlo]
   [xlo yhi zhi]
   [xhi ylo zhi]
   [xhi yhi zlo]
   [xhi yhi zhi]])

(defn clip [x xlo xhi]
  (cond
    (< x xlo) xlo
    (> x xhi) xhi
    :else x))

(defn cube-in-range
  "Compute whether the cube is in range of the sensor."
  [[[xlo ylo zlo] [xhi yhi zhi]] {[x y z] :loc radius :radius}]
  (let [xx (clip x xlo xhi)
        yy (clip y ylo yhi)
        zz (clip z zlo zhi)]
    (<= (manhattan [x y z] [xx yy zz]) radius)))

(defn third [xs] (nth xs 2))

(defn bounding-cube [sensors]
  [[(reduce min (map (comp first :loc) sensors))
    (reduce min (map (comp second :loc) sensors))
    (reduce min (map (comp third :loc) sensors))]
   [(reduce max (map (comp first :loc) sensors))
    (reduce max (map (comp second :loc) sensors))
    (reduce max (map (comp third :loc) sensors))]])

(def test-data-2 (parse "pos=<10,12,12>, r=2
pos=<12,14,12>, r=2
pos=<16,12,12>, r=4
pos=<14,14,14>, r=6
pos=<50,50,50>, r=200
pos=<10,10,10>, r=5"))

(defn midpoint [x y]
  (quot (+ x y) 2))

(defn split-cube
  "Split a cube into 8 smaller cubes."
  [[[xlo ylo zlo] [xhi yhi zhi]]]
  (let [xen (midpoint xlo xhi)
        yen (midpoint ylo yhi)
        zen (midpoint zlo zhi)]
    [[[xlo ylo zlo]
      [xen yen zen]]
     [[(inc xen) ylo zlo]
      [xhi yen zen]]
     [[xlo (inc yen) zlo]
      [xen yhi zen]]
     [[xlo ylo (inc zen)]
      [xen yen zhi]]
     [[xlo (inc yen) (inc zen)]
      [xen yhi zhi]]
     [[(inc xen) ylo (inc zen)]
      [xhi yen zhi]]
     [[(inc xen) (inc yen) zlo]
      [xhi yhi zen]]
     [[(inc xen) (inc yen) (inc zen)]
      [xhi yhi zhi]]]))

(defn score-cube [sensors cube]
  (count (filter (partial cube-in-range cube) sensors)))

(defn point-in-range
  "Compute whether the cube is in range of the sensor."
  [point {loc :loc radius :radius}]
  (<= (manhattan point loc) radius))

(defn score-point [sensors point]
  (count (filter (partial point-in-range point) sensors)))

(defn all-points
  [[[xlo ylo zlo] [xhi yhi zhi]]]
  (for [x (range xlo (inc xhi))
        y (range ylo (inc yhi))
        z (range zlo (inc zhi))]
    [x y z]))

(defn volume [[[xlo ylo zlo] [xhi yhi zhi]]]
  (*' (inc (- xhi xlo))
      (inc (- yhi ylo))
      (inc (- zhi zlo))))

(defn best-cubes [data]
  (loop [cubes [(bounding-cube data)]]
    (if (<= (transduce (map volume) + cubes) 1000) cubes
        (let [cubes (mapcat split-cube cubes)
              best-score (transduce (map (partial score-cube data)) max 0 cubes)]
          (recur (filter (fn [cube] (= best-score (score-cube data cube))) cubes))))))

(defn point-keyfn [point]
  (- (manhattan [0 0 0] point)))

(defn best-point [data]
  (let [cubes (best-cubes data)
        pts (mapcat all-points cubes)]
    (if (> (count pts) 1)
      (apply max-key (partial score-point data) (sort-by point-keyfn pts))
      (first pts))))

(defn part-2 [data]
  (manhattan [0 0 0] (best-point data)))

(test/deftest test-part-2
  (test/is (= 36 (part-2 test-data-2))))

(def ans2 (time (part-2 data)))

;; ## Main

(defn -test [& _]
  (test/run-tests 'p23))

(defn -main [& _]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
