(ns p07
  (:require [clojure.string :as str]))

(def data-string (slurp "../input/07.txt"))

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

(defn parse-register [s]
  (let [name (re-find #"[a-z]+" s)]
    {:register (keyword name) 
     :length (count name)}))

(defn parse-number [s]
  (read-string (re-find #"\d+" s)))

(def digits (set "0123456789"))

(defn parse-op 
  "Read out the OP, either AND OR LSHIFT or RSHIFT."
  [s]
  (case (first s)
    \A {:op :AND :length 3}
    \O {:op :OR :length 2}
    \L {:op :LSHIFT :length 6}
    \R {:op :RSHIFT :length 6}))

(defn parse-left
  "Parse a single line of the instructions."
  [left]
  (cond
    (contains? digits (first left)) [:SET (parse-number left)]
    (= (first left) \N) [:NOT (:register (parse-register (subs left 4)))]
    :else (let [{reg :register len :length} (parse-register left)
                left (subs left (inc len))
                {op :op op-len :length} (parse-op left)
                left (subs left (inc op-len))]
             (if (contains? #{:LSHIFT :RSHIFT} op)
                 [op reg (parse-number left)]
                 [op reg (:register (parse-register left))]))))

(defn parse-line [line]
  (let [[left right] (str/split (str/trim line) #" -> ")]
    {:op (parse-left left) :register (:register (parse-register right))}))

(def test-program (vec (reverse (map parse-line (str/split-lines test-string))))) ; #'p07/test-program
; ({:op [:SET 123], :register :x} 
;  {:op [:SET 456], :register :y} 
;  {:op [:AND :x :y], :register :d} 
;  {:op [:OR :x :y], :register :e}
;  {:op [:LSHIFT :x 2], :register :f} 
;  {:op [:RSHIFT :y 2], :register :g} 
;  {:op [:NOT :x], :register :h} 
;  {:op [:NOT :y], :register :i}

(def program (vec (reverse (map parse-line (str/split-lines data-string)))))

(comment
  (+ 1 2))

(defn value [state op]
  (let [[op a b] op
        aval (get state a)
        bval (get state b)]
    (case op
      :SET a
      :AND (if (and aval bval) (bit-and aval bval))
      :OR  (if (and aval bval) (bit-or aval bval))
      :LSHIFT (if aval (bit-shift-left aval b))
      :RSHIFT (if aval (unsigned-bit-shift-right aval b))
      :NOT (if aval (bit-not aval)))))

(defn step [state insts stop]
  (if (or (empty? insts) (get state stop)) state
    (let [inst (peek insts)
          {op :op register :register} inst
          v (value state op)]
      (if (some? v) 
        (recur 
         (assoc state register v)
         (pop insts))
        (recur 
          state
          (vec (cons inst (pop insts))))))))




