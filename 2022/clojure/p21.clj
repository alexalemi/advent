;; # 🎄 Advent of Code 2022 - Day 21 - Monkey Math
;; For today's puzzle we have to do some arithmetic manipulations.
(ns p21
  (:require [clojure.string :as str]
            [clojure.test :as test]))

(def data-string (slurp "../input/21.txt"))

(def test-string "root: pppw + sjmn
dbpl: 5
cczh: sllz + lgvd
zczc: 2
ptdq: humn - dvpt
dvpt: 3
lfqf: 4
humn: 5
ljgn: 2
sjmn: drzm * dbpl
sllz: 4
pppw: cczh / lfqf
lgvd: ljgn * ptdq
drzm: hmdt - zczc
hmdt: 32")

(defn parse [s]
  (into {}
        (for [line (str/split-lines s)]
          (let [[var expr] (str/split line #": ")]
            [(keyword var)
             (or (parse-long expr)
                 (let [[_ left op right] (re-find #"(\w\w\w\w) ([\*\+-/]) (\w\w\w\w)" expr)]
                   [(keyword op) (keyword left) (keyword right)]))]))))

(def data (parse data-string))
(def test-data (parse test-string))

;; ## Logic
;; For the first part we just have to evalute the big tree,
;; let's make a memoizing recursive evaluator to evaluate the
;; top level node.

(defn make-evaluate [env]
  (let [evaluate (fn [mem-evaluate sym]
                   (let [evaluate (fn [x] (mem-evaluate mem-evaluate x))
                         res (env sym)]
                     (if (number? res) res
                         (let [[op left right] res]
                           (case op
                             := (= (evaluate left) (evaluate right))
                             :+ (+ (evaluate left) (evaluate right))
                             :- (- (evaluate left) (evaluate right))
                             :* (* (evaluate left) (evaluate right))
                             :/ (/ (evaluate left) (evaluate right)))))))
        mem-evaluate (memoize evaluate)]
    (partial mem-evaluate mem-evaluate)))

(test/deftest test-part-1
  (test/is (= 152 ((make-evaluate test-data) :root))))

(def evaluate (make-evaluate data))

(def ans1 (evaluate :root))

;; ## Part 2
;; For part 2 we have to simplify the expression somewhat
;; we are told that the top level comparison is an equality
;; and that the `humn` keyword is an input that we have to apply.

;; To see what we're dealing with, we'll first render out the expression,
;; expanding the roots and leaving a `:humn` keyword in there.

(defn render [env sym]
  (let [x (env sym)]
    (if (or (number? x) (keyword? x)) x
        (let [[op left right] x
              left (render env left)
              right (render env right)]
          (if (and (number? left) (number? right))
            (case op
              := (= left right)
              :+ (+ left right)
              :- (- left right)
              :* (* left right)
              :/ (/ left right))
            [op left right])))))

;; At this point we'll have a top level equality,
;; Looking at our data, one side is a pure number and the other
;; side is a nested expression.  We'll simplify the expression
;; by peeling back expressions on the one side.
;;
;; That is, if we have a = x + b where a and b are numbers
;; then we know that x = a - b.  We'll do that sort of thing.

(defn inner-simplify [z op a b]
  (assert (number? z))
  (case op
    :+ (if (number? b)
         [(- z b) a]
         [(- z a) b])
    :- (if (number? b)
         [(+ z b) a]
         [(- a z) b])
    :* (if (number? b)
         [(/ z b) a]
         [(/ z a) b])
    :/ (if (number? b)
         [(* b z) a]
         [(/ a z) b])))

(defn simplify [expr]
  (let [[op left right] expr]
    (assert (= op :=))
    (cond
      (keyword? left) right
      (keyword? right) left
      (number? left)
      (let [[z a] (apply inner-simplify left right)]
        (-> expr
            (assoc 1 z)
            (assoc 2 a)))
      (number? right)
      (let [[z a] (apply inner-simplify right left)]
        (-> expr
            (assoc 1 z)
            (assoc 2 a))))))

(defn part-2 [data]
  (let [data (assoc-in data [:root 0] :=)
        data (assoc data :humn :humn)
        data (render data :root)]
    (first (filter number? (iterate simplify data)))))

(test/deftest test-part-2
  (test/is (= 301 (part-2 test-data))))

(def ans2 (part-2 data))

;; ## Extrapolation
;; Saw this online, but since the whole thing is a tree, we know its a linear function and we can simply
;; extrapolate to get the answer

(defn part-2-fast [data]
  (letfn [(f [x] ((make-evaluate (-> data (assoc-in [:root 0] :-) (assoc :humn x))) :root))]
    (/ (f 0) (- (f 0) (f 1)))))

;; ## Main

(defn -test [& _]
  (test/run-tests 'p21))

(defn -main [& _]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
