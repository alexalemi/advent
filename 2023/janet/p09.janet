## Advent of Code 2023 - Day 9

(use "./util")
(use judge)

(def data-string (string/trim (slurp "../input/09.txt")))
(def test-string `0 3 6 9 12 15
1 3 6 10 15 21
10 13 16 21 30 45`)

(defn process-line [line]
  (map parse (string/split " " line)))

(defn ->data [s]
  (freeze 
    (map process-line (string/split "\n" s))))

(def data (->data data-string))
(def test-data (->data test-string))
(test test-data
  [[0 3 6 9 12 15]
   [1 3 6 10 15 21]
   [10 13 16 21 30 45]])

(defn accumulator [state0]
  (var state state0)
  (fn [increment]
    (+= state increment)
    state))

(test ((accumulator 5) 0) 5)
(test ((accumulator 5) 5) 10)

(defn differences [xs]
  (defn diff [[_ past] x] [(- x past) x])
  (drop 1 (map first (accumulate diff [0 0] xs))))

(test (differences [0 3 6 9 12 15]) [3 3 3 3 3])
(test (differences [3 3 3 3 3]) [0 0 0 0])

(defn all-zeros? [xs] (all zero? xs))

(test (all-zeros? [0 0 0 0]) true)
(test (all-zeros? [0 0 1 0]) false)

(defn generator [line]
  (let [difference-seqs (take-until all-zeros? (iterate differences line))
        accumulators (map (comp accumulator last) (reverse difference-seqs))]
    (generate [_ :iterate true] (reduce (fn [val f] (f val)) 0 accumulators))))

(test (resume (generator (test-data 0))) 18)
(test (resume (generator (test-data 1))) 28)
(test (resume (generator (test-data 2))) 68)

(defn part-1 [data]
  (sum (map (comp resume generator) data)))

(test (part-1 test-data) 114)
(def ans1 (part-1 data))
(test ans1 1938800261)

## Part 2

(defn reverse-generator [line]
  (let [difference-seqs (take-until all-zeros? (iterate differences line))
        accumulators (map (comp accumulator first) (reverse difference-seqs))]
    (generate [_ :iterate true] (reduce (fn [val f] (f (- val))) 0 accumulators))))

(test (resume (reverse-generator (test-data 2))) 5)

(defn part-2 [data]
  (sum (map (comp resume reverse-generator) data)))

(test (part-2 test-data) 2)
(def ans2 (part-2 data))
(test ans2 1112)

(defn main [&]
  (print "Answer1:" ans1)
  (print "Answer2:" ans2))

