# Advent of Code 2024 - Day 1

(use judge)

(def data (slurp "../input/01.txt"))
(def test-data `3   4
4   3
2   5
1   3
3   9
3   3`)

(def grammar
  ~{:main (some :line)
    :end (+ "\n" -1)
    :line (group (* (number (some :d)) (some :s) (number (some :d)) :end))})


(defn parse-lists [s]
  (apply map tuple (peg/match grammar s)))


(defn part-1 [s]
  (->> (peg/match grammar s)
       (apply map tuple)
       (map sorted)
       (apply map -)
       (map math/abs)
       (reduce + 0)))

(test (part-1 test-data) 11)

(def ans1 (part-1 data))

(defn part-2 [s]
  (let [[one two]
        (->> (peg/match grammar s)
             (apply map tuple)
             (map sorted))
        counts (frequencies two)]
    (reduce + 0 (map (fn [x] (* x (get counts x 0))) one))))

    
(test (part-2 test-data) 31)

(def ans2 (part-2 data))

(defn main [&]
  (print "Answer1:" ans1)
  (print "Answer2:" ans2))
