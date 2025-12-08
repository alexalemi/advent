;; # Advent of Code - 2025 - Day 1
(ns p01
  [:require [clojure.test :as test]
   [clojure.string :as str]])

;; ## Load data

(defonce data-string (slurp "../input/01.txt"))
(def test-string "L68
L30
R48
L5
R60
L55
L1
L99
R14
L82")

(defn read-line [s]
  [(keyword (str (first s)))
   (read-string (subs s 1))])

(defn split-lines [s]
  (str/split s #"\s+"))

(defn parse [s]
  (map read-line (split-lines s)))

(def data (parse data-string))
(def test-data (parse test-string))

;; ## Part 1
;; We want to implement a simple state transformation function.

(def SIZE 100)
(def INIT 50)

(defn step [state [dir amount]]
  (mod (({:L - :R +} dir) state amount) SIZE))

(defn part-1 [data]
  (->> data
       (reductions step INIT)
       (filter zero?)
       (count)))

(assert (= (part-1 test-data) 3))

(def ans-1 (part-1 data))

;; ## Part 2
;; Now we want to know how many times we pass through zero.

(defn steps [state [dir amount]]
  (range amount))

(defn step-zeros [[state zeros] [dir amount]]
  (defn step [x] (mod (({:L dec :R inc} dir) x) SIZE))
  (loop [state state
         zeros zeros
         remaining amount]
    (if (zero? remaining) [state zeros]
        (let [next (step state)]
          (recur next (if (zero? next) (inc zeros) zeros) (dec remaining))))))

(reduce step-zeros [INIT 0] test-data)

(defn part-2 [data]
  (second (reduce step-zeros [INIT 0] data)))

(assert (= (part-2 test-data) 6))

(def ans-2 (part-2 data))

(defn -main [& args]
  (println "Answer1: " ans-1)
  (println "Answer2: " ans-2))
