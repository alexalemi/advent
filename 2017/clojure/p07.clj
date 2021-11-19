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
       [name val children]))

(defn make-child-parent
  [data]
  (transduce
    (comp (map process-line)
          (filter #(get % 2))
          (mapcat #(let [[parent _ children] %] (for [child children] [child parent]))))
    conj {} data))

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


(test/run-tests)

(println)
(println "Answer1:", ans1)
; (println "Answer2:", ans2)


