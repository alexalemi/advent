# Advent of Code Day 14
(use "./util")
(use judge)

(def data-string (slurp "../input/14.txt"))
(def test-string `O....#....
O.OO#....#
.....##...
OO.#O....O
.O.....O#.
O.#..O.#.#
..O..#O..O
.......O..
#....###..
#OO..#....`)

(def data-peg
  ~{:rock (group (<- (* (group (* (line) (column))) "O")))
    :cube (group (<- (* (group (* (line) (column))) "#")))
    :blank (+ "\n" ".")
    :main (some (+ :blank :rock :cube))})


(defn ->data [s]
  (var rocks @[])
  (var cubes @[])
  (eachp [k v] (freeze (from-pairs (peg/match data-peg s)))
    (array/push (if (= v "O") rocks cubes) k))
  (freeze {:cubes (->set cubes) :rocks (->set rocks)}))

(def data (->data data-string))
(def test-data (->data test-string))

(defn extent [{:cubes cubes :rocks rocks}]
  [(max ;(map first (set-keys cubes)) ;(map first (set-keys rocks)))
   (max ;(map second (set-keys cubes)) ;(map second (set-keys rocks)))])

(defn raise [{:cubes cubes :rocks rocks}]
  (let [[Y X] (extent data)]
    (var rocks (struct/to-table rocks))
    (seq [x :range-to [1 X]
          y :range-to [1 Y]]
      (when (set-has? rocks [y x])
        (var new-y y)
        (while (and (> new-y 1) (not (or (set-has? rocks [(dec new-y) x]) (set-has? cubes [(dec new-y) x]))))
          (-- new-y))
        (set-remove! rocks [y x])
        (set-add! rocks [new-y x])))
    rocks))

(defn score [Y rocks]
  (sum (map (comp (fn [y] (inc (- Y y))) first) (set-keys rocks))))

(defn part-1 [data]
  (let [rocks (raise data)
        [Y X] (extent data)]
    (score Y rocks)))

(test (part-1 test-data) 136)
(def ans1 (part-1 data))
(test ans1 113078)


# Part 2
# now we have to do a ton of different iterations, I assume we're gonna hit a cycle.

(defn rot-90 [data]
  (let [{:cubes cubes :rocks rocks} data
        [Y X] (extent data)]
    (defn rot [[y x]]
      [x (inc (- Y y))])
    (freeze {:cubes (map-keys rot cubes) :rocks (map-keys rot rocks)})))

(defn raiser [x]
  {:cubes (x :cubes) :rocks (raise x)})

(defn cycle [x]
  (var x x)
  (let [x (raiser x) # n
        x (rot-90 x)
        x (raiser x) # w
        x (rot-90 x)
        x (raiser x) # s
        x (rot-90 x)
        x (raiser x) # e
        x (rot-90 x)]
    (freeze x)))


(defn detect-cycles [data]
  (var seen @{data 0})
  (var i 0)
  (var x data)
  (set x (cycle x))
  (++ i)
  (while (not (in seen x))
    (put seen x i)
    (set x (cycle x))
    (++ i))
  [i (seen x) x])

(defn part-2 [data]
  (let [[Y X] (extent data)
        [end start x] (detect-cycles data)
        period (- end start)
        rem (% (- 1000000000 start) period)]
    (var x x)
    (loop [i :range [0 rem]]
      (set x (cycle x)))
    (score Y (x :rocks))))

(test (part-2 test-data) 64)
(def ans2 (part-2 data))
(test ans2 94255)


(defn main [&]
  (print "Answer1: " ans1)
  (print "Answer2: " ans2))
