# Advent of Code - Day j

(import jimmy/set)

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

(assert (= (part-1 test-data) 13))

(def ans1 (part-1 data))

(assert (= ans1 22_193))

# Part 2
# Now we win cards upon cards

(defn map-vals [f m]
  (from-pairs (seq [[k v] :pairs m] [k (f v)])))

(defn constantly [val]
  (fn [x] val))

(defn part-2 [data] 
  (let [matches (map-vals winners data)
        counts (map-vals (constantly 1) matches)]
     (for id 1 (inc (length matches)) 
       (for x 1 (inc (matches id)) 
         (update counts (+ id x) + (counts id))))
     (reduce + 0 (values counts))))

(assert (= (part-2 test-data) 30))

(def ans2 (part-2 data))

(assert (= ans2 5_625_994))


(defn main [&]
  (print "Answer1:" ans1)
  (print "Answer2:" ans2))
    


