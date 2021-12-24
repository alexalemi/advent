(ns advent24
  (:require
   [clojure.test :as test]
   [clojure.string :as str]))

(def data-string (slurp "../input/24.txt"))
(def test-strings
  ["inp x
mul x -1"
   "inp z
inp x
mul z 3
eql z x"
   "inp w
add z w
mod z 2
div w 2
add y w
mod y 2
div w 2
add x w
mod x 2
div w 2
mod w 2"])

(defn keywordize [x]
  (if (symbol? x) (keyword x) x))

(defn process-line [line]
  (map keywordize (read-string (str "[" line "]"))))

(defn process [s]
  (mapv process-line (str/split-lines s)))


(def data (process data-string))

(def registers #{:w :x :y :z})

(def state {:x 0 :y 0 :z 0 :w 0 :inp (list :a :b :c :d :e :f :g :h :i :j :k :l :m :n)})

(defn third [coll]
  (nth coll 2))

(defn inp [state reg]
  (let [inp (:inp state)
        next (first inp)
        inp (rest inp)]
   (assoc state reg next :inp inp)))

(defn value [state b]
  (if (registers b) (state b) b))

(defn one? [x]
  (and (number? x) (= x 1)))

(defn -zero? [x]
  (and (number? x) (= x 0)))

(defn mul [state reg b]
  (let [a (state reg)
        b (value state b)]
     (assoc state reg (list :* a b))))

(defn add [state reg b]
  (let [a (state reg)
        b (value state b)]
    (assoc state reg (list :+ a b))))

(defn -mod [state reg b]
  (let [a (state reg)
        b (value state b)]
    (assoc state reg (list :mod a b))))


(defn div [state reg b]
  (let [a (state reg)
        b (value state b)]
    (assoc state reg (list :div a b))))

(defn eql [state reg b]
  (let [a (state reg)
        b (value state b)]
    (assoc state reg (list := a b))))

(def rules
  {'(:* (? x) 0) 0
   '(:* 0 (? x)) 0
   '(:* (? x) 1) 'x
   '(:* 1 (? x)) 'x
   '(:* (?c A) (?c B)) '(* A B)})

(defn wildcard? [x]
  (and (list? x)
       (= (first x) '?)))

(defn wildcard-variable? [x]
  (and (list? x)
       (= (first x) '?v)))

(defn wildcard-constant? [x]
  (and (list? x)
       (= (first x) '?c)))

(defn wildcard-form [x] (second x))

(defn atom? [x] (not (coll? x)))

(defn wildcard-match [bindings var expr]
  (let [prev (bindings var)]
   (cond
     (nil? prev) (assoc bindings var expr)
     (= prev expr) bindings
     :else nil)))

(defn matcher
  "Tries to match the rule against the expression,
  if not, nil otherwise return a dictionary
  of bindings."
  ([rule expr] (matcher rule expr {}))
  ([rule expr bindings]
   (println "rule=" rule "expr=" expr "bindings=" bindings)
   (cond
     (nil? bindings) nil  ; short-circuit a failed match

     (atom? rule)
     (if (= rule expr) bindings nil)

     (wildcard? rule)
     (wildcard-match bindings (wildcard-form rule) expr)

     (wildcard-constant? rule)
     (if (number? expr)
       (wildcard-match bindings (wildcard-form rule) expr)
       nil)

     (wildcard-variable? rule)
     (if (keyword? expr)
       (wildcard-match bindings (wildcard-form rule) expr)
       nil)

     (empty? rule)
     (if (empty? expr) bindings nil)

     (empty? expr)
     (if (empty? rule) bindings nil)

     (atom? expr) nil

     :else
     (matcher (rest rule)
              (rest expr)
              (matcher
               (first rule)
               (first expr)
               bindings)))))

(defn evaluator
  "Instantiate the skeleton with the binding."
  [binding skeleton])


(defn simplify [expr]
  expr)

(defn simplify-all [state]
  (-> state
      (update :x simplify)
      (update :y simplify)
      (update :z simplify)
      (update :w simplify)))

(defn step [state inst]
  (let [[op a b] inst]
    (simplify
     (case op
      :inp (inp state a)
      :mul (mul state a b)
      :add (add state a b)
      :mod (-mod state a b)
      :div (div state a b)
      :eql (eql state a b)))))



(comment
  (defn base-26 [vals]
   (loop [vals vals
          num 0]
     (if (empty? vals) num
         (recur (rest vals) (+ (* num 26) (first vals))))))

  (base-26 [16 11 12 2 4 12 15]))

(comment
 (let [state (assoc state :inp (list 0 0 9 9 0 0 9 0 0 9 9 9 9 0))
       prog data]
   (:z (reduce step state prog))))

(comment
  (let [[a b c d e f g h i j k l m n] [0 0 9 9 0 0 9 0 0 9 9 9 9 0]]
   (+ (* 26 (+ (* 26 (+ (* 26 (+ (* 26 (+ (* 26 (+ (* 26 (+ a 16)) (+ b 11))) (+ e 12))) (+ f 2))) (+ h 4))) (+ i 12))) (+ n 15))))


(let [state state
      prog data
      n 5 ; 44
      prog (take n prog)]
  (println n (last prog))
  (reduce step state prog))
;; => (:+ (:* 26 (:+ (:* 26 (:+ (:* 26 (:+ (:* 26 (:+ (:* 26 (:+ (:* 26 (:+ :a 16)) (:+ :b 11))) (:+ :e 12))) (:+ :f 2))) (:+ :h 4))) (:+ :i 12))) (:+ :n 15))
