(use ./util)
(use judge)

(def data-string (string/trim (slurp "../input/07.txt")))
(def test-string `32T3K 765
T55J5 684
KK677 28
KTJJT 220
QQQJA 483`)

(defn ->data [s]
  (def card-vals
    {"T" 10 "J" 11 "Q" 12 "K" 13 "A" 14})
  (def poker-peg
    ~{:card (+ (number :d) (/ (<- (set "TJQKAJ")) ,card-vals))
      :line (* (group (repeat 5 :card)) :s+ (number :d+))
      :main (some (+ :line "\n"))})
  (freeze (partition 2 (peg/match poker-peg s))))

(def data (->data data-string))
(def test-data (->data test-string))
(test test-data
      [[[3 2 10 3 13] 765]
       [[10 5 5 11 5] 684]
       [[13 13 6 7 7] 28]
       [[13 10 11 11 10] 220]
       [[12 12 12 11 14] 483]])

(defn hand-type [hand]
  (case (freeze (sorted (values (frequencies hand))))
    [5] 700 # five of a kind
    [1 4] 600 # four of a kind
    [2 3] 500 # full house
    [1 1 3] 400 # three of a kind
    [1 2 2] 300 # two pair
    [1 1 1 2] 200 # one pair
    [1 1 1 1 1] 100 # high card
    [] 700)) # degenerate case of 5 jokers, is five of a kind.

(defn part-1 [data]
  (defn winnings [[rank- [[hand-type hand] bid]]]
    (* (inc rank-) bid))
  (->> data
       (map (juxt (comp (juxt hand-type identity) first) second))
       (sorted)
       (indexed)
       (map winnings)
       (sum)))


(test (part-1 test-data) 6440)
(def ans1 (part-1 data))
(test ans1 250232501)

# Part 2

(defn mutate-jokers
  "Have the Jokers turn into whatever card is most populated."
  [hand]
  (defn mode [hand]
    (->> hand
         # remove the jokers
         (filter pos?)
         (frequencies)
         (pairs)
         (sort-by (fn [[k v]] [v k]))
         (last)
         # the most common card
         (first)))
  (def target (mode hand))
  (defn replace [x] (if (= x 0) target x))
  (freeze (map replace hand)))


(defn part-2 [data]
  (defn winnings [[rank- [[hand-type hand] bid]]]
    (* (inc rank-) bid))
  (defn j->joker [hand]
    (freeze (map (fn [x] (if (= x 11) 0 x)) hand)))
  (->> data
       (map (fn [[hand bid]] [(j->joker hand) bid]))
       (map (juxt (comp (juxt (comp hand-type mutate-jokers) identity) first) second))
       (sorted)
       (indexed)
       (map winnings)
       (sum)))

(test (part-2 test-data) 5905)
(def ans2 (part-2 data))
(test ans2 249138943)


(defn main [&]
  (print "Answer1:" ans1)
  (print "Answer2:" ans2))
