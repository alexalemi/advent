;; # Advent of Code 2016 Day 9
(ns p09
  (:require [clojure.string :as str]))

;; Overall for this problem we are trying to build a decompressor
(def data-string (slurp "../input/09.txt"))

(def test-string "ADVENT
A(1x5)BC
(3x3)XYZ
A(2x2)BCD(2x2)EFG
(6x1)(1x3)A
X(8x2)(3x3)ABCY")

(defn to-num [s] (read-string (apply str s)))

(def digit? (into #{} "0123456789"))

(defn read-rule
  "Read and parse a ({size}x{copies}) and rest of string."
  [s]
  (let [s (rest s) ;eat the (
        [size s] (split-with digit? s)
        size (to-num size)
        s (rest s) ;eat the x
        [copies s] (split-with digit? s)
        copies (to-num copies)
        s (rest s)] ; eat the )
    [s {:size size :copies copies :length (count (str "(" size "x" copies ")"))}]))

(read-rule "(10x3)asdfb")

(defn decompress
  ([s] (decompress s []))
  ([s out]
   (let [c (first s)]
     (cond
       (nil? c) (apply str out)
       ; we have just seen a pattern
       (= c \()
       (let [[s {size :size copies :copies}] (read-rule s)
             [buffer s] (split-at size s)]
          (recur s (into [] (concat out (flatten (repeat copies buffer))))))
       :else
       (recur (rest s) (conj out c))))))

;; ## Part 1
(defonce ans1 (reduce + (map (comp count decompress) (str/split-lines data-string))))
(println "Answer1:" ans1)

(defn decompress-all [data]
  (str/join "\n" (map decompress (str/split-lines data))))


(defn decompress-completely [data]
  (->> data
      (iterate decompress-all)
      (partition 2 1)
      (drop-while (fn [[a b]] (not= a b)))
      ffirst))


;; ## Part 2
;; For part 2 it lookas as though we can't actually decompress the input, we don't have the memory for that.
;; so instead we'll have to try to be more clever.  My current thought is that what we'll do is
;; we'll try to just increment the size as we go along.

(defn dec-size
  ([rule] (update rule :size dec))
  ([n rule] (update rule :size - n)))


(defn advance-rules
  "Given all of the rules, advance them and update their count"
  ([rules] ; this version is called when there is only one step)
   [(->> rules
      (map dec-size)
      (filter (comp pos? :size)))
    (reduce * (map :copies rules))])
  ([rules n] ;; This version is called when there is a new rule))
   (->> rules
        (map (partial dec-size n))
        (filter (comp pos? :size)))))


(defn decompressed-size
  ([s] (decompressed-size s 0 nil))
  ([s tot rules]
   (let [c (first s)]
     (cond
       ; If empty we are done, return the tot
       (nil? c) tot
       ; we have just seen a pattern
       (= c \()
       (let [[s rule] (read-rule s)
             {length :length} rule
             decayed (advance-rules rules length)]
          (recur s tot (conj decayed rule)))
       :else ; a single character
       (let [[decayed increment] (advance-rules rules)]
         (recur (rest s) (+ tot increment) decayed))))))

(def ans2 (decompressed-size (str/trim data-string)))
(println "Answer2:" ans2)


(map decompressed-size ["(3x3)XYZ"
                        "X(8x2)(3x3)ABCY"
                        "(27x12)(20x12)(13x14)(7x10)(1x12)A"
                        "(25x3)(3x3)ABC(2x3)XY(5x2)PQRSTX(18x9)(3x2)TWO(5x7)SEVEN"])
