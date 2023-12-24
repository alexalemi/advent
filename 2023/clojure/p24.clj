;; # 🎄 Advent of Code 2023 - Day 24 - Never Tell me the Odds
(ns p24
  (:require [clojure.string :as str]
            [clojure.math.combinatorics :as combo]))

(def data-string (slurp "../input/24.txt"))
(def test-string "19, 13, 30 @ -2,  1, -2
18, 19, 22 @ -1, -1, -2
20, 25, 34 @ -2, -2, -4
12, 31, 28 @ -1, -2, -1
20, 19, 15 @  1, -5, -3")

(defn process-line [line]
  (let [[_ px py pz vx vy vz] (re-matches #"(-?\d+),\s+(-?\d+),\s+(-?\d+)\s+@\s+(-?\d+),\s+(-?\d+),\s+(-?\d+)" line)]
    {:p (mapv parse-long [px py pz])
     :v (mapv parse-long [vx vy vz])}))

(defn ->data [s]
  (map process-line (str/split-lines s)))

(def data (->data data-string))
(def test-data (->data test-string))

(defn move [ball t]
  (let [{[px py pz] :p [vx vy vz] :v} ball]
    [(+ px (* vx t))
     (+ py (* vy t))
     (+ pz (* vz t))]))

(defn inside-2d? [bounds pos]
  (let [[lo hi] bounds
        [x y] pos]
   (and
    (<= lo x hi)
    (<= lo y hi))))

(defn xy-collision? [bounds ball0 ball1]
  (let [[lo hi] bounds
        {[px0 py0] :p [vx0 vy0] :v} ball0
        {[px1 py1] :p [vx1 vy1] :v} ball1]
    (let [denom (- (* vx1 vy0) (* vx0 vy1))]
      (if (zero? denom)
        nil
        (let [t0 (/ (- (+ (* py1 vx1) (* px0 vy1)) (+ (* py0 vx1) (* px1 vy1))) denom)
              t1 (/ (- (+ (* py1 vx0) (* px0 vy0)) (+ (* py0 vx0) (* px1 vy0))) denom)]
          [(move ball0 t0) (move ball1 t1)]
          (and
           (>= t0 0)
           (>= t1 0)
           (inside-2d? bounds (move ball0 t0))
           (inside-2d? bounds (move ball1 t1))))))))


(defn part-1 [data bounds]
    (count (filter (fn [[x y]] (xy-collision? bounds x y)) (combo/combinations data 2))))

(assert (= 2 (part-1 test-data [7 27])))
(def ans1 (part-1 data [200000000000000 400000000000000]))

; # Part 2
; Now we need to find the magical input that will collide with everything.
; I solved this with mathematica, using only the first three lines of the input
; We are interested in finding 6 unknowns, the initial position and velocity,
; and each time we add a collision constraint, it introduces 1 more unknown, but
; gives us three equations (x, y, z):
;       p1 + t1 v1 == p0 + t1 v0
; so if we take any three of them, we have a system of equations we can solve,
; through that into mathematica and got:

(def ans2 1007148211789625)
(assert (= ans2 1007148211789625))

(defn -main []
  (println "Answer 1:" ans1)
  (println "Answer 2:" ans2))
