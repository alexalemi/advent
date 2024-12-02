# Advent of Code 2024 - Day 2

(def data (slurp "../input/02.txt"))
(def test-data `7 6 4 2 1
1 2 7 8 9
9 7 6 2 1
1 3 2 4 5
8 6 4 4 1
1 3 6 7 9`)

(def grammar
  ~{:main (some (group :line))
    :end (+ "\n" -1)
    # :line (group (* (some (* (number (some :d)) (some :s))) :end))
    :line (* (number (some :d)) 
             (some (* (some " ") 
                      (number (some :d))))
             :end)})

(defn spread [f] (fn [x] (apply f x)))

(defn parse-lists [s]
  (map (spread tuple) (peg/match grammar s)))

(defn pairs [lst]
  (seq [i :range [0 (dec (length lst))]]
    [(lst i) (lst (inc i))]))


(defn all-increasing [x]
  (every? (map (spread <) (pairs x))))

(defn all-decreasing [x]
  (every? (map (spread >) (pairs x))))

(defn diffs [x]
  (map (spread -) (pairs x)))

(defn at-least-1-at-most-3 [x]
  (let [diffs (map math/abs (map (spread -) (pairs x)))]
    (and (>= (apply min diffs) 1)
         (<= (apply max diffs) 3))))

(defn safe [x]
  (and (or (all-increasing x) (all-decreasing x))
       (at-least-1-at-most-3 x)))

(defn part-1 [s]
  (length (filter safe (parse-lists s))))

(assert (= (part-1 test-data) 2))

(def ans1 (part-1 data))

(assert (= (part-1 data) 359))

(defn minus-1 [lst]
  (seq [i :range [0 (length lst)]]
    (tuple/join (tuple/slice lst 0 i) (tuple/slice lst (inc i)))))

(defn safe-minus-1 [x]
  (or (safe x) (any? (map safe (minus-1 x)))))

(defn part-2 [s]
  (length (filter safe-minus-1 (parse-lists s))))

(assert (= (part-2 test-data) 4))

(def ans2 (part-2 data))

(assert (= (part-2 data) 418))

(defn main [&]
  (print "Answer1:" ans1)
  (print "Answer2:" ans2))
