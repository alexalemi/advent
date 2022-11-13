(ns p19
  (:require [util :as util]))

;; # 2016 - Day 19
;; In this puzzle there are a circle of elves which every round will steal the presents of the person
;; to their left.

(def data-string (read-string (slurp "../input/19.txt")))

(defn round [elves]
  (let [active (peek elves)
        left (pop (pop elves))]
    (conj left active)))


(defn eliminate-all [n]
  (first (first (drop-while #(> (count %) 1) (iterate round (util/queue (range 1 (inc n))))))))


(eliminate-all 5)

(defonce ans1 (eliminate-all data-string))
(println "Answer1:" ans1)

;; # Part 2
;; For this part we have to eliminate the elves that are on the other half of the circle.
;; So, for this I think I'll keep track of two queues.


(defn eliminate-all-2 [n]
  (let [half (inc (quot n 2))]
    (loop [front-elves (util/queue (range 1 half))
           back-elves (util/queue (range half (inc n)))]
       (if (seq front-elves)
        (let [rest (pop back-elves)
              front (pop front-elves)
              back (conj rest (peek front-elves))]
          (if (> (count back) (inc (count front)))
            (recur (conj front (peek back)) (pop back))
            (recur front back)))
        (first back-elves)))))


(defonce ans2 (eliminate-all-2 data-string))
(println "Answer2:" ans2)
