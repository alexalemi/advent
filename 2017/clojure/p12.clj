(ns advent12
  (:require
   [clojure.edn :as edn]
   [clojure.test :as test]
   [clojure.set :as set]
   [clojure.string :as str]))

(def test-string "0 <-> 2
1 <-> 1
2 <-> 0, 3, 4
3 <-> 2, 4
4 <-> 2, 3, 6
5 <-> 6
6 <-> 4, 5")
(def data-string (slurp "../input/12.txt"))

(defn read-set [s]
  (edn/read-string (str "#{" s "}")))
(def pattern #"(\d+) <-> ((\d+(, )?)*)")

(defn process-line [s]
  (let [[_ node children & _] (re-matches pattern s)]
    [(edn/read-string node) (read-set children)]))
(defn process [s]
  (into {} (map process-line (str/split-lines s))))

(def data (process data-string))
(def test-data (process test-string))

(defn cluster
  "Find the size of a cluster."
  ([data node] (cluster data #{} #{node}))
  ([data seen frontier]
   (if-let [node (first frontier)]
     (recur data
            (conj seen node)
            (-> frontier
                (disj node)
                (set/union (set/difference (data node) seen))))
     seen)))

(defn part-1 [data] (count (cluster data 0)))

(test/deftest test-part-1
  (test/is (= (part-1 test-data) 6))
  (test/is (= (part-1 data) 239)))

(time (def ans1 (part-1 data)))
(println)
(println "Answer 1:" ans1)

(defn eat-clusters
  [data] (reduce dissoc data (cluster data (key (first data)))))

(defn part-2 [data]
  (count (take-while not-empty (iterate eat-clusters data))))

(test/deftest test-part-2
  (test/is (= (part-2 test-data) 2))
  (test/is (= (part-2 data) 215)))

(time (def ans2 (part-2 data)))
(println)
(println "Answer 2:" ans2)

(test/run-tests)
