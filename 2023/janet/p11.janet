# Advent of Code Day 11

(use "./util")
(use judge)

(def data-string (slurp "../input/11.txt"))
(def test-string `...#......
.......#..
#.........
..........
......#...
.#........
.........#
..........
.......#..
#...#.....`)

(def data-peg
  ~{:skip (set ".\n")
    :cell (<- (* (group (* (line) (column))) "#"))
    :main (some (+ :skip :cell))})

(defn ->data [s]
  (freeze (keys (table ;(peg/match data-peg s)))))

(def data (->data data-string))
(def test-data (->data test-string))

(defn extent [xs]
  [(max-of (map first xs))
   (max-of (map last xs))])

(defn find-row [xs row]
  (find |(= $0 row) (map first xs)))

(defn find-col [xs col]
  (find |(= $0 col) (map last xs)))

(defn empties [data]
  (let [[Y X] (extent data)
        find-row* (partial find-row data)
        find-col* (partial find-col data)]
    {:rows (->set (filter (complement find-row*) (range 1 (inc Y))))
     :cols (->set (filter (complement find-col*) (range 1 (inc X))))}))

(defn manhattan-distance [[y1 x1] [y2 x2]]
  (+ (math/abs (- x2 x1)) (math/abs (- y2 y1))))

(defn distance
  [[y1 x1] [y2 x2]
   {:rows rows :cols cols}
   factor]
  (let [empty-row? (partial set-has? rows)
        empty-col? (partial set-has? cols)]
    (defn range* [a b]
      (range (inc (min a b)) (max a b)))
    (+ (manhattan-distance [y1 x1] [y2 x2])
       (* (dec factor) (length (filter empty-col? (range* x1 x2))))
       (* (dec factor) (length (filter empty-row? (range* y1 y2)))))))

(test (manhattan-distance [1 5] [11 10]) 15)
(test (distance [1 4] [9 8] (empties test-data) 2) 15)
(test (distance [6 2] [10 5] (empties test-data) 2) 9)
(test (distance [3 1] [7 10] (empties test-data) 2) 17)
(test (distance [10 1] [10 5] (empties test-data) 2) 5)

(defn sum-of-distances [data factor]
  (let [emp (empties data)
        n (length data)]
    (sum
      (seq [i :range [1 n]
            j :range [0 i]]
        (distance (data i) (data j) emp factor)))))

(defn part-1 [data]
  (sum-of-distances data 2))

(test (part-1 test-data) 374)
(def ans1 (part-1 data))
(test ans1 9648398)

(defn part-2 [data]
  (sum-of-distances data 1000000))

(test (sum-of-distances test-data 10) 1030)
(test (sum-of-distances test-data 100) 8410)
(def ans2 (part-2 data))
(test ans2 618800410814)
# 618801029606 is too high

(defn main [&]
  (print "Answer1: " ans1)
  (print "Answer2: " ans2))
