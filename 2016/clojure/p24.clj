(ns p24
  (:require [clojure.string :as str]
            [util :as util]))

;; # 2016 - Day 24
;; For this one we have a big sort of travelling salesman maze type puzzle.

(def data-string (str/split-lines (slurp "../input/24.txt")))
(def test-string (str/split-lines "###########
#0.1.....2#
#.#######.#
#4.......3#
###########"))

(def DIGITS {\0 0 \1 1 \2 2 \3 3 \4 4 \5 5 \6 6 \7 7 \8 8 \9 9})

(defn process [puzz]
  (reduce
   (fn [m [k v]]
     (cond
       (= \0 v) (assoc m :loc k)
       (DIGITS v) (update m :targets conj k)
       (= \# v) (update m :walls conj k)
       :else m))
   {:loc nil :targets #{} :walls #{}}
   (for [[y line] (map-indexed vector puzz)
         [x c] (map-indexed vector line)]
     [[x y] c])))


(def data (process data-string))
(def test-data (process test-string))



(defn neighbors [state]
  (letfn [(valid? [state] (not ((:walls state) (:loc state))))
          (pop-target [state] (update state :targets disj (:loc state)))
          (neighbor-locs [[x y]] [[(inc x) y] [(dec x) y] [x (inc y)] [x (dec y)]])]
   (transduce
     (comp
       (map #(assoc state :loc %))
       (filter valid?)
       (map pop-target))
     conj
     (neighbor-locs (:loc state)))))


(defn manhattan [[x y] [x0 y0]]
  (+ (abs (- x x0)) (abs (- y y0))))

(defn goal? [state]
  (and (not= state :start)
       (empty? (:targets state))))

(defn heuristic [state]
  (if (goal? state) 0
      (reduce min (map (partial manhattan (:loc state)) (:targets state)))))

(defn shortest-path [state]
  (let [cost (constantly (constantly 1))]
    (util/a-star
     state
     goal?
     cost
     neighbors
     heuristic)))

(dec (count (shortest-path test-data)))

(defonce short-path (shortest-path data))

(def ans1 (dec (count short-path)))
(println "Answer1:" ans1)

;; ## Part 2

;; In order to handle this part, what we'll do is modify the neighbor function
;; so that if we were originally at the goal we'll add another target which is at the
;; original start location.

(def zero-loc (:loc data))

(defn add-home [state]
  (if (and
       (goal? state)
       (not= (:loc state) zero-loc))
    (update state :targets conj zero-loc)
    state))

(defn new-neighbors [state]
  (map add-home (neighbors state)))

(defonce short-path-2
    (util/a-star
     data
     goal?
     (constantly (constantly 1))
     new-neighbors
     heuristic))

(dec (count short-path-2))
