# Advent of Code - Day 13

(use "./util")
(use judge)

(def data-string (slurp "../input/13.txt"))
(def test-string `#.##..##.
..#.##.#.
##......#
##......#
..#.##.#.
..##..##.
#.#.##.#.

#...##..#
#....#..#
..##..###
#####.##.
#####.##.
..##..###
#....#..#`)

(def data-peg
  ~{:skip (set ".\n")
    :cell (* (<- (group (* (line) (column)))) "#")
    :main (some (+ (group :cell) :skip))})

(defn process-board [board]
  (->set (freeze (keys (from-pairs (peg/match data-peg board))))))

(defn ->data [s]
  (map process-board (string/split "\n\n" s)))

(def data (->data data-string))
(def test-data (->data test-string))

(defn extent [board]
  [(max-of (map first (set-keys board)))
   (max-of (map second (set-keys board)))])

(defn transpose [board]
  (freeze (->set (map reverse (set-keys board)))))

(defn count-mismatch
  "The board reflects about its ith col"
  [board i]
  # to reflect, everywhere there is a cell, there is still a cell
  # and everywhere there isn't a cell, there is not a cell.
  (let [[Y X] (extent board)]
    (if
      (> i (/ X 2))
      # the split is on the right half
      (count false? (seq [x :range-to [(inc i) X]
                          y :range-to [1 Y]]
                      (= (board [y x]) (board [y (- i (dec (- x i)))]))))
      (count false? (seq [x :range-to [1 i]
                          y :range-to [1 Y]]
                      (= (board [y x]) (board [y (+ i (inc (- i x)))])))))))

(defn maybe [x]
  (if (nil? x) 0 x))

(defn score [board]
  (let [[Y X] (extent board)]
    (+ (maybe (find (comp zero? (partial count-mismatch board)) (range 1 X)))
       (* 100 (maybe (find (comp zero? (partial count-mismatch (transpose board))) (range 1 Y)))))))

(defn part-1 [data]
  (sum (map score data)))

(test (part-1 test-data) 405)
(def ans1 (part-1 data))
(test ans1 35232)

# Part 2

(defn score-2 [board]
  (let [[Y X] (extent board)]
    (+ (maybe (find (comp one? (partial count-mismatch board)) (range 1 X)))
       (* 100 (maybe (find (comp one? (partial count-mismatch (transpose board))) (range 1 Y)))))))

(defn part-2 [data]
  (sum (map score-2 data)))

(test (part-2 test-data) 400)
(def ans2 (part-2 data))
# 23078 wrong
(test ans2 37982)

(defn main [&]
  (print "Answer1: " ans1)
  (print "Answer2: " ans2))
