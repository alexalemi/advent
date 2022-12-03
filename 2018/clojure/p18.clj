;; #🎄 Advent of Code 2018 - Day 18
;; # Settlers of The North Pole
;;
;; For this puzzle we have a Cellular Automata to implement.
;; There are three states, open ground, trees and lumberyards.

(ns p18
  (:require [clojure.string :as str]
            [clojure.set :as set]
            [nextjournal.clerk :as clerk]))

(def data-string (slurp "../input/18.txt"))
(def test-string ".#.#...|#.
.....#|##|
.|..|...#.
..|#.....#
#.#|||#|#|
...#.||...
.|....|...
||...#|.#|
|.||||..|.
...#.|..|.")

(defn neighbors
  "All eight neighbors"
  [[x y]]
  [[(dec x) (dec y)]
   [x (dec y)]
   [(inc x) (dec y)]

   [(dec x) y]
   [(inc x) y]

   [(dec x) (inc y)]
   [x (inc y)]
   [(inc x) (inc y)]])


(defn process [s]
 (reduce
   (fn [m [loc c]] (assoc m loc ({\. :open \| :tree \# :yard} c)))
   {}
   (for [[y row] (map vector (range) (str/split-lines s))
         [x char] (map vector (range) row)]
     [[x y] char])))

(def data (process data-string))
(def test-data (process test-string))

(defn bounds [state]
  (let [all-sites (keys state)]
    [(transduce (map first) max 0 all-sites)
     (transduce (map second) max 0 all-sites)]))
(def symbols {:tree "🌲" :yard "🪵" :open "🟩"})

(defn score [state]
  (let [{:keys [tree yard]} (frequencies (vals state))]
    (* tree yard)))

(defn render [state]
  (clerk/html
   (let [[X Y] (bounds state)]
     [:div
       (for [y (range (inc Y))]
         [:div {:style {:white-space "nowrap" :font-size "5pt" :line-height "7pt"}}
          (apply str (for [x (range (inc X))]
                        (symbols (state [x y]))))])
       [:p (str "⬆ Score: " (score state))]])))


(defn rule
  "After closing over the state, produce a rule for a reduce-kv"
  [state]
  (fn [m k v]
    (let [neighs (map state (neighbors k))]
     (case v
       :open (if (<= 3 (count (filter #(= % :tree) neighs)))
                 (assoc m k :tree)
                 (assoc m k :open))
       :tree (if (<= 3 (count (filter #(= % :yard) neighs)))
                 (assoc m k :yard)
                 (assoc m k :tree))
       :yard (if (and (some #(= % :yard) neighs)
                      (some #(= % :tree) neighs))
                 (assoc m k :yard)
                 (assoc m k :open))))))

(defn step
  "Do a full round"
  [state]
  (reduce-kv (rule state) {} state))

;; Having written all the rules, let's validate the test case

(def test-movie (take 11 (iterate step test-data)))

(map render test-movie)

;; Everything checks out

;; ## Part 1

(render data)

(def step-10 (nth (iterate step data) 10))
(render step-10)
(def ans1 (score step-10))

;; ## Part 2
;;
;; For part 2 we are supposed to run this thing basically forever,
;; though I have a sneaking suspicion that it will approach a limit cycle,
;; so let's see if we can idenify it.

(def target-time 1000000000)

(def first-repeat
  (time (reduce
         (fn [[seen t] x] (if (seen x)
                            (reduced [t (seen x) x])
                            [(assoc seen x t) (inc t)]))
         [{} 0]
         (iterate step data))))

(def final-state
  (let [[front back state] first-repeat
        remainder (rem (- target-time back) (- front back))]
    (nth (iterate step state) remainder)))

(render final-state)

;; It's clear the puzzle gets stuck in spirals in the end.

(def ans2 (score final-state))

(defn -main [& _]
  (println "Answer1: " ans1)
  (println "Answer2: " ans2))
