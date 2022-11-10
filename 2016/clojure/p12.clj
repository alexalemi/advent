(ns p12
  (:require [clojure.string :as str]))

;; # 2016 Day 12
;; For this challenge it looks like we have to implement a simple virtual machine.


;; First we load in both the problem data and a test string
(def data-string (slurp "../input/12.txt"))

(def test-string "cpy 41 a
inc a
inc a
dec a
jnz a 2
dec a")

;; In order to turn this into some representable code, let's consume the
;; string and put out a list of instructions.

(def DIGITS (into #{} "+-0123456789"))

(defn value-or-reg [x]
  (if (every? DIGITS x)
    (read-string x)
    (keyword x)))

(defn process-line [line]
  (let [[op a b] (str/split line #" ")]
    (if b
        [(keyword op) (value-or-reg a) (value-or-reg b)]
        [(keyword op) (value-or-reg a)])))

(defn process [s]
  (mapv process-line (str/split-lines s)))

(def data (process data-string))
(def test-data (process test-string))

;; To represent a machine, we'll load in the code, as well as some `loc` field and the register values themselves.

(defn machine [codes]
  {:registers {:a 0 :b 0 :c 0 :d 0}
   :loc 0
   :done false
   :codes codes})

;; Now let's implement the machine itself, the main method will be step, which will do a single update given the code.
(defn value [machine x]
  (if (keyword? x) ((:registers machine) x) x))

(defn step [machine]
  (let [{:keys [codes loc]} machine
        code (get codes loc)
        [op a b] code]
    (case op
      nil (assoc machine :done true)
      :cpy (-> machine
            (assoc-in [:registers b] (value machine a))
            (update :loc inc))
      :inc (-> machine
            (update-in [:registers a] inc)
            (update :loc inc))
      :dec (-> machine
            (update-in [:registers a] dec)
            (update :loc inc))
      :jnz (if (zero? (value machine a))
             (update machine :loc inc)
             (update machine :loc + (value machine b))))))

;; Having defined that, we can execute the machine and return the value in the :a register.
(defn run-and-return-a [machine]
  ((comp :a :registers) (last (take-while (complement :done) (iterate step machine)))))

(run-and-return-a (machine test-data))

(defonce ans1 (run-and-return-a (machine data)))
(println "Answer1:" ans1)


(defonce ans2 (run-and-return-a (assoc-in (machine data) [:registers :c] 1)))
(println "Answer2:" ans2)
