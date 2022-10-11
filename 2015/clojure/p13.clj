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

(comment
  (into {} (map process-line (str/split-lines data-string))))
    ; (re-matches #"(\w+) would (gain|lose) (\d+) happiness units by sitting next to (\w+)." s)))
