# Advent of Code 2024 - Day 3

(def data (string/trim (slurp "../input/03.txt")))
(def test-data `xmul(2,4)%&mul[3,7]!@^do_not_mul(5,5)+mul(32,64]then(mul(11,8)mul(8,5))`)

(defn finder
  "Creates a peg that finds all locations of str in the text."
  [str]
  (peg/compile ~(any (+ ,str 1))))

(def grammar
   (finder ~(group (* "mul(" (number :d+) "," (number :d+) ")")))) 


(defn part-1 [s]
  (->> (peg/match grammar s)
       (map (fn [[a b]] (* a b)))
       (sum)))


(assert (= (part-1 test-data) 161))
(def ans1 (part-1 data))
(assert (= ans1 174561379))


## Part 2

(def test-data-2 `xmul(2,4)&mul[3,7]!^don't()_mul(5,5)+mul(32,64](mul(11,8)undo()?mul(8,5))`)

(defn split-dos [s]
  (peg/match ~(split "do()" (<- (any 1))) s))

(defn upto-dont [s]
 (first (peg/match ~(<- (any (if-not "don't()" 1))) s)))
  
(defn part-2 [s]
  (->> s
       split-dos
       (map upto-dont)
       (map part-1)
       sum))

(assert (= (part-2 test-data-2) 48))
(def ans2 (part-2 data))
(assert (= ans2 106921067))

(defn main [&]
  (print "Answer1:" ans1)
  (print "Answer2:" ans2))
