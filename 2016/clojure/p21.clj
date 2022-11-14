(ns p21
  (:require [clojure.string :as str]
            [clojure.math.combinatorics :as combo]))

;; # 2016 - Day 21
;; For this puzzle we have to implement a little machine that takes instructions
;; and scrambles a password.

(def data-string (str/split-lines (slurp "../input/21.txt")))

(def START "abcdefgh")

(defn to-int [s] (read-string s))

(def regexs
  #{[#"swap position (\d) with position (\d)" (fn [[_ a b]] `(swap-pos ~(to-int a) ~(to-int b)))]
    [#"swap letter (\w) with letter (\w)" (fn [[_ a b]] `(swap-let ~(first a) ~(first b)))]
    [#"rotate (left|right) (\d+) (step|steps)" (fn [[_ dir size _]] `(rotate-pos ~(* (if (= dir "right") 1 -1) (to-int size))))]
    [#"rotate based on position of letter (\w)" (fn [[_ let]] `(rotate-let ~(first let)))]
    [#"reverse positions (\d) through (\d)" (fn [[_ a b]] `(reverse-pos ~(to-int a) ~(to-int b)))]
    [#"move position (\d) to position (\d)" (fn [[_ a b]] `(move ~(to-int a) ~(to-int b)))]})


(defn process-line [line]
  (some (fn [[r f]] (if-let [match (re-matches r line)] (f match))) regexs))


(defn to-state [start] (into {} (map vector start (range))))

(defn map-vals [m f]
  (reduce-kv (fn [m k v] (assoc m k (f v))) {} m))

(defn reverse-map [m]
  (reduce-kv (fn [m k v] (assoc m v k)) {} m))

(defn render [state]
  (let [state (reverse-map state)]
     (apply str (for [i (range (count state))]
                  (state i)))))

(defn swap-pos [state a b]
  (map-vals state #(or ({a b b a} %) %)))

(defn swap-let [state a b]
  (-> state
      (assoc a (state b))
      (assoc b (state a))))

(defn reverse-pos [state a b]
  (map-vals state #(if (<= a % b) (- b (- % a)) %)))

(defn rotate-pos [state n]
  (map-vals state #(mod (+ % n) (count state))))

(defn move [state a b]
  (map-vals state #(if (= % a)
                     b
                     ((fn [x] (if (>= x b) (inc x) x)) ((fn [x] (if (> x a) (dec x) x)) %)))))


(defn rotate-let [state l]
  (let [loc (state l)]
    (map-vals state (fn [x] (mod (+ x 1 loc (if (>= loc 4) 1 0)) (count state))))))

(def data (mapv process-line data-string))

(def test-result (let [state (to-state "abcde")
                       prog '((swap-pos 4 0)
                              (swap-let \d \b)
                              (reverse-pos 0 4)
                              (rotate-pos -1)
                              (move 1 4)
                              (move 3 0)
                              (rotate-let \b)
                              (rotate-let \d))]
                      (eval `(-> ~state
                                 ~@prog
                                 render))))

(defn scramble [start]
  (let [state (to-state start)]
    (eval `(-> ~state
               ~@data
               render))))

(defonce ans1 (scramble "abcdefgh"))
(println "Answer1:" ans1)

;; # Part 2
;; For this part, we are supposed to unscramble a final password.
;; My first go was to just brute-force this:

(comment
  (defonce ans2 (apply str (first (filter #(= "fbgdceah" (scramble %)) (combo/permutations "fbgdceah")))))
  (println "Answer2:" ans2))

;; But then I realized we could more elegantly actually reverse the forward evaluations
;; by replacing each instruction with its inverse
;;
;; The tricky instruction seems to be the `rotate-let` one, and for that I'll special case to the 8
;; letter words we are dealing with.

(def INVERSE-TABLE
  {1 -1, 3 -2, 5 -3, 7 -4, 2 2, 4 1, 6 0, 0 -1})

(defn inverse-rotate-let [state l]
  (let [loc (state l)]
    (rotate-pos state (INVERSE-TABLE loc))))

(defn inverse-instruction [inst]
  (let [[arg a b] inst]
    (case arg
      p21/swap-pos inst
      p21/swap-let inst
      p21/rotate-pos `(rotate-pos ~(- a))
      p21/rotate-let `(inverse-rotate-let ~a)
      p21/reverse-pos inst
      p21/move `(move ~b ~a))))


(defn un-scramble [final]
  (let [state (to-state final)]
    (eval `(-> ~state
               ~@(map inverse-instruction (reverse data))
               render))))


(defonce ans2 (un-scramble "fbgdceah"))
(println "Answer2:" ans2)
