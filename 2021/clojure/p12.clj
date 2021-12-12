(ns advent12
  (:require
   [clojure.test :as test]
   [clojure.string :as str]))

(def data-string (slurp "../input/12.txt"))
(def test-strings ["start-A
start-b
A-c
A-b
b-d
A-end
b-end" "dc-end
HN-start
start-kj
dc-start
dc-HN
LN-dc
HN-end
kj-sa
kj-HN
kj-dc" "fs-end
he-DX
fs-he
start-DX
pj-DX
end-zg
zg-sl
zg-pj
pj-he
RW-he
fs-DX
pj-RW
zg-RW
start-pj
he-WI
zg-he
pj-fs
start-RW"])

(defn process-line [line]
  (str/split line #"-"))

(defn process
  "Turn the string into a map, each node to the set of its children"
  [data-string]
  (let [lines (str/split-lines data-string)
        pairs (concat
               (map process-line lines)
               ; reverse all of the inputs to symmetrize
               (map (comp vec reverse process-line) lines))]
    (reduce (fn [acc [k v]] (update acc k (fnil conj #{}) v))
            {} pairs)))

(def data (process data-string))
(def test-data (mapv process test-strings))

(defn lower-case?
  "Is a string all lowercase?"
  [s] (= s (str/lower-case s)))

(def lower-case-filter
  (fn [x] (set (filter lower-case? x))))

(defn paths-from-filter
  "Given a filtering function for the invalid nodes to revisit in your own path,
  construct all paths

  paths-from-filter: current path -> nodes you shouldn't revisit"
  [visited-filter]
  (fn [data]
   (loop [partials #{'("start")} paths #{}]
     (let [x (first partials)
           last (first x)
           visited (visited-filter x)
           next-step (remove visited (get data last #{}))]
      (cond
        ; If we are out of partial paths, we're done, return
        (empty? partials) paths
        ; if we hit a partial path that ends in end, its a full path
        (= "end" last) (recur (rest partials) (conj paths x))
        ; If there are no valid next steps, drop the partial path
        (empty? next-step) (recur (rest partials) paths)
        ; otherwise, add a new partial for all possible valid children
        :else (recur
               (into (rest partials) (map (partial conj x) next-step))
               paths))))))

(def paths (paths-from-filter lower-case-filter))


(test/deftest test-part-1
  (test/are [x y] (= (count (paths x)) y)
    (get test-data 0) 10
    (get test-data 1) 19
    (get test-data 2) 226
    data 3495))

(time (def ans1 (count (paths data))))
(println)
(println "Answer 1:" ans1)


(defn visited-lower-twice? [path]
  (> (reduce max 0 (map val (frequencies (filter lower-case? path)))) 1))

(defn locs-to-remove
  "Filtering function for the second part.

  You can revisit anything but 'start' unless we've visited a lower case
  node twice already, at which point we can't revisit any lower case nodes
  again."
  [path]
  (if (visited-lower-twice? path)
    (set (filter lower-case? path))
    #{"start"}))

(def paths-2 (paths-from-filter locs-to-remove))


(test/deftest test-part-2
  (test/are [x y] (= (count (paths-2 x)) y)
    (get test-data 0) 36
    (get test-data 1) 103
    (get test-data 2) 3509
    data 94849))

(time (def ans2 (count (paths-2 data))))
(println)
(println "Answer 2:" ans2)

(test/run-tests)
