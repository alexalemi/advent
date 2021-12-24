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
    (cond
      (-zero? b)
      (assoc state reg 0)

      (-zero? a)
      (assoc state reg 0)

      (one? b)
      (assoc state reg a)

      (one? a)
      (assoc state reg b)

      (and (number? a) (number? b))
      (assoc state reg (* a b))

      (and (number? b) ; (* 26 (+ sym num)) -> (+ (* 26 sym) (* 26 num))
           (seq? a)
           (= (first a) :+)
           (keyword? (second a))
           (number? (third a)))
      (assoc state reg `(:+ (:* ~b ~(second a)) ~(* b (third a))))

      :else
      (assoc state reg `(:* ~a ~b)))))

(defn add [state reg b]
  (let [a (state reg)
        b (value state b)]
    (cond
      (-zero? b)
      (assoc state reg a)

      (-zero? a)
      (assoc state reg b)

      (and (number? a) (number? b))
      (assoc state reg (+ a b))

      (and (number? b); (+ (+ :let num) num)
           (seq? a)
           (= (first a) :+)
           (keyword? (second a))
           (number? (third a)))
      (assoc state reg (:+ (second a) (+ b (third a))))

      (and (seq? a) ; (+ (+ X num) (+ Y num))
           (= (first a) :+)
           (number? (third a))
           (seq? b)
           (= (first b) :+)
           (number? (third b)))
      (assoc state reg `(:+ ~(second a) ~(second b) ~(+ (third b) (third a))))

      :else
      (assoc state reg `(:+ ~a ~b)))))

(defn -mod [state reg b]
  (let [a (state reg)
        b (value state b)]
    (cond
      (-zero? a)
      (assoc state reg 0)

      (and (number? a) (number? b))
      (assoc state reg (mod a b))

      (and (number? b)
           (= (first a) :+)  ; (mod (+ :let num) 26)
           (keyword? (second a))
           (number? (third a))
           (> (- b (third a)) 9))
      (assoc state reg a)

      :else
      (assoc state reg `(:mod ~a ~b)))))


(defn div [state reg b]
  (let [a (state reg)
        b (value state b)]
    (cond
      (-zero? a)
      (assoc state reg 0)

      (one? b)
      (assoc state reg a)

      (and (number? a) (number? b))
      (assoc state reg (quot a b))

      (and (number? b) ; (div (+ (* 26 X) (+ :let num)) 26) -> X
           (= b 26)
           (= (first a) :+)
           (= (first (second a)) :*) ; special case
           (= (second (second a)) 26)
           (= (first (third a)) :+)
           (keyword? (second (third a)))
           (< (third (third a)) (- 26 9)))
      (assoc state reg (third (second a)))

      :else
      (assoc state reg `(:div ~a ~b)))))

(defn eql [state reg b]
  (let [a (state reg)
        b (value state b)]
    (cond
      (= a b)
      (assoc state reg 1)

      (and (number? a) (number? b) (not= a b))
      (assoc state reg 0)

      (and (number? a) (keyword? b) (> a 9)) ; symbols can't be more than 9
      (assoc state reg 0)

      :else
      (assoc state reg `(:= ~a ~b)))))


(defn step [state inst]
  (let [[op a b] inst]
    (case op
      :inp (inp state a)
      :mul (mul state a b)
      :add (add state a b)
      :mod (-mod state a b)
      :div (div state a b)
      :eql (eql state a b))))

(let [state state
      prog data
      n 44
      prog (take n prog)]
  (println n (last prog))
  (reduce step state prog))
;; => (:+ (:* 26 (:+ (:* 26 (:+ (:* 26 (:+ (:* 26 (:+ (:* 26 (:+ (:* 26 (:+ :a 16)) (:+ :b 11))) (:+ :e 12))) (:+ :f 2))) (:+ :h 4))) (:+ :i 12))) (:+ :n 15))




(defn base-26 [vals]
  (loop [vals vals
         num 0]
    (if (empty? vals) num
        (recur (rest vals) (+ (* num 26) (first vals))))))

(base-26 [16 11 12 2 4 12 15])

(comment
 (let [state (assoc state :inp (list 0 0 9 9 0 0 9 0 0 9 9 9 9 0))
       prog data]
   (:z (reduce step state prog))))

(comment
  (let [[a b c d e f g h i j k l m n] [0 0 9 9 0 0 9 0 0 9 9 9 9 0]]
   (+ (* 26 (+ (* 26 (+ (* 26 (+ (* 26 (+ (* 26 (+ (* 26 (+ a 16)) (+ b 11))) (+ e 12))) (+ f 2))) (+ h 4))) (+ i 12))) (+ n 15))))

