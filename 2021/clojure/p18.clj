(ns advent18
  (:require
   [clojure.zip :as zip]
   [clojure.test :as test]
   [clojure.string :as str]))

(def data-string (slurp "../input/18.txt"))
(def test-string "[[[0,[5,8]],[[1,7],[9,6]]],[[4,[1,2]],[[1,4],2]]]
[[[5,[2,8]],4],[5,[[9,9],0]]]
[6,[[[6,2],[5,6]],[[7,6],[4,7]]]]
[[[6,[0,7]],[0,9]],[4,[9,[9,0]]]]
[[[7,[6,4]],[3,[1,3]]],[[[5,5],1],9]]
[[6,[[7,3],[3,2]]],[[[3,8],[5,7]],4]]
[[[[5,4],[7,7]],8],[[8,3],8]]
[[9,3],[[9,9],[6,[4,9]]]]
[[2,[[7,7],7]],[[5,8],[[9,3],[0,2]]]]
[[[[5,2],5],[8,[3,7]]],[[5,[7,5]],[4,4]]]")

(defn process [s]
  (map read-string (str/split-lines s)))

(def data (process data-string))
(def test-data (process test-string))

(defn pair?
  "Is this a pair of two values?"
  [x] (and (vector? x)
           (= (count x) 2)
           (number? (first x))
           (number? (second x))))

(defn split-num [n]
  (let [a (quot n 2)
        b (- n a)]
    [a b]))

(defn find-split [loc]
  (loop [loc loc]
    (let [node (zip/node loc)]
      (cond
        (zip/end? loc) nil
        (and (number? node) (>= node 10)) loc
        :else (recur (zip/next loc))))))

(defn split-tree
  "Split at the first value or nil."
  [data]
  (let [loc (find-split (zip/vector-zip data))]
    (if (nil? loc) nil
        (zip/root
         (zip/edit loc split-num)))))

(defn depth [loc]
  (count (take-while some? (iterate zip/up loc))))

(defn find-explode [loc]
  (loop [loc loc]
    (let [node (zip/node loc)]
      (cond
        (zip/end? loc) nil
        (and (pair? node) (> (depth loc) 4)) loc
        :else (recur (zip/next loc))))))

(defn next-number [loc]
  (->> (iterate zip/next loc)
       (drop 1)
       (take-while (complement zip/end?))
       (filter (comp number? zip/node))
       first))

(defn prev-number [loc]
  (->> (iterate zip/prev loc)
       (drop 1)
       (take-while (complement nil?))
       (filter (comp number? zip/node))
       first))

(defn add-next-number [loc b]
  (if-let [numloc (next-number loc)]
    (prev-number (zip/replace numloc (+ (zip/node numloc) b)))
    loc))

(defn add-prev-number [loc a]
  (if-let [numloc (prev-number loc)]
    (next-number (zip/replace numloc (+ (zip/node numloc) a)))
    loc))

(defn explode
  "Explode a 4 deep pair if there is one otherwise nil."
  [data]
  (let [orig (zip/vector-zip data)
        loc (find-explode orig)]
    (if (nil? loc) nil
        (let [[a b] (zip/node loc)]
          (-> loc
              (zip/replace 0)
              (add-next-number b)
              (add-prev-number a)
              (zip/root))))))

(test/deftest test-explode
  (test/are [data result] (= (explode data) result)
    [[[[[9 8] 1] 2] 3] 4]   [[[[0 9] 2] 3] 4]
    [7 [6 [5 [4 [3 2]]]]]  [7 [6 [5 [7 0]]]]
    [[6 [5 [4 [3 2]]]] 1]  [[6 [5 [7 0]]] 3]
    [[3 [2 [8 0]]] [9 [5 [4 [3 2]]]]]  [[3 [2 [8 0]]] [9 [5 [7 0]]]]))

(defn simplify
  "Simplify a snailfish expression."
  [x]
  (loop [x x]
    (let [s (explode x)]
      (if s (recur s)
          (let [y (split-tree x)]
            (if y (recur y) x))))))

(defn add-snailfish [a b]
  (simplify [a b]))

(test/deftest test-addition
  (test/is (= (add-snailfish
               [[[[4 3] 4] 4] [7 [[8 4] 9]]]
               [1 1])
              [[[[0 7] 4] [[7 8] [6 0]]] [8 1]])))

(defn magnitude [expr]
  (if (number? expr) expr
      (let [[a b] expr]
        (+ (* 3 (magnitude a)) (* 2 (magnitude b))))))

(defn part-1 [data]
  (magnitude (reduce add-snailfish data)))

(time (def ans1 (part-1 data)))
(println)
(println "Answer 1:" ans1)

(defn part-2 [data]
  (reduce
   max
   (for [x data
         y data
         :when (not= x y)]
     (magnitude (add-snailfish x y)))))

(time (def ans2 (part-2 data)))
(println)
(println "Answer 2:" ans2)

(test/deftest test-answers
  (test/is (= (part-1 data) 4323))
  (test/is (= (part-2 data) 4749)))

(test/run-tests)
