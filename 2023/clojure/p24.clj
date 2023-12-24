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
;
; $$ p_1 + t_1 v_1 == p_0 + t_1 v_0 $$
;
; so if we take any three of them, we have a system of equations we can solve,
; threw that into mathematica originally, but going forward, let's try a
; different approach.
; ## Alternating Minimization
;
; The issue is that we need to determine both our balls initial position and velocity,
; as well as the times for each of the collisions. The problem is this is a nonlinear
; system of equations.  So let's split the problem into pieces.
;
; If we knew the initial position and velocity of our ball, we could solve
; for the time for the nearest approach to each of the other balls in closed form.
; Similarly, if we knew the times at which we wanted to hit the other balls, we could
; work out what the best initial position and velocity would be, so let's do that
; and just alternately project onto the two different conditions.
;
; So, first, let's work out the best position and velocity for the ball if we happen to know each of the times,
; the best position and velocity is the one that minimizes the distances to all of the balls.
; (The notation here is a bit sloppy, the $p$s, $v$s and $t$ are vectors, $t_i$ is the $i$-th component of $t$, the vector of
; collision times.
;
; $$\begin{align}
;   p^*, v^* &= \textsf{project}_{pv}(p, v, t) = \min_{p,v} \phi(p,v,t) \\
;   &= \min \sum_i ((p_i + t_i v_i) - (p + t_i v))^2 \\
;   &= \min \sum_i (p_i + t_i v_i)^2 - 2 (p_i + t_i v_i) \cdot (p + t_i v) + (p + t_i v)^2
; \end{align}$$
;
;The minimum is found by setting the derivative equal to zero.
;
; $$ 0 = \partial_p \phi = \sum_i -(p_i + t_i v_i) + (p + t_i v) $$
;
;which implies
;
; $$ p^* = \frac 1 N \sum_i p_i + t_i (v_i - v) $$
;
; or in words, the best starting position is the average of the place we would have been at the start if we know the collision times and velocities.
; repeating the same calculation for the best starting velocity we get:
;
; $$ 0 = \partial_v \phi = \sum_i -(p_i + t_i v_i)t_i + (p + t_i v)t_i $$
;
; for the optimal value of
;
; $$ v^* = \frac{\sum t_i ((x_i - x) + t_i v_i)}{\sum_i t_i^2} $$
;
; Finally, if we know the best starting position and velocity, we can work out the best intersection times, this time with:
;
; $$ 0 = \partial_{t_i} \phi = (p_i + t_i v_i) \cdot v_i - 2(p_i + t_i v_i)\cdot v - 2 v_i\cdot (p + t_i v) + (p + t_i v)\cdot v $$
;
; which gives
;
; $$ t_i = \frac{ p\cdot v_i + v\cdot p_i - p\cdot v }{ v^2 - 2 v\cdot v_i} $$
;

(defn a+ [& vs] (apply map + vs))
(defn a- [& vs] (apply map - vs))
(defn a* [& vs] (apply map * vs))
(defn sum [vs] (reduce + vs))
(defn adot [v1 v2] (sum (a* v1 v2)))
(defn asqr [v] (adot v v))
(defn a*-scalar [a & vs] (apply map (fn [x] (* a x)) vs))


(defn project [data state]
  (let [n (count data)
        ps (map :p data)
        vs (map :v data)
        {:keys [p v ts]} state
        newp (a*-scalar (/ 1.0 n)
                        (apply a+
                               (map (fn [pi vi ti] (a+ p (a+ pi (a*-scalar ti (a- vi v)))))
                                    ps vs ts)))
        newv (a*-scalar (/ 1.0 (asqr ts))
                        (apply a+
                               (map (fn [pi vi ti] (a+ v (a*-scalar ti (a+ (a- pi p) (a*-scalar ti vi)))))
                                    ps vs ts)))
        newts (map (fn [pi vi _ti]
                     (/ (+ (adot vi p) (adot v pi) (- (adot v p)) (- (adot vi pi)))
                        (* 1.0 (asqr (a- vi v))))) ps vs ts)]
    (assoc state :p newp :v newv :ts newts)))

(let [data test-data
      state {:p [0 0 0]
             :v [1 1 1]
             :ts (take (count test-data) (repeat 1))}
      project* (partial project data)]
  (map (comp sum :p) (take 15 (iterate project* state))))
  ;(take 5 (iterate project* state)))


(def ans2 1007148211789625)
(assert (= ans2 1007148211789625))

(defn -main []
  (println "Answer 1:" ans1)
  (println "Answer 2:" ans2))
