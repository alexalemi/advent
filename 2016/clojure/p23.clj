(ns p23
  (:require [clojure.string :as str]))

;; # 2016 - Day 23
;;
;; For this day it seems like we can reuse our computer architecture from day 12
;;
;; First we load in both the problem data and a test string
(def data-string (slurp "../input/23.txt"))

(def test-string "cpy 2 a
tgl a
tgl a
tgl a
cpy 1 a
dec a
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


(defn toggle [inst]
 (let [[op a b] inst]
   (cond
     (nil? b) (if (= op :inc)
                [:dec a]
                [:inc a])
     (some? b) (if (= op :jnz)
                 [:cpy a b]
                 [:jnz a b]))))


(defn step [machine]
  (let [{:keys [codes loc]} machine
        code (get codes loc)
        [op a b] code]
    (case op
      nil (assoc machine :done true)
      :cpy (if (keyword? b)
             (-> machine
              (assoc-in [:registers b] (value machine a))
              (update :loc inc))
             (update machine :loc inc))
      :inc (if (keyword? a)
            (-> machine
             (update-in [:registers a] inc)
             (update :loc inc))
            (update machine :loc inc))
      :dec (if (keyword? a)
             (-> machine
              (update-in [:registers a] dec)
              (update :loc inc))
             (update machine :loc inc))
      :jnz (if (zero? (value machine a))
             (update machine :loc inc)
             (update machine :loc + (value machine b)))
      :tgl (let [which (+ loc (value machine a))
                 code (get-in machine [:codes which])]
             (if code
                (-> machine
                 (assoc-in [:codes which] (toggle code))
                 (update :loc inc))
                (update machine :loc inc))))))

;; Having defined that, we can execute the machine and return the value in the :a register.
(defn run-and-return-a [machine]
  ((comp :a :registers) (last (take-while (complement :done) (iterate step machine)))))


(run-and-return-a (machine test-data))

(defonce ans1 (run-and-return-a (assoc-in (machine data) [:registers :a] 7)))
(println "Answer1:" ans1)

(comment
  (defonce ans2 (run-and-return-a (assoc-in (machine data) [:registers :a] 12)))
  (println "Answer2:" ans2))

;; The first part ran okay, but the second part never finished,
;; if we look at the actual code we are executing, it becomes clear that it is
;;
;;      = 89*84 + 12! = 479009076
;;
