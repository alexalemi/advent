(ns advent14
  (:require
   [clojure.test :as test]
   [clojure.string :as str]
   [clojure.set :as set]))

(def test-string "flqrgnkx")
(def data-string (str/trim (slurp "../input/14.txt")))

(defn initial-state [n]
  {:position 0 :skip-size 0 :vals (range n)})

(defn step [state length]
  (let [{:keys [position skip-size vals]} state
        n (count vals)
        x (cycle vals)
        x (drop position x)
        [cut x] (split-at length x)
        rest (take (- n length) x)
        joined (cycle (concat (reverse cut) rest))]
    (-> state
        (assoc :vals (take n (drop (- n position) joined)))
        (update :position + (+ skip-size length))
        (update :position mod n)
        (update :skip-size inc)
        (update :skip-size mod n))))

(defn expand-input [inp]
  (as-> inp s
    (map int s)
    (concat s [17 31 73 47 23])
    (repeat 64 s)
    (flatten s)))

(defn hexify [vs]
  (apply str (map #(format "%02x" %) vs)))

(defn unhexify [hex]
  (apply str
         (map
          (fn [[x y]] (char (Integer/parseInt (str x y) 16)))
          (partition 2 hex))))

(defn knot-hash [s]
  (->> s
       expand-input
       (reduce step (initial-state 256))
       :vals
       (partition 16)
       (map (partial reduce bit-xor))
       hexify))

(defn hex-to-binary [s]
  (map {true 1 false 0}
       (mapcat (fn [i] (map #(bit-test i %) (reverse (range 8))))
               (map #(Integer/parseInt % 16)
                    (map #(apply str %) (partition 2 s))))))

(def s "a0c2017")
(hex-to-binary s)

(defn gen-field [key]
  (pmap
   (comp
    hex-to-binary
    knot-hash
    #(str key "-" %))
   (range 128)))

(time (def test-field (gen-field test-string)))
(time (def field (gen-field data-string)))

(defn part-1 [field]
  (reduce +
          (map #(reduce + %) field)))

(test/deftest test-part-1
  (test/is (= (part-1 test-field) 8108))
  (test/is (= (part-1 field) 8194)))

(time (def ans1 (part-1 field)))
(println)
(println "Answer 1:" ans1)

(defn make-map [field]
  (reduce set/union #{} (map-indexed (fn [row hex] (into #{} (keep-indexed (fn [k v] (when (= v 1) [row k])) hex))) field)))

(def field-map (make-map field))
(def test-field-map (make-map test-field))

(defn neighbors [node]
  (let [[x y] node]
    #{[(inc x) y]
      [(dec x) y]
      [x (inc y)]
      [x (dec y)]}))

(defn cluster
  "Generate a connected cluster."
  ([field-map query] (cluster field-map #{} #{query}))
  ([field-map seen queue]
   (if-let [node (first queue)]
     (recur
      field-map
      (conj seen node)
      (set/union (disj queue node)
                 (set/difference
                  (set/intersection
                   (neighbors node) field-map) seen)))
     seen)))

(defn remove-cluster [field-map]
  (apply disj field-map (cluster field-map (first field-map))))

(defn part-2 [field-map]
  (count (take-while not-empty (iterate remove-cluster field-map))))

(test/deftest test-part-2
  (test/is (= (part-2 test-field-map) 1242))
  (test/is (= (part-2 field) 1141)))

(time (def ans2 (part-2 field-map)))
(println)
(println "Answer 2:" ans2)

(test/run-tests)
