(ns advent11
  (:require
   [clojure.test :as test]
   [clojure.string :as str]))

(def test-string "5483143223
2745854711
5264556173
6141336146
6357385478
4167524645
2176841721
6882881134
4846848554
5283751526")
(def data-string (slurp "../input/11.txt"))

(defn enumerate [coll]
  (zipmap (range) coll))

(defn process [s]
  (into {:flashes 0}
        (for [[row line] (enumerate (str/split-lines s))
              [col s] (enumerate line)]
          [[row col] (read-string (str s))])))

(def data (process data-string))
(def test-data (process test-string))

(defn map-val [f m]
  (reduce-kv (fn [m k v] (assoc m k (f v))) {} m))

(defn neighbors [loc]
  (let [[x y] loc]
    [[(inc x) y]
     [(dec x) y]
     [(inc x) (inc y)]
     [(inc x) (dec y)]
     [(dec x) (inc y)]
     [(dec x) (dec y)]
     [x (inc y)]
     [x (dec y)]]))

(defn set-to-zero [data flashed]
  (if (empty? flashed) data
      (recur (update (assoc data (first flashed) 0) :flashes inc)
             (rest flashed))))

(defn step [data]
  (loop [data data flashed #{} queue (remove keyword? (keys data))]
    (if-let [loc (first queue)]
      (let [new-x (inc (data loc))
            flash (and (> new-x 9) (not (contains? flashed loc)))]
        (recur
         (update data loc inc)
         (if flash (conj flashed loc) flashed)
         (if flash
           (concat (rest queue)
                   (filter data (remove flashed (neighbors loc))))
           (rest queue))))
      (set-to-zero data flashed))))

(defn printer [data]
  (println)
  (println (let [locs (remove keyword? (keys data))
                 max-x (reduce max (map first locs))
                 max-y (reduce max (map second locs))]
             (str/join "\n" (for [x (range (inc max-x))]
                              (apply str (for [y (range (inc max-y))]
                                           (data [x y])))))))
  (println " :flashes: " (:flashes data)))

(defn part-1 [data]
  (:flashes (nth (iterate step data) 100)))

(test/deftest test-part-1
  (test/is (= (part-1 test-data) 1656))
  (test/is (= (part-1 data) 1705)))

(time (def ans1 (part-1 data)))
(println)
(println "Answer 1:" ans1)

(defn modified-step [data]
  (let [data (step data)
        flashes (:flashes data)]
    (if (= flashes 100) nil
        (assoc data :flashes 0))))

(defn part-2 [data]
  (count (take-while some? (iterate modified-step data))))

(test/deftest test-part-2
  (test/is (= (part-2 test-data) 195))
  (test/is (= (part-2 data) 265)))

(time (def ans2 (part-2 data)))
(println "Answer 2:" ans2)

(test/run-tests)
