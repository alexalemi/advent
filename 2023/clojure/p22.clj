;; # 🎄 Advent of Code 2023 - Day 22 - Sand Slabs
(ns p22
  (:require [clojure.string :as str]
            [clojure.set :as set]))

(def data-string (slurp "../input/22.txt"))
(def test-string "1,0,1~1,2,1
0,0,2~2,0,2
0,2,3~2,2,3
0,0,4~0,2,4
2,0,5~2,2,5
0,1,6~2,1,6
1,1,8~1,1,9")

(defn ->data [s]
  (zipmap
    (range)
    (for [line (str/split-lines (str/trim s))]
      (apply vector
          (for [part (str/split line #"~")]
              (mapv parse-long (str/split part #",")))))))

(def data (->data data-string))
(def test-data (->data test-string))

(defn all-squares [[[x0 y0 z0] [x1 y1 z1]]]
  (for [x (range x0 (inc x1))
        y (range y0 (inc y1))
        z (range z0 (inc z1))]
    [x y z]))

(defn underneath [[[x0 y0 z0] [x1 y1 _z1]]]
  (for [x (range x0 (inc x1))
        y (range y0 (inc y1))]
    [x y (dec z0)]))

(defn dec-cube [[[x0 y0 z0] [x1 y1 z1]]]
  [[x0 y0 (dec z0)] [x1 y1 (dec z1)]])

(defn drop-down [filled cube]
   (letfn [(supported-by [loc] (or (filled loc) (when (= (last loc) 0) :ground)))]
     (loop [cube cube]
       (let [supports (keep supported-by (underneath cube))]
         (if (empty? supports)
            (recur (dec-cube cube))
            [cube (set supports)])))))


(defn find-supported-by [data]
    (loop [queue (sort-by (comp last first val) data)
           placed {}
           filled {}
           supported-by {}]
      (if-let [[which cube] (first queue)]
        (let [[dropped-cube supports] (drop-down filled cube)]
            (recur
                (next queue)
                (assoc placed which dropped-cube)
                (into filled (zipmap (all-squares dropped-cube) (repeat which)))
                (assoc supported-by which supports)))
        supported-by)))

(defn expendable [supported-by]
    (reduce disj
            (set (keys supported-by))
            (map first (filter (fn [x] (= (count x) 1)) (vals supported-by)))))

(defn part-1 [data]
    (count (expendable (find-supported-by data))))

(assert (= (part-1 test-data) 5))
(defonce ans1 (part-1 data))
(assert (= ans1 391))

;; # Part 2
;; Now we need to cause a chain reaction.

(defn map-vals [m f & args]
    (reduce-kv (fn [m k v] (let [v* (apply f v args)]
                            (if (empty? v*)
                              m
                              (assoc m k v*))))
               {} m))


(defn chain-reaction [supported-by which]
    (loop [supported-by supported-by
           queue #{which}
           removed #{}]
      (if-let [x (first queue)]
        (recur
           (map-vals supported-by disj x)
           (into (disj queue x)
                 (reduce-kv (fn [m k v] (if (= v #{x}) (conj m k) m)) #{} supported-by))
           (conj removed x))
        removed)))

(defn part-2 [data]
  (let [supported-by (find-supported-by data)
        crucial (set/difference
                 (set (keys supported-by))
                 (expendable supported-by))]
    (transduce
      (map (comp dec count (partial chain-reaction supported-by)))
      +
      crucial)))

(assert (= (part-2 test-data) 7))
(defonce ans2 (part-2 data))
(assert (= ans2 69601))


(defn -main []
  (println "Answer 1:" ans1)
  (println "Answer 2:" ans2))
