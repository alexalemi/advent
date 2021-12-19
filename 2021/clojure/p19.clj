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

(defn process
 "Reads the input into a list of lists of vectors"
 [s]
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

(defn transforms
 "All 24 transformations of a vector."
 [vec]
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

(comment
  "For the first part of this puzzle, we are going to look for pairs of sensors
  that are overlapping. We know that each pair has at least 12 points in common, for those
  12 points there will be 66 line segments. So, for each sensor we'll make a list of all
  of the line segments they see (their distances which are rotationally and mirror invariant)
  and then look for how many of these line segment distances they have in common, if there 
  are at least 66, we know they are overlapping.")

(defn overlapping-pairs
  "Measure the overlap of two sorted lists of triplets, [distance p1 p2]
  Returns both the number of segments they have in common as well
  as an example pair of segments with the same length."
  [x y]
  (loop [x x
         y y
         overlaps 0  ; how many overlaps there are.
         a-pair nil] ; an example pair they have in common.
    (let [[a x1 y1] (first x)
          [b x2 y2] (first y)]
      (cond
        (or (empty? x) (empty? y)) [overlaps a-pair]
        (= a b) (recur (rest x) (rest y) (inc overlaps) [[x1 y1] [x2 y2]])
        (< a b) (recur (rest x) y overlaps a-pair)
        :else   (recur x (rest y) overlaps a-pair)))))

(defn sensor-distances
  "Given a list of beacon locations, return a list
  of all line segments, [distance pt1 pt2]." [beacons]
  (sort-by first
           (map (fn [[a b]] [(squared-length a b) a b])
                (combo/combinations beacons 2))))

(defn map-vals [f m]
  (reduce-kv (fn [m k v] (assoc m k (f v))) {} m))

(defn filter-vals [f m]
  (reduce-kv (fn [m k v] (if (f v) (assoc m k v) m)) {} m))

(def OVERLAPS 66) ; 12 choose 2

(defn find-overlaps
  "Look for at least 66 share line segment distances.

  This will return a map with 
    [i j] : [n [[a b] [c d]]]
  type keys.  This indicates that sensor i and j overlap
  with n segments in common, one such example line segment is
  a-b and c-d.  

  Since these overlap we now know that either a->c or a->d."
  [data]
  (let [sensors (enumerate data)
        sensor-ds (map-vals sensor-distances sensors)
        overlaps (into {} (for [[i ds1] sensor-ds
                                [j ds2] sensor-ds
                                :when (< i j)]
                            [[i j] (overlapping-pairs ds1 ds2)]))]
    (filter-vals #(>= (first %) OVERLAPS) overlaps)))

(comment
  "After we are able to find pairs of sensors that do overlap and an example line segment
  that has to map from each, we should be able to determine the exact aligning transformation
  quickly.
  
  Given that we know the segment a-b has to match c-d we know that either a->c or a->d.
  That give us a useful offset vector, for the rotations we'll just sort of try them all 
  and bail early if there isn't a match.")

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
  "Create a dictionary of the inverse rotations for each of the rotations."
  (into
   {}
   (for [i (range 24)]
     [i (first (filter #(= [1 2 3] ((rot %) ((rot i) [1 2 3]))) (range 24)))])))

(defn inverse-transform
  "Given a transform, return its inverse."
  [t]
  (let [{:keys [rot-id offset]} t]
    (comp (rot (inverse-rot rot-id)) (sub-vec offset))))

(defn test-alignment
  "Test whether the transform t will map the y list of beacons
  onto the x list of beacons." [x y t]
  (>= (count
       (set/intersection
        (set (sort x))
        (set (sort (map (transform t) y)))))
      12))

(defn find-alignment
  "Find the proper aligning transformation between 
  sensor x and y with their overlap data."
  [data [[x y] overlap]]
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

(defn filter-keys [f m]
  (reduce-kv
   (fn [m k v] (if (f k) (assoc m k v) m))
   {} m))

(defn augment-alignments [alignments]
  (merge (map-vals transform alignments)
         (reduce-kv
          (fn [m k v] (assoc m (apply vector (reverse k)) (inverse-transform v)))
          {} alignments)))

(comment "Now that we know the alignments between all of the pieces,
  something like 
    {[1 4] {:rot-id 7 :offset [7 5 10]}
     [2 4] {:rot-id 1 :offset [-12 -23 20]}}
 
 we need to stitch this whole thing together in a single map.
 
 We'll do a sort of depth first flooding of the whole thing.")

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




