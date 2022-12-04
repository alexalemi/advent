(ns p07
  (:require [clojure.string :as str]))

(def data-string (str/trim (slurp "../input/07.txt")))

(def test-string 
  "123 -> x
456 -> y
x AND y -> d
x OR y -> e
x LSHIFT 2 -> f
y RSHIFT 2 -> g
NOT x -> h
NOT y -> i")

;;; Handwritten parser

(defn parse-both [s]
  (let [name (re-find #"[a-z0-9]+" s)]
    {:register name
     :length (count name)}))

(defn parse-register [s]
  (let [name (re-find #"[a-z]+" s)]
    {:register (keyword name) 
     :length (count name)}))

(defn parse-number [s]
  (read-string (re-find #"\d+" s)))

(def digits (set "0123456789"))

(defn parse-slot [s]
  (if (every? digits s) (parse-number s) (:register (parse-register s))))

(comment
  (parse-slot "1231")
  (parse-slot "x"))

(defn parse-op 
  "Read out the OP, either AND OR LSHIFT or RSHIFT."
  [s]
  (case (first s)
    \A {:op :AND :length 3}
    \O {:op :OR :length 2}
    \L {:op :LSHIFT :length 6}
    \R {:op :RSHIFT :length 6}
    :else (println "I don't understand" s)))

(defn parse-left
  "Parse a single line of the instructions."
  [left]
  (cond
    (every? digits left) [:SET (parse-slot left)]
    (not (str/includes? left " ")) [:LOOKUP (parse-slot left)]
    (= (first left) \N) [:NOT (parse-slot (subs left 4))]
    :else (let [{reg :register len :length} (parse-both left)
                left (subs left (inc len))
                {op :op op-len :length} (parse-op left)
                left (subs left (inc op-len))]
            [op (parse-slot reg) (parse-slot left)])))

(comment
  (every? digits "1231")
  (parse-left "x AND y")
  (parse-left "123")
  (parse-left "a")
  (parse-left "NOT x")
  (parse-register "x AND y"))


(defn parse-line [line]
  (let [[left right] (str/split (str/trim line) #" -> ")]
    {:op (parse-left left) :register (parse-slot right)}))

(defn format-program [s]
  (->> (str/split-lines s)
       (map parse-line)
       reverse
       vec
       (reduce (fn [m x] (assoc m (:register x) (:op x))) {})))


(def test-program (format-program test-string))
; ({:op [:SET 123], :register :x} 
;  {:op [:SET 456], :register :y} 
;  {:op [:AND :x :y], :register :d} 
;  {:op [:OR :x :y], :register :e}
;  {:op [:LSHIFT :x 2], :register :f} 
;  {:op [:RSHIFT :y 2], :register :g} 
;  {:op [:NOT :x], :register :h} 
;  {:op [:NOT :y], :register :i}

(def program (format-program data-string))

(comment
  (parse-line "eu RSHIFT 1 -> fb")
  (->> (str/split-lines data-string)
       (map parse-line)))

(defn value [prog register cache]
  (let [mem (atom cache)]
    ((fn e [x]
      (if (number? x)
          x
          (if-let [y (find @mem x)]
            (val y)
            (let [[op a b] (get prog x) 
                  res (case op
                       :LOOKUP (e a)
                       :SET (e a)
                       :AND (bit-and (e a) (e b))
                       :OR  (bit-or (e a) (e b))
                       :LSHIFT (bit-shift-left (e a) (e b))
                       :RSHIFT (unsigned-bit-shift-right (e a) (e b))
                       :NOT (bit-not (e a))
                       :else (println "I don't understand:" x op a b))]
                (swap! mem assoc x res)
                res)))) register)))

      
(comment
  (value test-program :d {})
  (value program :a {})
  (+ 1 1))

(defonce ans1 (value program :a {}))
(println "Answer1:" ans1)

(defonce ans2 (value program :a {:b ans1}))
(println "Answer2:" ans2)

