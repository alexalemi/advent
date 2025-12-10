# Advent of Code 2025 - Day 3

(use judge)

(def data-string (string/trim (slurp "../input/03.txt")))
(def test-string `987654321111111
811111111111119
234234234234278
818181911112111`)

(def grammar
  ~{:main (split "\n" (group :line))
    :line (some (number :d))})


(defn parse [s]
  (peg/match grammar s))


(def data (parse data-string))
(def test-data (parse test-string))

(defn rest [line]
  (drop 1 line))

(defn maxi [line]
  (defn -maxi [line val loc i]
    (if (empty? line)
      [val loc]
      (if (> (first line) val)
        (-maxi (rest line) (first line) i (inc i))
        (-maxi (rest line) val loc (inc i)))))
  (-maxi (rest line) (first line) 0 1))

(defn max-num [n line]
  (defn -max-num [line val n]
    (if (zero? n)
      val
      (let [[x loc] (maxi (array/slice line 0 (- (length line) (dec n))))]
        (-max-num (array/slice line (inc loc)) (+ (* 10 val) x) (dec n)))))
  (-max-num line 0 n))


(defn part-1 [data]
  (sum (map (partial max-num 2) data)))

(test (part-1 test-data) 357)
(test (part-1 data) 17445)


(defn part-2 [data]
  (sum (map (partial max-num 12) data)))

(test (part-2 test-data) 3121910778619)
(test (part-2 data) 173229689350551)

(defn main [&]

  (def ans1 (part-1 data))
  (print "Answer1: " ans1)
  (def ans2 (part-2 data))
  (print "Answer2: " ans2))
