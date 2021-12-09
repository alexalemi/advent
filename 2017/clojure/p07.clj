(ns advent07
  (:require
   [clojure.string :as string]
   [clojure.test :as test]))

(defn process-line
  "name (number) -> child, child2"
  [s]
  (let [[header footer] (string/split s #" -> ")
        [_ name weight] (re-find #"(\w+) \((\d+)\)" header)
        val (read-string weight)
        children (if footer (string/split footer #", ") nil)]
    {:name name :weight val :children children}))

(defn extend-data
  "Take the list of maps and make it a dictionary"
  [data]
  (merge-with merge
              (zipmap (map :name data) data)
              (into {} (mapcat #(for [child (:children %)] [child {:parent (:name %)}]) (filter :children data)))))

(def data (extend-data (map process-line (string/split-lines (slurp "../input/07.txt")))))

(defn find-root
  ([data] (find-root data (first (first data))))
  ([data which]
   (if-let [parent (:parent (data which))]
     (recur data parent) which)))

(def test-string "pbga (66)
xhth (57)
ebii (61)
havc (66)
ktlj (57)
fwft (72) -> ktlj, cntj, xhth
qoyq (66)
padx (45) -> pbga, havc, qoyq
tknk (41) -> ugml, padx, fwft
jptl (61)
ugml (68) -> gyxo, ebii, jptl
gyxo (61)
cntj (57)")

(def test-data (extend-data (map process-line (string/split-lines test-string))))

(test/deftest test-part-1
  (test/is (= "tknk" (find-root test-data))))

(time (def ans1 (find-root data)))
(println)
(println "Answer1:", ans1)

(defn get-weight
  [data which]
  (let [node (data which)]
    (if-let [children (:children node)]
      (+ (:weight node) (reduce + (map #(get-weight data %) children)))
      (:weight node))))

(def mget-weight (memoize get-weight))

(defn add-total-weights
  [data]
  (merge-with into data
              (into {} (map (fn [which] [which {:total-weight (mget-weight data which)}]) (keys data)))))

(def weighted-test-data (add-total-weights test-data))
(def weighted-data (add-total-weights data))

(defn child-weights
  [data node]
  (map (partial mget-weight data) (:children node)))

(defn child-weight-map
  [data node]
  (zipmap (:children node)
          (child-weights data node)))

(defn only-one?
  [nums]
  (= 1 (count (into #{} nums))))

(defn balanced-children?
  [data node]
  (only-one? (child-weights data node)))

(defn odd-one-out
  [m]
  (some (fn [[k _]] (when (only-one? (vals (dissoc m k))) k)) m))

(defn find-problem-node
  ([data] (find-problem-node data (data (find-root data))))
  ([data node]
   (if (balanced-children? data node)
     node
     (recur data (data (odd-one-out (child-weight-map data node)))))))

(defn new-weight
  [data node]
  (- (:weight node) 
     (- (:total-weight node) 
        (first (vals (dissoc (child-weight-map 
                               data (data (:parent node))) (:name node)))))))

(defn part-2
  [data]
  (new-weight data (find-problem-node data)))

(test/deftest test-part-2
  (test/is (= 60 (part-2 weighted-test-data))))

(time (def ans2 (part-2 (add-total-weights data))))
(println)
(println "Answer2:", ans2)

(test/run-tests)



