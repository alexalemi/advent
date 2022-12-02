;; # Day 2
(ns p02
  (:require [clojure.string :as str]))

(defn parse [s]
  (map keyword (str/split s #" ")))

(def data
  (->> (str/split-lines (slurp "../input/02.txt"))
       (map parse)))

(def lookup
  {:A :rock
   :B :paper
   :C :scissors
   :X :rock
   :Y :paper
   :Z :scissors})

(def beat
  {:rock :scissors
   :scissors :paper
   :paper :rock})

;; Each choice has a shape-score.
(def scores
  {:rock 1
   :paper 2
   :scissors 3})

(defn score
  "The score for a round is the sum of the score for the shape and the outcome."
  [[a b]]
  (+ (scores b)
     (cond
       ;; lose
       (= b (beat a)) 0
       ;; tie
       (= a b) 3
       ;; win
       :else 6)))

(def ans1
  (transduce
   (comp
    (map #(map lookup %))
    (map score))
   +
   data))

(defn complete [[a b]]
  (let [a (lookup a)]
   (case b
     :X [a (beat a)]
     :Y [a a]
     :Z [a (beat (beat a))])))

(def ans2
  (transduce
   (comp
    (map complete)
    (map score))
   +
   data))


(defn -main [& _]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
