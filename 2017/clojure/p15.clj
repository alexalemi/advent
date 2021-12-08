(ns advent15
  (:require
   [clojure.test :as test]
   [clojure.edn :as edn]
   [clojure.string :as str]))

(def test-string "Generator A starts with 65
Generator B starts with 8921")
(def data-string (slurp "../input/15.txt"))

(defn process-line [s]
  (edn/read-string ((re-matches #"Generator \w starts with (\d+)" s) 1)))

(defn process [s]
  (map process-line (str/split-lines s)))

(def data (process data-string))
(def test-data (process test-string))

(def MODULO 2147483647)
(def FACTORS [16807 48271])
(defn make-generator [factor]
  (fn [^Integer state] ^Integer (mod (* ^Integer state ^Integer factor) ^Integer MODULO)))
(def generators (map make-generator FACTORS))
(def genA (first generators))
(def genB (second generators))


(defn step [state]
  (let [[a b] state]
    [(genA a) (genB b)]))

(defn low-16 [x]
  (map #(bit-test ^Integer x %) (range 16)))

(defn part-1 [data]
  ; (reduce (fn [acc [a b]] (if (= (low-16 a) (low-16 b)) (inc acc) acc)) 0 (take 40000000 (iterate step data)))
  (let [[^Integer a ^Integer b] data]
    (reduce + (take 40000000 (map (fn [^Integer a ^Integer b] (if (= (low-16 a) (low-16 b)) 1 0))
                               (iterate genA a)
                               (iterate genB b))))))

(time (def ans1 (part-1 data)))
; (def ans1 592)
(println)
(println "Answer 1:" ans1)  ; 592

(defn part-2 [data]
  (let [[^Integer a ^Integer b] data]
    (reduce + (take 5000000 (map (fn [^Integer a ^Integer b] (if (= (low-16 a) (low-16 b)) 1 0))
                              (filter #(= (mod % 4) 0) (iterate genA a))
                              (filter #(= (mod % 8) 0) (iterate genB b)))))))


(time (def ans2 (part-2 data)))
(println)
(println "Answer 2:" ans2)  ; 320


