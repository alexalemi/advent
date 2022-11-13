(ns p20
  (:require [clojure.string :as str]
            [util :as util]))

;; # 2016 - Day 20
;; For this challenge, it looks like we are given a bit list of ranges
;; and we are interested in the lowest value integer that isn't contained
;; in any of the blocks.

(def MAX 4294967295)

(defn process-line [line]
  (let [[_ low high] (re-matches #"(\d+)-(\d+)" line)]
    [(read-string low) (read-string high)]))

(def ranges (mapv process-line (str/split-lines (slurp "../input/20.txt"))))
(def test-ranges [[5 8] [0 2] [4 7]])

;; My plan here is that I'm going to try to write a stepper that will take a
;; given index, and then process through the whole list to try to find the next
;; position it should sit at, by looking for the maximum top of all of the
;; overlapping regions it will also try to prune out the ranges that no longer
;; matter to speed up the processing.

(def test-data {:loc 0 :ranges test-ranges})

(defn one-pass [state]
  (reduce
   (fn [{loc :loc :as state} [low high]]
     (if (<= low loc)
       (assoc state :loc (max loc (inc high)))
       (update state :ranges conj [low high])))
   (assoc state :ranges [])
   (:ranges state)))


(defn lowest-allowed [ranges]
  (:loc (util/fixed-point (iterate one-pass {:loc 0 :ranges ranges}))))

(defonce ans1 (lowest-allowed ranges))
(println "Answer1:" ans1)


(defn crawl [state]
  (let [lowest (util/fixed-point (iterate one-pass state))
        next-lowest (reduce #(min %1 (first %2)) (:max state) (:ranges lowest))]
    (-> lowest
      (update :allowed + (- next-lowest (:loc lowest)))
      (assoc :loc next-lowest))))

(defn all-allowed [ranges max]
  (let [state {:loc 0 :ranges ranges :allowed 0 :max (inc max)}]
    (:allowed (util/fixed-point (iterate crawl state)))))


(def ans2 (all-allowed ranges MAX))
(println "Answer2: " ans2)
