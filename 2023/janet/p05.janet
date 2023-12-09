## Advent of Code 2023 - Day 5

(use judge)

(def data-string (slurp "../input/05.txt"))
(def test-string `seeds: 79 14 55 13

seed-to-soil map:
50 98 2
52 50 48

soil-to-fertilizer map:
0 15 37
37 52 2
39 0 15

fertilizer-to-water map:
49 53 8
0 11 42
42 0 7
57 7 4

water-to-light map:
88 18 7
18 25 70

light-to-temperature map:
45 77 23
81 45 19
68 64 13

temperature-to-humidity map:
0 69 1
1 0 69

humidity-to-location map:
60 56 37
56 93 4`)

(defn ->entry [[dest init size]]
  (let [tail (+ init size)]
    [dest init tail]))

(defn contains? [[_ init tail] x]
  (<= init x (dec tail)))

(defn transform [entry x]
  (let [[dest init _] entry]
    (if (contains? entry x) 
      (+ dest (- x init))
      x)))

(test (->entry [50 98 2]) [50 98 100])
(test (->entry [50 98 2]) [50 98 100])
(test (transform (->entry [50 98 2]) 5) 5)
(test (transform (->entry [50 98 2]) 98) 50)


(def data-peg
  ~{:x (+ :a :d (set "-_"))
    :x+ (some :x)
    :entry (/ (group (* (number :d+) :s (number :d+) :s (number :d+))) ,->entry)
    :map (group (* (<- :x+) " map:" :s+ (group (some (+ :entry "\n")))))
    :seeds (group (* "seeds: " (some (+ (number :d+) " "))))
    :main (* :seeds :s+ (group (some (+ :map :s+))))})

(defn ->data [s]
  (let [[seeds map-pairs] (peg/match data-peg s)
        map-names (map first map-pairs)]
    # Check that the maps are in order
    (var current "seed")
    (each name map-names
      (let [[fm to] (peg/match ~(* (<- :w+) "-to-" (<- :w+)) name)]
        (assert (= fm current))
        (set current to)))
    (freeze {:seeds seeds
             :maps (map last map-pairs)})))

(def test-data (->data test-string))
(test test-data
  {:maps [[[50 98 100] [52 50 98]]
          [[0 15 52] [37 52 54] [39 0 15]]
          [[49 53 61] [0 11 53] [42 0 7] [57 7 11]]
          [[88 18 25] [18 25 95]]
          [[45 77 100] [81 45 64] [68 64 77]]
          [[0 69 70] [1 0 69]]
          [[60 56 93] [56 93 97]]]
   :seeds [79 14 55 13]})
(def data (->data data-string))

(defn map-transform [x entries]
  (var result x)
  (each entry entries
    (when (contains? entry x)
        (set result (transform entry x))
        (break)))
  result)

(test (map-transform 97 [[50 98 100] [52 50 98]]) 99)
(test (map-transform 1 [[50 98 100] [52 50 98]]) 1)
(test (map-transform 98 [[50 98 100] [52 50 98]]) 50)

(defn maps-transform [maps x]
  (reduce map-transform x maps))

(test (maps-transform (test-data :maps) 79) 82)
(test (maps-transform (test-data :maps) 14) 43)

(defn part-1 [{:seeds seeds :maps maps}]
  (min ;(map (partial maps-transform maps) seeds)))

(test (part-1 test-data) 35)
(def ans1 (part-1 data))
(test ans1 165788812)

## Part 2
# Now we need to map whole ranges through the data.

(defn ->range [[init size]] [init (+ init size)])


(defn range-transform
  "Transform a range by an entry. Returns original and transformed ranges."
  [[h t] entry]
  (def [dest init tail] entry)
  (defn transform* [entry x] (inc (transform entry (dec x))))
  (cond
    # fully left, do nothing
    (<= tail h) [[[h t]] []]
    # fully right, do nothing
    (> init t) [[[h t]] []]
    # whole inside
    (and (<= init h) (>= tail t))
    [[[]] [(transform entry h) (transform* entry t)]]
    # left half of range inside
    (and (<= init h) (< tail t))
    [[[tail t]] [(transform entry h) (transform* entry tail)]]
    # right half of range inside
    (and (> init h) (>= tail t))
    [[[h init]] [(transform entry init) (transform* entry t)]]
    # contained
    [[[h init] [tail t]]
     [(transform entry init) (transform* entry tail)]]))

(test (range-transform [50 70] [50 98 100]) [[[50 70]] []])
(test (range-transform [500 700] [50 98 100]) [[[500 700]] []]) 
(test (range-transform [120 150] [50 100 200]) [[[]] [70 100]]) 
(test (range-transform [50 150] [75 100 200]) [[[50 100]] [75 125]]) 
(test (range-transform [150 250] [75 100 200]) [[[200 250]] [125 175]]) 
(test (range-transform [100 300] [75 150 200]) 
      [[[100 150] [200 300]] [75 125]]) 

(defn transform-range-with-map [x entries]
  (var old @[x])
  (var transformed @[])
  (each entry entries
    (var untransformed @[])
    (each r old
      (let [[unused active] (range-transform r entry)]
        (when (not (empty? active))
          (array/push transformed active))
        (when (not (empty? unused))
          (array/push untransformed ;unused))))
    (set old untransformed))
  (freeze (filter (comp not empty?) (array/concat old transformed))))

(test (transform-range-with-map [79 93] ((test-data :maps) 0)) [[81 95]])
(test (transform-range-with-map [79 93] ((test-data :maps) 0)) [[81 95]])
(test (range-transform [55 67] [52 50 98]) [[[]] [57 69]])

(defn transform-ranges-with-map [xs entries]
  (catseq [x :in xs] (transform-range-with-map x entries)))

(test (transform-range-with-map [55 67] ((test-data :maps) 0)) [[57 69]])
(test (transform-ranges-with-map [[79 93] [55 67]] ((test-data :maps) 0)) @[[81 95] [57 69]])

(defn transform-ranges-with-maps [xs maps]
  (reduce transform-ranges-with-map xs maps))

(comment
  (let [data test-data
        seed-ranges (map ->range (partition 2 (data :seeds)))]
    (transform-ranges-with-maps seed-ranges (data :maps))))

(defn part-2 [data]
  (let [seed-ranges (map ->range (partition 2 (data :seeds)))]
    (first (min ;(transform-ranges-with-maps seed-ranges (data :maps))))))

(test (part-2 test-data) 46)
(def ans2 (part-2 data))
(test ans2 1928058)

(defn main [&]
  (print "Answer 1:" ans1)
  (print "Answer 2:" ans2))

