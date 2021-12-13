(ns advent13
  (:require
   [clojure.test :as test]
   [clojure.string :as str]))

(def data-string (slurp "../input/13.txt"))
(def test-string "6,10
0,14
9,10
0,3
10,4
4,11
6,0
6,12
4,1
0,13
10,12
3,4
3,0
8,4
1,10
2,14
8,10
9,0

fold along y=7
fold along x=5")

; (def process [s])

(defn read-vector [s]
  (read-string (str "[" s "]")))

(defn read-fold [s]
  (let [[_ axis num] (re-matches #"fold along (x|y)=(\d+)" s)]
    {:axis (keyword axis) :which (read-string num)}))

(defn process [data-string]
  (let [[marks folds] (str/split data-string #"\n\n")
        marks (str/split-lines marks)
        folds (str/split-lines folds)
        marks (map read-vector marks)
        folds (map read-fold folds)]
    {:marks (set marks) :folds folds}))

(def data (process data-string))
(def test-data (process test-string))

(defn draw [data]
  (let [{marks :marks} data
        max-x (reduce max (map first marks))
        max-y (reduce max (map second marks))]
    (println)
    (println (str/join "\n" (for [y (range (inc max-y))]
                              (apply str (for [x (range (inc max-x))]
                                           (if (marks [x y]) "██" "  "))))))))

(defn fold [fold-data mark]
  (let [{:keys [axis which]} fold-data
        [x y] mark]
    (if (= axis :y)
      [x (if (> y which) (- which (- y which)) y)]
      [(if (> x which) (- which (- x which)) x) y])))


(defn do-fold [data]
  (let [{:keys [marks folds]} data
        f (first folds)]
    (-> data
        (assoc :marks (set (map (partial fold f) marks)))
        (assoc :folds (rest folds)))))

(defn part-1 [data]
  (count (:marks (do-fold data))))

(test/deftest test-part-1
  (test/is (= (part-1 test-data) 17))
  (test/is (= (part-1 data) 687)))

(time (def ans1 (count (:marks (do-fold data)))))
(println)
(println "Answer 1:" ans1)

(def folded (nth (iterate do-fold data) (count (:folds data))))

(println "Answer2:")
(draw folded)

(test/run-tests)

