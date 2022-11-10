(ns p13
  (:require [util :as util]
            [nextjournal.clerk :as clerk]
            [clojure.set :as set]))

;; # 2016 - Day 13
;; It looks like for this challenge we are to imagine an infinite grid of points with a
;; function determining which cells are active.

(defn on-bits
  "Compute how many bits in a number are on."
  [x]
  (if (zero? x) 0
    (+ (bit-and x 1) (on-bits (bit-shift-right x 1)))))


(defn open? [favorite-number [x y]]
  (even? (on-bits (+ favorite-number (* x x) (* 3 x) (* 2 x y) y (* y y)))))


(clerk/add-viewers!
 [{:pred boolean?
    :render-fn '#(v/html [:div.inline-block {:style {:width 16 :height 16}
                                             :class (if (not %) "bg-black" "bg-white border-solid border-2 border-black")}])}
  {:pred (every-pred list? (partial every? (some-fn boolean? vector?)))
   :render-fn '#(v/html (into [:div.flex.flex-col] (v/inspect-children %2) %1))}
  {:pred (every-pred vector? (complement map-entry?) (partial every? boolean?))
   :render-fn '#(v/html (into [:div.flex.inline-flex] (v/inspect-children %2) %1))}])


;; Let's try to render the grid from the example, with a magic number of 10.

(into '() (reverse (for [y (range 10)] (into [] (for [x (range 10)] (open? 10 [x y]))))))

(defn raw-neighbors [[x y]]
  [[(inc x) y]
   [(dec x) y]
   [x (inc y)]
   [x (dec y)]])


(defn non-negative? [x] (>= x 0))


(defn neighbors [open? [x y]]
  (into []
        (comp (filter open?) (filter (fn [[x y]] (and (non-negative? x) (non-negative? y)))))
        (raw-neighbors [x y])))

(neighbors (partial open? 10) [0 0])

(defn manhatten [[x0 y0] [x1 y1]]
  (+ (abs (- x1 x0)) (abs (- y1 y0))))


;; The example problem in the puzzle is asking us to go to [7 4] from our starting location of [1 1]

(defn shortest-path-length [favorite goal]
 (dec
  (count
   (util/a-star
    [1 1] ;; start
    goal ;; goal
    (constantly (constantly 1)) ;; cost
    (partial neighbors (partial open? favorite)) ;; neighbors
    (partial manhatten goal)))))

(shortest-path-length 10 [7 4])


(def favorite-number (read-string (slurp "../input/13.txt")))

(defonce ans1 (shortest-path-length favorite-number [31 39]))
(println "Answer1:" ans1)


;; ## Part 2
;; The second part asks us to give the number of locations we can reach in at most 50 steps.  For this I feel as though I should do a sort of
;; flood filling type enterprise.

;; We'll first simply our computation of neighbors.
(def neighs (partial neighbors (partial open? favorite-number)))

;; What we'll do here is we'll just keep track of the current frontier as well as all of the seen spots.
(def state {:step 0 :seen #{[1 1]} :frontier (into #{} (neighs [1 1]))})


;; having done that we can write a simple update function that dumps the current frontier into the seen
;; and creates a new frontier of all of the neighbors of the current frontier (minus the seen)
(defn step [state]
  (let [{:keys [step seen frontier]} state
        newseen (into seen frontier)
        cands (reduce into #{} (map neighs frontier))]
    (-> state
        (update :step inc)
        (assoc :seen newseen)
        (assoc :frontier (set/difference cands newseen)))))


;; Our answer is then
(defonce ans2 ((comp count :seen) (nth (iterate step state) 50)))
(println "Answer2: " ans2)
