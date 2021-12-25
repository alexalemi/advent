(ns advent25
  (:require
   [clojure.string :as str]
   [clojure.test :as test]
   [clojure.set :as set]))

(def data-string (slurp "../input/25.txt"))
(def test-string "v...>>.vv>
.vv>>.vv..
>>.>v>...v
>>v>>.>.v.
v>v.vv.v..
>.>>..v...
.vv..>.>v.
v.v..>>v.v
....v..v.>")

(defn enumerate [coll]
  (zipmap (range) coll))

(defn process [s])

(defn process [s]
  (let [occupied (for [[row line] (enumerate (str/split-lines s))
                       [col c] (enumerate line)
                       :when (not= c \.)]
                   [c [row col]])
        height (count (str/split-lines s))
        width (count (first (str/split-lines s)))
        downy (map second (filter #(= (first %) \v) occupied))
        righty (map second (filter #(= (first %) \>) occupied))]
    {:downy (set downy) :righty (set righty) :width width :height height}))

(def data (process data-string))
(def test-data (process test-string))

(defn next-right [occupied width loc]
  (let [[row col] loc
        new-loc [row (mod (inc col) width)]]
    (if (occupied new-loc) loc new-loc)))

(defn next-down [occupied height loc]
  (let [[row col] loc
        new-loc [(mod (inc row) height) col]]
    (if (occupied new-loc) loc new-loc)))

(defn step [data]
  (let [{:keys [righty downy width height]} data
        occupied (into righty downy)
        go-right (partial next-right occupied width)
        new-rights (into #{} (map go-right righty))
        occupied (into new-rights downy)
        go-down (partial next-down occupied height)
        new-downs (into #{} (map go-down downy))]
    (-> data
        (assoc :righty new-rights)
        (assoc :downy new-downs))))

(defn printer [data])

(defn printer [data]
  (println (str/join "\n" (for [row (range (:height data))]
                            (apply str (for [col (range (:width data))]
                                         (cond
                                           ((:righty data) [row col]) \>
                                           ((:downy data) [row col]) \v
                                           :else \.)))))))

(defn part-1 [data]
  (inc (count (take-while (fn [[a b]] (not= a b)) (partition 2 1 (iterate step data))))))

(test/deftest test-part-1
  (test/is (= (part-1 test-data) 58)))

(time (def ans-1 (part-1 data)))
(println)
(println "Answer 1:" ans-1)

(test/run-tests)
