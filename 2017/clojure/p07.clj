(ns advent07
  (:require
   [clojure.edn :as edn]
   [clojure.string :as string]
   [clojure.test :as test]))

(def data (string/split-lines (slurp "../input/07.txt")))

(defn process-line
  "name (number) -> child, child2"
  [s]
  (let [[header footer](string/split s #" -> ")
        [_ name weight] (re-find #"(\w+) \((\d+)\)" header)
        val (edn/read-string weight)
        children (if footer (string/split footer #", ") nil)]
       {:name name :weight val :children children}))

(defn make-child-parent
  [data]
  (transduce
    (comp (map process-line)
          (filter :children)
          (mapcat #(for [child (:children %)] [child (:name %)])))
    conj {} data))


(def processed-data (map process-line data))
(def child-parent (make-child-parent data))

(defn find-root
  ([child-parent] (find-root child-parent (first (first child-parent))))
  ([child-parent child]
   (if-let [parent (get child-parent child)]
     (recur child-parent parent)
     child)))

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


(test/deftest test-part-1
  (test/is (= "tknk"
              (find-root
               (make-child-parent
                (string/split-lines test-string))))))

(time (def ans1 (find-root (make-child-parent data))))

(defn make-full-data
  [data]
  (merge-with into
             (into {} (map (fn [x] [(:name x) x]) (map process-line data)))
             (into {} (map (fn [x] (let [[child parent] x] [child {:parent parent}])) (make-child-parent data)))))

(def full-data (make-full-data data))
(def full-test-data (make-full-data (string/split-lines test-string)))

(defn get-weight-old
  ([full-data node] (get-weight full-data #{node} 0))
  ([full-data queue total]
   (if (empty? queue) total
       (let [which (first queue)
             new-queue (rest queue)
             node (full-data which)]
          (recur full-data
                 (into new-queue (:children node))
                 (+ total (:weight node)))))))


(defn get-weight
  [full-data which]
  (let [node (full-data which)]
    (if-let [children (:children node)]
      (+ (:weight node) (reduce + (map #(get-weight full-data %) children)))
      (:weight node))))

(def memoized-get-weight (memoize get-weight))

(defn add-total-weights
  [full-data]
  (merge-with into full-data
             (into {} (map (fn [which] [which {:total-weight (memoized-get-weight full-data which)}]) (keys full-data)))))

(def full2-test-data (add-total-weights full-test-data))

(defn find-broken-node
  [full-data]
  (first (filter (fn [node] (not= 1 (count (into #{} (map (fn [which] (:total-weight (full-data which))) (:children node)))))) (filter :children (vals full-data)))))

(defn child-weights
  [full-data node]
  (into {} (map (fn [which] [which (:total-weight (full-data which))]) (:children node))))


(defn odd-one-out
  [data which]
  (= 1 (count (disj (into #{} (vals (dissoc (child-weights data (data (:parent (data which)))) which))) (:total-weight (data which))))))


(defn new-weight
  [data which]
  (- (:weight (data which))
    (- (:total-weight (data which)) (first (disj (into #{} (vals (child-weights data (data (:parent (data which)))))) (:total-weight (data which)))))))

(defn find-bad-key
  [data]
  (first (filter #(odd-one-out data %) (map :name (filter :children (vals data))))))


(defn fixing-weight
  [data]
  (new-weight data (find-bad-key data)))


(test/deftest test-part-2
  (test/is (= 60
              (fixing-weight (add-total-weights full-test-data)))))


(- (:weight (foo "apjxafk")) 8)

(time (def ans2 (fixing-weight (add-total-weights full-data))))

(comment TODO "I really need to fix this up, its broken right now!")

(test/run-tests)

(println)
(println "Answer1:", ans1)
(println "Answer2:", ans2)


