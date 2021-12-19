(ns advent19
  (:require
   [clojure.test :as test]
   [clojure.core.matrix :as matrix]
   [clojure.set :as set]
   [clojure.string :as str]
   [clojure.math.combinatorics :as combo]))

(def data-string (slurp "../input/19.txt"))
(def test-string (slurp "../input/19test.txt"))

(defn read-vector [s]
  (read-string (str "[" s "]")))

(defn process [s]
  (let [scanners (rest (map str/trim (str/split s #"--- scanner \d+ ---")))]
    (into [] (for [scanner scanners]
               (mapv read-vector (str/split-lines scanner))))))

(def data (process data-string))
(def test-data (process test-string))

(defn enumerate [coll]
  (zipmap (range) coll))

(defn vec-add [a b] (mapv + a b))
(defn vec-sub [a b] (mapv - a b))

(defn sq [x] (* x x))

(defn squared-length
  "length of a line between two points."
  [a b]
  (let [[x1 y1 z1] a
        [x2 y2 z2] b]
    (+ (sq (- x1 x2))
       (sq (- y1 y2))
       (sq (- z1 z2)))))

(defn transforms [vec]
  (let [[x y z] vec]
    [[x     y     z]
     [x  (- y) (- z)]
     [(- x)    y  (- z)]
     [(- x) (- y)    z]
     [y     z     x]
     [y  (- z) (- x)]
     [(- y)    z  (- x)]
     [(- y) (- z)    x]
     [z     x     y]
     [z  (- x) (- y)]
     [(- z)    x  (- y)]
     [(- z) (- x)    y]
     [(- x)    z     y]
     [x  (- z)    y]
     [x     z  (- y)]
     [(- x) (- z) (- y)]
     [(- z)    y     x]
     [z  (- y)    x]
     [z     y  (- x)]
     [(- z) (- y) (- x)]
     [(- y)    x     z]
     [y  (- x)    z]
     [y     x  (- z)]
     [(- y) (- x) (- z)]]))

(defn volume-of-tetrahedron [vecs]
  (matrix/det (matrix/transpose (conj (matrix/transpose (apply vector vecs)) [1 1 1 1]))))

(defn overlapping-pairs
  "Measure the overlap of two sorted lists of triplets, [distance p1 p2]"
  [x y]
  (loop [x x
         y y
         overlaps 0
         a-pair nil]
    (let [[a x1 y1] (first x)
          [b x2 y2] (first y)]
      (cond
        (or (empty? x) (empty? y)) [overlaps a-pair]
        (= a b) (recur (rest x) (rest y) (inc overlaps) [[x1 y1] [x2 y2]])
        (< a b) (recur (rest x) y overlaps a-pair)
        :else   (recur x (rest y) overlaps a-pair)))))

(defn overlap
  "Measure the overlap of two sorted lists"
  [x y]
  (loop [x x y y n 0]
    (let [a (first x)
          b (first y)]
      (cond
        (or (empty? x) (empty? y)) n
        (= a b) (recur (rest x) (rest y) (inc n))
        (< a b) (recur (rest x) y n)
        :else   (recur x (rest y) n)))))

(defn sensor-distances [beacons]
  (sort-by first
           (map (fn [[a b]] [(squared-length a b) a b])
                (combo/combinations beacons 2))))

(defn map-vals [f m]
  (reduce-kv (fn [m k v] (assoc m k (f v))) {} m))

(defn filter-vals [f m]
  (reduce-kv (fn [m k v] (if (f v) (assoc m k v) m)) {} m))

(def OVERLAPS 66) ; 12 choose 2

(defn find-overlaps
  "look for at least 66 share line segment distances."
  [data]
  (let [sensors (enumerate data)
        sensor-ds (map-vals sensor-distances sensors)
        overlaps (into {} (for [[i ds1] sensor-ds
                                [j ds2] sensor-ds
                                :when (< i j)]
                            [[i j] (overlapping-pairs ds1 ds2)]))]
    (filter-vals #(>= (first %) OVERLAPS) overlaps)))

(defn rot [i]
  (fn [x] ((transforms x) i)))

(defn add-vec [x]
  (fn [y] (mapv + y x)))

(defn sub-vec [x]
  (fn [y] (mapv - y x)))

(defn transform [t]
  (let [{:keys [rot-id offset]} t]
    (comp (add-vec offset) (rot rot-id))))

(def inverse-rot
  (into
   {}
   (for [i (range 24)]
     [i (first (filter #(= [1 2 3] ((rot %) ((rot i) [1 2 3]))) (range 24)))])))

(defn inverse-transform [t]
  (let [{:keys [rot-id offset]} t]
    (comp (rot (inverse-rot rot-id)) (sub-vec offset))))

(let [t {:rot-id 7 :offset [5 11 -10]}]
  ((inverse-transform t) ((transform t) [1 2 3])))

(defn test-alignment [x y t]
  (>= (count
       (set/intersection
        (set (sort x))
        (set (sort (map (transform t) y)))))
      12))

(defn find-alignment [data [[x y] overlap]]
  (let [sensors (enumerate data)
        [_ [[a _] [c d]]] overlap]
    (first (filter (partial test-alignment (sensors x) (sensors y))
                   (for [i (range 24)
                         off [(mapv - a ((rot i) c)) (mapv - a ((rot i) d))]]
                     {:rot-id i :offset off})))))

(defn find-alignments [data]
  (let [overlaps (find-overlaps data)]
    (reduce-kv
     (fn [m k v]
       (assoc m k (find-alignment data [k v])))
     {} overlaps)))

(def test-alignments (find-alignments test-data))
(def alignments (find-alignments data))

(defn filter-keys [f m]
  (reduce-kv
   (fn [m k v] (if (f k) (assoc m k v) m))
   {} m))

(defn augment-alignments [alignments]
  (merge (map-vals transform alignments)
         (reduce-kv
          (fn [m k v] (assoc m (apply vector (reverse k)) (inverse-transform v)))
          {} alignments)))

(defn stitch [data]
  (let [alignments (find-alignments data)
        alignments (augment-alignments alignments)
        start-id 0]
    (loop [global (set (data start-id))
           seen #{start-id}
           transforms {start-id identity}
           todo (filter-keys (comp #{start-id} first) alignments)]
      (let [[[a b] t] (first todo)]
        (cond
          (empty? todo) {:global global :origins (for [i (range (count data))]
                                                   ((transforms i) [0 0 0]))}

          (contains? seen b) (recur
                              global
                              seen
                              transforms
                              (rest todo))

          :else (let [t-prime (comp (transforms a) t)]
                  (recur
                   (into global (map t-prime (data b)))
                   (conj seen b)
                   (assoc transforms b t-prime)
                   (into (rest todo) (filter-keys (fn [[left _]] (= left b)) alignments)))))))))

(defn part-1 [data]
  (count (:global (stitch data))))

(test/deftest test-part-1
  (test/is (= (part-1 test-data) 79))
  (test/is (= (part-1 data) 483)))

(time (def ans1 (part-1 data)))
(println)
(println "Answer 1:" ans1)

(defn abs [x] (max x (- x)))

(defn manhattan [[a b]]
  (let [[x1 y1 z1] a
        [x2 y2 z2] b]
    (+ (abs (- x1 x2)) (abs (- y1 y2)) (abs (- z1 z2)))))

(defn part-2 [data]
  (let [origins (:origins (stitch data))]
    (reduce max (map manhattan (combo/combinations origins 2)))))

(test/deftest test-part-2
  (test/is (= (part-2 test-data) 3621))
  (test/is (= (part-2 data) 14804)))

(time (def ans2 (part-2 data)))
(println)
(println "Answer 2:" ans2)

(test/run-tests)




