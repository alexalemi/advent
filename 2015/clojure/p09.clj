(ns p09
 (:require [clojure.string :as str]))

(defonce data-string (slurp "../input/09.txt"))

(defn parse-line [s]
  (let [[_ frm to cost] (re-matches #"(\w+) to (\w+) = (\d+)" s)]
    {:frm frm :to to :cost (read-string cost)}))

(def test-string "London to Dublin = 464
London to Belfast = 518
Dublin to Belfast = 141")

(defonce test-data (map parse-line (str/split-lines test-string)))
(defonce data (map parse-line (str/split-lines data-string)))

(defn process [data]
  {:cities (into (into #{} (map :frm data)) (map :to data))
   :nodes (reduce (fn [m x] (-> m (assoc (list (:frm x) (:to x)) (:cost x))
                               (assoc (list (:to x) (:frm x)) (:cost x)))) {} data)})

(def test-info (process test-data))
(def info (process data))

(defn cost [costs path]
  (reduce + (map costs (partition 2 1 path))))

(defn permutations [s]
  (lazy-seq
   (if (seq (rest s))
     (apply concat (for [x s] (map #(cons x %) (permutations (remove #{x} s)))))
     [s])))


(defn shortest-path [data]
  (apply min (map (partial cost (:nodes data)) (permutations (:cities data)))))


(defonce ans1 (shortest-path info))
(println "Answer1:" ans1)

(defn longest-path [data]
  (apply max (map (partial cost (:nodes data)) (permutations (:cities data)))))

(defonce ans2 (longest-path info))
(println "Answer2:" ans2)

(comment
  (shortest-path test-data)
  (map (partial cost (:nodes data)) (permutations (:cities data)))

  (apply min (map (partial cost (:nodes test-info)) (permutations (:cities test-info))))

  (into (into #{} (map :frm test-data)) (map :to test-data))
  (into (into #{} (map :frm data)) (map :to data)))
