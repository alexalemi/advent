
(use "./util")
(use judge)
(import spork/math)

(def data-string (slurp "../input/08.txt"))
(def test-string `RL

AAA = (BBB, CCC)
BBB = (DDD, EEE)
CCC = (ZZZ, GGG)
DDD = (DDD, DDD)
EEE = (EEE, EEE)
GGG = (GGG, GGG)
ZZZ = (ZZZ, ZZZ)`)

(def ->keyword ~(/ (<- (some :w+)) ,keyword))

(def data-peg
  ~{:keyword (/ (<- (some :w+)) ,keyword)
    :children (group (* :keyword ", " :keyword))
    :line (group (* :keyword " = (" :children ")")) 
    :directions (group (some (/ (<- (set "RL")) ,keyword)))
    :main (* :directions "\n\n" (some (+ :line "\n")))})

(test (peg/match data-peg test-string)
  @[@[:R :L]
    @[:AAA @[:BBB :CCC]]
    @[:BBB @[:DDD :EEE]]
    @[:CCC @[:ZZZ :GGG]]
    @[:DDD @[:DDD :DDD]]
    @[:EEE @[:EEE :EEE]]
    @[:GGG @[:GGG :GGG]]
    @[:ZZZ @[:ZZZ :ZZZ]]])

(defn ->data [s]
   (let [[directions & nodes] (peg/match data-peg s)]
      {:directions (freeze directions)
       :tree (freeze (from-pairs nodes))}))

(test (->data test-string)
  {:directions [:R :L]
   :tree {:AAA [:BBB :CCC]
          :BBB [:DDD :EEE]
          :CCC [:ZZZ :GGG]
          :DDD [:DDD :DDD]
          :EEE [:EEE :EEE]
          :GGG [:GGG :GGG]
          :ZZZ [:ZZZ :ZZZ]}})

(def data (->data data-string))
(def test-data (->data test-string))

(defn step [{:directions directions :tree tree} t loc]
   (let [n (length directions)
         direction (get directions (% t n))
         [left right] (tree loc)]
     (case direction
       :R right
       :L left)))

(test (step test-data 0 :AAA) :CCC)
(test (step test-data 1 :AAA) :BBB)

(defn part-1 [data]
  (defn walk [loc t]
    (if (= loc :ZZZ) t
      (walk (step data t loc) (inc t))))
  (walk :AAA 0))

(test (part-1 test-data) 2)

(def ans1 (part-1 data))

(test ans1 20659)

# Part 2

(def test-string-2 `LR

11A = (11B, XXX)
11B = (XXX, 11Z)
11Z = (11B, XXX)
22A = (22B, XXX)
22B = (22C, 22C)
22C = (22Z, 22Z)
22Z = (22B, 22B)
XXX = (XXX, XXX)`)

(def test-data-2 (->data test-string-2))

(def A-chr (last :AAA))
(def Z-chr (last :AAZ))

(defn ends-A? [k]
  (= (last k) A-chr))
(defn ends-Z? [k]
  (= (last k) Z-chr))

(defn steps [data start]
  (defn walk [loc t]
    (if (ends-Z? loc) t
      (walk (step data t loc) (inc t))))
  (walk start 0))

(defn part-2 [data]
  (->> (filter ends-A? (keys (data :tree)))  # get the locs that end with A
       (map (partial steps data))  # for each figure out the recurrance time
       (map (comp frequencies math/factor)) # prime factorize each and count factors
       ((partial apply merge-with max)) # combine the factorizations
       (pairs) # get pairs
       (map product) # take product
       (product))) # product to reduce

(test (part-2 test-data-2) 6)
(def ans2 (part-2 data))
(test ans2 15690466351717)

(defn main [&]
  (print "Answer1: " ans1)
  (print "Answer2: " ans2))
