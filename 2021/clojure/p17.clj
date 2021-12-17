(ns advent17
  (:require
   [clojure.test :as test]))

(def data-string (slurp "../input/17b.txt"))
(def test-string "target area: x=20..30, y=-10..-5")

(defn process [s]
  (let [[_ & nums] (re-matches #"target area: x=(-?\d+)..(-?\d+), y=(-?\d+)..(-?\d+)\n?" s)]
    (mapv read-string nums)))

(def data (process data-string))
(def test-data (process test-string))

(defn init
  ([] (init 0 0))
  ([vx vy] {:x 0 :y 0 :vx vx :vy vy}))

(defn sign [x]
  (cond
    (pos? x) 1
    (zero? x) 0
    :else -1))

(defn step [state]
  (let [{:keys [_ _ vx vy]} state]
    (-> state
        (update :x + vx)
        (update :y + vy)
        (update :vx - (sign vx))
        (update :vy - 1))))

(defn inside? [area state]
  (let [[x1 x2 y1 y2] area
        {:keys [x y]} state]
    (and (and (>= x x1) (<= x x2))
         (and (>= y y1) (<= y y2)))))

(defn beyond? [area state]
  (let [[_ x2 y1 _] area
        {:keys [x y]} state]
    (or (> x x2) (< y y1))))

(test/deftest test-inside
  (test/are [loc exp] (= exp (inside? [20 30 -10 -5] {:x (loc 0) :y (loc 1)}))
    [20 -10] true
    [21 -10] true
    [21 -9] true
    [21 -12] false
    [31 -4] false
    [31 -8] false
    [30 -5] true))

(def MAXT 1000)
(defn create-traj [area vx vy]
  (let [state (init vx vy)
        not-beyond? (complement (partial beyond? area))]
    (take MAXT (take-while not-beyond? (iterate step state)))))

(defn hits-area? [area traj]
  (some (partial inside? area) traj))

(defn max-height [traj]
  (reduce max (map :y traj)))

(defn part-1 [area n]
  (->> (for [vx (range n) vy (range n)] (create-traj area vx vy))
       (filter (partial hits-area? area))
       (map max-height)
       (reduce max)))

(test/deftest test-part-1
  (test/is (= (part-1 test-data 10) 45)))

(time (def ans1 (part-1 data 300)))
(println)
(println "Answer 1:" ans1)

(defn part-2 [area n]
  (->> (for [vx (range n) vy (range (- n) n)] (create-traj area vx vy))
       (filter (partial hits-area? area))
       (count)))

(test/deftest test-part-2
  (test/is (= (part-2 test-data 50) 112)))

(time (def ans2 (part-2 data 300)))
(println)
(println "Answer 2:" ans2)

(test/run-tests)
