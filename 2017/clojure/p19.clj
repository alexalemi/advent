(ns advent19
  (:require
   [clojure.test :as test]
   [clojure.string :as str]))

(def test-string "     |          
     |  +--+    
     A  |  C    
 F---|----E|--+ 
     |  |  |  D 
     +B-+  +--+
")
(def data-string (slurp "../input/19.txt"))

(defn indexify [coll]
  (zipmap (range) coll))

(defn find-first [pred coll] (first (filter pred coll)))

(defn process [s]
  (let [board (for [[row line] (indexify (str/split-lines s))
                    [col c]    (indexify line)
                    :when (not= c \space)]
                [[row col] c])
        [start _] (find-first (fn [[loc _]] (let [[x _] loc] (zero? x))) board)]
    {:board (into {} board) :t 0 :loc start :direction :down :visited []}))

(defn neighbors [loc]
  (let [[x y] loc]
    #{[(inc x) y]
      [(dec x) y]
      [x (inc y)]
      [x (dec y)]}))

(defn move [loc direction]
  (let [[y x] loc]
    (case direction
      :up    [(dec y) x]
      :down  [(inc y) x]
      :left  [y (dec x)]
      :right [y (inc x)])))

(def opposite-direction
  {:down :up
   :up :down
   :right :left
   :left :right})

(def data (process data-string))
(def test-data (process test-string))

(def letters (into #{} "ABCDEFGHIJKLMNOPQRSTUVWXYZ"))

(defn find-direction [x y]
  (let [[y1 x1] x
        [y2 x2] y]
    (cond
      (and (> x2 x1) (= y1 y2)) :right
      (and (< x2 x1) (= y1 y2)) :left
      (and (= x2 x1) (> y2 y1)) :down
      (and (= x2 x1) (< y2 y1)) :up)))

(defn plus-move [data]
  (let [{:keys [board loc direction]} data
        newloc (first (filter board (disj (neighbors loc) (move loc (opposite-direction direction)))))]
    (assoc (assoc data :loc newloc) :direction (find-direction loc newloc))))

(defn non-plus-move [data]
  (let [{:keys [board loc direction]} data
        newloc (move loc direction)]
    (if (board loc) (assoc data :loc newloc) (assoc data :finished true))))

(defn step [data]
  (let [data (update data :t inc)
        {:keys [board loc visited]} data
        x (get board loc)]
    (cond-> data
      ; if we're standing on a letter
      (contains? letters x) (assoc :visited (conj visited x))
      ; if we're on a turn, find the neighboring place
      (= x \+) (plus-move)
      (not= x \+) (non-plus-move))))

(defn part-1 [data]
  (apply str (:visited (last (take-while (complement :finished) (iterate step data))))))

(test/deftest test-part-1
  (test/is (= (part-1 test-data) "ABCDEF"))
  (test/is (= (part-1 data) "GPALMJSOY")))

(time (def ans1 (part-1 data)))
(println)
(println "Answer 1:" ans1)

(defn part-2 [data]
  (dec (count (take-while (complement :finished) (iterate step data)))))

(test/deftest test-part-2
  (test/is (= (part-2 test-data) 38))
  (test/is (= (part-2 data) 16204)))

(time (def ans2 (part-2 data)))
(println "Answer 2:" ans2)

(test/run-tests)

