(ns advent15
  (:require
   [clojure.test :as test]
   [clojure.string :as str]
   [clojure.data.priority-map :refer [priority-map]]))

(def data-string (slurp "../input/15.txt"))
(def test-string "1163751742
1381373672
2136511328
3694931569
7463417111
1319128137
1359912421
3125421639
1293138521
2311944581")

(defn enumerate [coll]
  (zipmap (range) coll))

(defn process
  "Read in the board string and output a collection:
       {:costs :: loc -> cost
        :goal :start}"
  [s]
  (let [costs (into {} (for [[row line] (enumerate (str/split-lines s))
                             [col s] (enumerate line)]
                         [[col row] (read-string (str s))]))
        width (dec (count (first (str/split-lines s))))
        height (dec (count (str/split-lines s)))]
    {:costs costs :goal [width height] :start [0 0]}))

(def data (process data-string))
(def test-data (process test-string))

(defn neighbors
  "Return up down left and right of a loc."
  [loc]
  (let [[x y] loc]
    [[(inc x) y]
     [(dec x) y]
     [x (inc y)]
     [x (dec y)]]))

;; First implementation general A-star algorithm

(defn abs [x] (max x (- x)))

(defn manhattan [end pos]
  (let [[end-x end-y] end
        [pos-x pos-y] pos]
    (+ (abs (- end-x pos-x))
       (abs (- end-y pos-y)))))

(defn reconstruct-path
  "Knowing the parent of each node, construct the full path."
  [came-from current]
  (loop [current current
         path (list current)]
    (let [prev (came-from current)]
      (if prev
        (recur prev (conj path prev))
        path))))

(defn a-star
  "General A-star algorithm."
  [costs goal start h]
  (loop [frontier (priority-map start (h start))
         came-from {}
         best-score {start 0}
         prev nil
         neighs '()]
    (let [[current _] (first frontier)
          score (best-score prev)
          neigh (first neighs)]
      (cond
        (and (empty? frontier) (empty? neighs)) :failure
        (= current goal) (reconstruct-path came-from current)
        (empty? neighs)
        (recur
         (dissoc frontier current)
         came-from
         best-score
         current
         (filter costs (neighbors current)))
        :else
        (let [neigh-cost (costs neigh)
              tentative-score (+ neigh-cost score)
              prev-score (get best-score neigh ##Inf)
              f-score (+ tentative-score (h neigh))]
          (if (< tentative-score prev-score)
            (recur
             (assoc frontier neigh f-score)
             (assoc came-from neigh prev)
             (assoc best-score neigh tentative-score)
             prev
             (rest neighs))
            (recur
             frontier
             came-from
             best-score
             prev
             (rest neighs))))))))

(defn shortest-cost-old [data]
  (let [{:keys [costs goal start]} data
        h (partial manhattan goal)]
    (reduce + (map costs (rest (a-star costs goal start h))))))

;; Second implementation: simple dijkstra

(defn neighbor-costs
  "Returns a map of all neighbors to their travel costs."
  [costs loc]
  (let [ns (filter costs (neighbors loc))
        cs (map costs ns)]
   (zipmap ns cs)))

(defn map-val
  "Map a function over the vals of a map."
  [m f & args]
  (reduce-kv (fn [m k v] (assoc m k (apply f v args))) {} m))

(defn remove-keys
  "Remove some of the keys in a map."
  [pred m]
  (reduce-kv (fn [m k v] (if (pred k) m (assoc m k v))) {} m))

(defn shortest-cost
  "Recursive dijstra's algorithm."
  [data]
  (let [{:keys [costs start goal]} data
        f (partial neighbor-costs costs)]
    (loop [frontier (priority-map start 0)  ; the nodes we have to expand
           min-cost {start 0}]  ; the nodes we already know the optimal cost of.
     (let [[current cost] (peek frontier)
           ; for all of the neighbors of the current node, see if we've just
           ; discovered a shorter path.
           dists (map-val (remove-keys min-cost (f current)) + cost)]
       (if (= current goal) cost
         (recur (merge-with min (pop frontier) dists)
                (assoc min-cost current cost)))))))

(test/deftest test-part-1
  (test/is (= (shortest-cost test-data) 40))
  (test/is (= (shortest-cost data) 714)))

(time (def ans1 (shortest-cost-old data)))
(time (def ans1 (shortest-cost data)))
(println)
(println "Answer 1:" ans1)

(defn vec-add
  "vector addition"
  [a b] (mapv + a b))

(defn wrap-add
  "Adds n to x but 9 wraps around to 1."
  [x n]
  (cond
    (= n 0) x
    (= x 9) (recur 1 (dec n))
    :else (recur (inc x) (dec n))))

(defn expand-map 
  "Expand the map 5x in both directions."
  [data]
  (let [{:keys [costs goal]} data
        [width height] (map inc goal)]
    (-> data
        (assoc :goal [(dec (* 5 width)) (dec (* 5 height))])
        (assoc :costs (apply merge (for [i (range 5)
                                         j (range 5)]
                                     (let [origin [(* i width) (* j height)]
                                           offset (+ i j)]
                                       (into {} (for [[loc cost] costs]
                                                  [(vec-add loc origin) (wrap-add cost offset)])))))))))

(test/deftest test-part-2
  (test/is (= (shortest-cost (expand-map test-data)) 315))
  (test/is (= (shortest-cost (expand-map data)) 2948)))

(time (def ans2 (shortest-cost (expand-map data))))
(println)
(println "Answer 2:" ans2)

(test/run-tests)
