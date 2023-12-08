# Advent of Code - Day j

(use ./util)
(import jimmy/set)
(use judge)

(def data-string (slurp "../input/04.txt"))
(def test-string `Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53
Card 2: 13 32 20 16 61 | 61 30 68 82 17 32 24 19
Card 3:  1 21 53 59 44 | 69 82 63 72 16 21 14  1
Card 4: 41 92 73 84 69 | 59 84 76 51 58  5 54 83
Card 5: 87 83 26 28 32 | 88 30 70 12 93 22 82 36
Card 6: 31 18 13 56 72 | 74 77 10 23 35 67 36 11`)

(def card-peg
  ~{:nums (/ (group (some (+ (number :d+) :s+))) ,(partial apply set/new))
    :card (group (* "Card" :s+ (number :d+) ":" :s+ (/ (* :nums "| " :nums) ,|{:wins $0 :card $1})))
    :main (some (+ :card "\n"))})

(defn ->data [s]
  (from-pairs (peg/match card-peg s)))

(def data (->data data-string))
(def test-data (->data test-string))
(test test-data
  @{1 {:card "{31 48 9 53 6 83 17 86}"
       :wins "{41 48 83 17 86}"}
    2 {:card "{32 30 82 24 61 19 17 68}"
       :wins "{32 16 20 13 61}"}
    3 {:card "{72 14 21 63 69 1 82 16}"
       :wins "{59 21 1 53 44}"}
    4 {:card "{59 58 84 54 5 76 51 83}"
       :wins "{41 84 73 69 92}"}
    5 {:card "{70 12 93 30 82 36 88 22}"
       :wins "{32 26 87 28 83}"}
    6 {:card "{10 77 36 35 11 67 74 23}"
       :wins "{31 56 13 72 18}"}})

(defn score [num-matches]
  (if (pos? num-matches) 
    (math/pow 2 (dec num-matches))
    0))

(defn winners [{:wins wins :card card}]
    (set/count (* card wins) pos?))

(defn part-1 [data]
  (->> data
       values
       (map (comp score winners))
       (reduce + 0)))

(test (part-1 test-data) 13)
(def ans1 (part-1 data))
(test ans1 22193)

# Part 2
# Now we win cards upon cards


(defn part-2 [data] 
  (let [matches (map-vals winners data)
        counts (map-vals (always 1) matches)]
    (loop [id :range-to [1 (length matches)]
           x :range-to [1 (matches id)]]
      (update counts (+ id x) + (counts id)))
    (sum counts)))

(test (part-2 test-data) 30)
(def ans2 (part-2 data))
(test ans2 5625994)

(defn main [&]
  (print "Answer1:" ans1)
  (print "Answer2:" ans2))
    


