(ns p14
  (:require [util]
            [util :as util]
            [clojure.string :as str]))

;; # 2016 - Day 14
;; Looks like for today we are supposed to process md5 hashes again,
;; I put the md5 routine in util.

(def salt (str/trim (slurp "../input/14.txt")))
(def test-salt "abc")

(defn hash-with-salt [salt index]
  (util/md5 (str salt index)))

(def hash (partial hash-with-salt salt))
(def test-hash (partial hash-with-salt test-salt))

;; Now we have convenient wrappers for our hashing function:

(test-hash 18)
(test-hash 39)
(test-hash 816)

;; It looks like for a key to be a valid one-time-pass key, it needs to have an index that
;; hashes to a sequence with a triplet in it, *and* we need to then be able to find that
;; same chacter five times in a row within the next 1000 hashes.  Let's start by writing
;; a routine to measure whether any characters are repeated 3 or 5 times.

(defn find-triplet [h]
  (first (first (filter #(apply = %) (partition 3 1 h)))))

(defn find-quints [h]
  (first (first (filter #(apply = %) (partition 5 1 h)))))

(find-quints (test-hash 816))

;(def hashseq
;  (map (juxt identity test-hash) (range)))

;; We're gonna try to make a lazy-seq of all of the valid one-time-pads

(defn has-matching [i c quints]
  (->> quints
       (drop-while #(< (first %) (inc i)))
       (take-while #(<= (first %) (+ i 1000)))
       (some #(= (second %) c))))

(defn lookup [test-hash n]
 (let [valid-keys (let [hashseq (map (juxt identity test-hash) (range))
                        triplets (filter second (map (fn [[i h]] [i (find-triplet h)]) hashseq))
                        quints (filter second (map (fn [[i h]] [i (find-quints h)]) hashseq))]
                    (filter (fn [[i c]] (has-matching i c quints)) triplets))]
   (first (nth valid-keys (dec n)))))


(time (lookup test-hash 64))


(defonce ans1 (lookup hash 64))

;; ## Part 2
;; looks like for this part we are supposed to implement key-stretching so that
;; every time we compute a hash we have to repeat the process 2016 times.

(defn stretch [hf]
    #(nth (iterate util/md5 (hf %)) 2016))


(def stretched-test-hash (stretch test-hash))
(def stretched-hash (stretch hash))


(lookup stretched-test-hash 64)

(defonce ans2 (lookup stretched-hash 64))
