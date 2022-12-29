;; # 🎄 Advent of Code 2018 - Day 22 - Mode Maze
(ns p22
  (:require [clojure.test :as test]
            [util :as util]))

#_(def data-string (slurp "../input/22.txt"))

(def data {:depth 11991
           :target [6 797]})

(def test-data {:depth 510
                :target [10 10]})

(declare erosion-level)
(declare geologic-index)

(defn raw-geologic-index [{[target-x target-y] :target :as data} [x y]]
  (cond
    (and (= x 0) (= y 0)) 0
    (and (= x target-x) (= y target-y)) 0
    (= y 0) (* x 16807)
    (= x 0) (* y 48271)
    :else (* (erosion-level data [(dec x) y])
             (erosion-level data [x (dec y)]))))

(defn raw-erosion-level [{depth :depth :as data} loc]
  (mod (+ depth (geologic-index data loc)) 20183))

(def geologic-index (memoize raw-geologic-index))
(def erosion-level (memoize raw-erosion-level))

(defn type [data loc]
  (let [level (erosion-level data loc)]
    (case (mod level 3)
      0 :rocky
      1 :wet
      2 :narrow)))

;; ## Part 1

(test/deftest test-part-1
  (test/are [t loc] (= t (type test-data loc))
    :rocky [0 0]
    :wet [1 0]
    :rocky [0 1]
    :narrow [1 1]
    :rocky [10 10]))

(def type-scores
  {:rocky 0
   :wet 1
   :narrow 2})

(defn risk-level [data]
  (let [{[x1 y1] :target} data]
    (reduce +
            (for [x (range (inc x1))
                  y (range (inc y1))]
              (type-scores (type data [x y]))))))

(risk-level test-data)

(def ans1 (time (risk-level data)))

;; ## Part 2
;; For Part 2, we need to find the shortest path, I'll use my generalized a-star routine, but need to implement the logic.
;;
;; we need to implement `start goal cost neighbors heuristic`

(def ^:dynamic *data* data)

(def allowed-tools
  ;; Defines which tool combinations are allowed in each region.
  {:rocky #{#{:climbing-gear} #{:torch}}
   :wet #{#{:climbing-gear} #{}}
   :narrow #{#{:torch} #{}}})

(def init {:loc [0 0]
           :equipment #{:torch}})

(defn raw-neighbors [[x y]]
  [[(inc x) y]
   [(dec x) y]
   [x (inc y)]
   [x (dec y)]])

(defn valid-loc? [[x y]]
  (and (>= x 0) (>= y 0)))

(defn right-equipment? [equipment loc]
  (some? ((allowed-tools (type *data* loc)) equipment)))

(defn neighbors [{loc :loc equipment :equipment :as state}]
  (let [which (type *data* loc)]
    (concat
     (for [new-loc (filter valid-loc? (raw-neighbors loc))
           :when (right-equipment? equipment new-loc)]
       (assoc state :loc new-loc))
     (for [equip (disj (allowed-tools which) equipment)]
       (assoc state :equipment equip)))))

(defn cost [{loc0 :loc equip0 :equipment}]
  (fn [{loc1 :loc equip1 :equipment}]
    (if (= equip1 equip0) 1 7)))

(defn goal? [{loc :loc equipment :equipment}]
  (and (= loc (:target *data*))
       (= equipment #{:torch})))

(defn manhattan [[x1 y1] [x2 y2]]
  (+ (abs (- x2 x1)) (abs (- y2 y1))))

(defn heuristic [{loc :loc equipment :equipment}]
  (+ (manhattan loc (:target *data*))
     (if (= equipment #{:torch}) 0 7)))

(defn part-2 [data]
  (let [state init]
    (binding [*data* data]
      (let [path (util/a-star
                  init
                  goal?
                  cost
                  neighbors
                  heuristic)]
        (reduce + (map (fn [[a b]] ((cost a) b)) (partition 2 1 path)))))))

(test/deftest test-part-2
  (test/is (= 45 (part-2 test-data))))

(def ans2 (time (part-2 data)))

;; ## Main

(defn -test [& _]
  (test/run-tests 'p22))

(defn -main [& _]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
