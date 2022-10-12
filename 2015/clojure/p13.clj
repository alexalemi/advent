(ns p13
  (:require [clojure.string :as str]))


(def data-string (slurp "../input/13.txt"))
(def test-string "Alice would gain 54 happiness units by sitting next to Bob.
Alice would lose 79 happiness units by sitting next to Carol.
Alice would lose 2 happiness units by sitting next to David.
Bob would gain 83 happiness units by sitting next to Alice.
Bob would lose 7 happiness units by sitting next to Carol.
Bob would lose 63 happiness units by sitting next to David.
Carol would lose 62 happiness units by sitting next to Alice.
Carol would gain 60 happiness units by sitting next to Bob.
Carol would gain 55 happiness units by sitting next to David.
David would gain 46 happiness units by sitting next to Alice.
David would lose 7 happiness units by sitting next to Bob.
David would gain 41 happiness units by sitting next to Carol.")

(defn process-line [s]
  (let [[_ frm sign cost to] (re-matches #"(\w+) would (gain|lose) (\d+) happiness units by sitting next to (\w+)." s)]
    {(list frm to)
     (* (read-string cost) (if (= sign "gain") +1 -1))}))

(defonce data (into {} (map process-line (str/split-lines data-string))))
(defonce test-data (into {} (map process-line (str/split-lines test-string))))

(defn permutations [s]
  (lazy-seq
   (if (seq (rest s))
     (apply concat (for [x s] (map #(cons x %) (permutations (remove #{x} s)))))
     [s])))

(defn all-people [data]
  (set (map first (keys data))))

(defn configurations [data]
  (let [people (all-people data)]
     (map #(conj % (first people)) (permutations (rest people)))))

(defn wrap-1 [s] (take (inc (count s)) (cycle s)))

(defn score [data path]
  (let [seatings (partition 2 1 (wrap-1 path))]
   (reduce + (map data (concat seatings (map reverse seatings))))))

(defn best-score [data]
  (apply max (map (partial score data) (configurations data))))

(defonce ans1 (best-score data))
(println "Answer1:" ans1)

(defn augment [data]
  (into data (for [x (all-people data)] {(list "Me" x) 0 (list x "Me") 0})))

(comment
  test-data
  (best-score (augment test-data)))

(defonce ans2 (best-score (augment data)))
(println "Answer2:" ans2)


(comment
  test-data)


        ; (re-matches #"(\w+) would (gain|lose) (\d+) happiness units by sitting next to (\w+)." s)))
