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

     (atom? expr) nil

     (empty? expr)
     (if (empty? rule) bindings nil)

     :else
     (matcher (rest rule)
              (rest expr)
              (matcher
               (first rule)
               (first expr)
               bindings)))))


(defn placeholder? [x]
  (and (list? x)
       (= (first x) '!)))

(defn placeholder-form [x] (second x))

(defn fill-in [dict form]
  (cond
    (atom? form) (or (dict form) form)
    (empty? form) form
    :else
    (cons (fill-in dict (first form))
          (fill-in dict (rest form)))))


(defn evaluator
  "Instantiate the skeleton with the binding."
  [binding skeleton]
  (cond
    (atom? skeleton) skeleton
    (empty? skeleton) skeleton

    (placeholder? skeleton)
    (let [form (placeholder-form skeleton)]
      (eval (fill-in binding form)))

    :else ; should be a list
    (cons (evaluator binding (first skeleton))
          (evaluator binding (rest skeleton)))))


(declare simplify-exp)

(defn number-equal [a b]
  (if (= a b) 1 0))

(def the-rules
  {'(:* 0 (? x)) 0
   '(:* (? x) 0) 0
   '(:* (? x) 1) '(! 'x)
   '(:* 1 (? x)) '(! 'x)
   '(:* (?c n) (?c m)) '(! (* n m))

   '(:+ 0 (? x)) '(! 'x)
   '(:+ (? x) 0) '(! 'x)
   '(:+ (?c n) (?c m)) '(! (+ n m))

   '(:div 0 (? x)) 0
   '(:div (? x) 1) '(! 'x)
   '(:div (?c n) (?c m)) '(! (quot n m))

   '(:mod 0 (? x)) 0
   '(:mod (?c n) (?c m)) '(! (mod n m))
   '(:mod (:+ (?v x) 16) 26) '(:+ (! x) 16)

   '(:= (?c n) (?v x)) '(! (if (> n 9) 0 (quote (= n x))))
   '(:= (?c n) (?c m)) '(! (number-equal n m))

   '(:+ (:+ (?v x) (?c n)) (?c m)) '(:+ (! x) (! (+ n m)))
   '(:= (:+ (?v x) 28) (?v y)) 0
   '(:= (:+ (?v x) 24) (?v y)) 0
   '(:mod (:+ (:* (? x) 26) (? y)) 26) '(! 'y)
   '(:div (:+ (:* (? x) 26) (? y)) 26) '(! 'x)})

   ; ; '(:* (:+ (?v x) (?c n)) (?c m)) '(:+ (:* (! m) (! x)) (! (* n m)))

   ;general distributive
   ; '(:* (:+ (? x) (? y)) (? z)) '(:+ (:* (! z) (! x)) (:* (! z) (! y)))})

   ; ; '(:* (:+ (?v x) (?c n)) (?c m)) '(:+ (:* (! 'x) (! m)) (! (* n m)))})

(defn try-rules [exp]
  (letfn [(scan [rules]
            (if (empty? rules) exp
                (let [[rule skeleton] (first rules)
                      dict (matcher rule exp)]
                  (if (nil? dict)
                   (scan (rest rules))
                   (simplify-exp (evaluator dict skeleton))))))]
     (scan the-rules)))

(defn simplify-exp [exp]
  (cond
    (nil? exp) nil
    (atom? exp) exp
    (empty? exp) exp

    :else
    (try-rules (if (list? exp)
                 (cons (simplify-exp (first exp))
                       (simplify-exp (rest exp)))
                 exp))))

(defn simplify [exp]
  (simplify-exp exp))


(defn simplify-all [state]
  (-> state
      (update :x simplify)
      (update :y simplify)
      (update :z simplify)
      (update :w simplify)))

(defn step [state inst]
  (let [[op a b] inst]
    (simplify-all
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
      n 100, ; 44
      prog (take n prog)]
  (println n (last prog))
  (:z (reduce step state prog)))
;; => (:+ (:* (:div (:+ (:* (:+ (:* (:+ :a 16) 26) (:+ :b 11)) (:+ (:* 25 (:= (:= (:+ :c 7) :d) 0)) 1)) (:* (:+ :d 12) (:= (:= (:+ :c 7) :d) 0))) 26) (:+ (:* 25 (:= (:= (:+ (:mod (:+ (:* (:+ (:* (:+ :a 16) 26) (:+ :b 11)) (:+ (:* 25 (:= (:= (:+ :c 7) :d) 0)) 1)) (:* (:+ :d 12) (:= (:= (:+ :c 7) :d) 0))) 26) -3) :e) 0)) 1)) (:* (:+ :e 12) (:= (:= (:+ (:mod (:+ (:* (:+ (:* (:+ :a 16) 26) (:+ :b 11)) (:+ (:* 25 (:= (:= (:+ :c 7) :d) 0)) 1)) (:* (:+ :d 12) (:= (:= (:+ :c 7) :d) 0))) 26) -3) :e) 0)))
