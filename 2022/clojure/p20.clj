;; # 🎄 Advent of Code 2022 - Day 20 -
(ns p20
  (:require [clojure.string :as str]
            [clojure.test :as test]
            [clojure.set :as set]))

;; ## Data

(def data-string (slurp "../input/20.txt"))

(def test-string "1
2
-3
3
-2
0
4")

(defn enumerate [coll]
  (map vector (range) coll))

(defn parse [s]
  (let [data (map parse-long (str/split-lines s))
        m (into {} (enumerate (enumerate data)))]
    {:by-loc m
     :by-val (set/map-invert m)}))

(def test-data (parse test-string))
(def data (parse data-string))

;; ## Logic

(defn move [{:keys [by-loc by-val] :as data} val]
  (let [loc (by-val val)
        [_ x] val
        ;; The start and end are the same location
        new-loc (mod (+ loc x) (dec (count by-val)))
        ;; To get things to align, set 0 to the end.
        new-loc (if (zero? new-loc) (dec (count by-val)) new-loc)
        ;; Different behavior if we're moving left or right
        [start stop op] (if (>= new-loc loc)
                          [(inc loc) (inc new-loc) dec]
                          [new-loc loc inc])]
    (-> data
        (update :by-loc merge
                (into {new-loc val}
                      (for [i (range start stop)]
                        [(op i) (by-loc i)])))
        (update :by-val merge
                (into {val new-loc}
                      (for [i (range start stop)]
                        [(by-loc i) (op i)]))))))

(defn render [data]
  (let [{by-loc :by-loc} data]
    (into []
          (for [i (range (count (:by-loc data)))]
            (second (by-loc i))))))

(defn grove-coordinates [coll]
  (transduce
   (comp
    (map (fn [i] (+ i (.indexOf coll 0))))
    (map (fn [i] (mod i (count coll))))
    (map coll))
   +
   [1000 2000 3000]))

;; ## Part 1

(defn part-1 [data]
  (grove-coordinates (render (reduce move data (enumerate (render data))))))

(test/deftest test-part-1
  (test/is (= 3 (part-1 test-data))))

(defonce ans1 (part-1 data))

;; ## Part 2

(def KEY 811589153)

(defn part-2 [data]
  (let [{by-loc :by-loc} data
        by-loc (update-vals by-loc (fn [[i x]] [i (* x KEY)]))
        data {:by-loc by-loc :by-val (set/map-invert by-loc)}
        mixing-order (enumerate (render data))]
    (loop [data data counter 10]
      (println "On step=" counter)
      (if (pos? counter)
        (recur (reduce move data mixing-order) (dec counter))
        (grove-coordinates (render data))))))

(test/deftest test-part-2
  (test/is (= 1623178306 (part-2 test-data))))

(defonce ans2 (part-2 data))

;; ## Main

(defn -test [& _]
  (test/run-tests 'p20))

(defn -main [& _]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))

