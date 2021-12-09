(ns advent09
  (:require
   [clojure.string :as str]
   [clojure.test :as test]))

(def test-string "2199943210
3987894921
9856789892
8767896789
9899965678")
(def data-string (slurp "../input/09.txt"))

(defn neighbors [loc]
  (let [[x y] loc]
    [[(inc x) y]
     [(dec x) y]
     [x (inc y)]
     [x (dec y)]]))

(defn process [s]
  (into {} (for [[row line] (map-indexed vector (str/split-lines s))
                 [col val] (map-indexed vector line)]
             [[row col] (read-string (str val))])))

(def data (process data-string))
(def test-data (process test-string))

(defn local-minimum [data loc]
  (let [val (get data loc)]
    (every? #(> % val) (filter some? (map data (neighbors loc))))))

(defn minima [data]
  (let [test (partial local-minimum data)]
    (filter (fn [[k v]] (if (test k) v nil)) data)))

(defn part-1 [data]
  (reduce + (map #(inc (val %)) (minima data))))

(test/deftest test-part-1
  (test/is (= (part-1 test-data) 15))
  (test/is (= (part-1 data) 603)))

(time (def ans1 (part-1 data)))
(println)
(println "Answer 1:" ans1)

(defn basin [data seed]
  (loop [queue #{seed}
         seen #{}]
    (if-let [loc (first queue)]
      (let [height (get data loc)]
        (letfn [(valid [height] (fn [loc] (let [x (get data loc)] (and (< x 9) (> x height)))))]
          (recur
           (into (rest queue) (remove seen (filter (valid height) (filter data (neighbors loc)))))
           (conj seen loc))))
      seen)))

(defn part-2 [data]
  (let [mins (minima data)]
    (reduce * (take-last 3 (sort (for [min mins] (count (basin data (key min)))))))))

(test/deftest test-part-2
  (test/is (= (part-2 test-data) 1134))
  (test/is (= (part-2 data) 786780)))

(time (def ans2 (part-2 data)))
(println)
(println "Answer 2:" ans2)

(test/run-tests)

