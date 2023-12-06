# Advent of Code - Day 6

(import ./util)

(def data-string (slurp "../input/06.txt"))
(def test-string `Time:      7  15   30
                 Distance:  9  40  200`)

(def race-peg
  ~{:num (number :d+)
    :space (some " ")
    :time-line (* "Time:" (group (some (+ :space :num))) "\n")
    :distance-line (* "Distance:" (group (some (+ :space :num))))
    :main (* :time-line :distance-line)})


(defn ->data [s]
  (util/zip ;(peg/match race-peg s)))

(def data (->data data-string))
(def test-data (->data test-string))

(defn ways-to-beat [time distance]
  (var ways 0)
  (loop [hold :range [0 time]]
    (if (> (* hold (- time hold)) distance) (set ways (inc ways))))
  ways)
    
(defn part-1 [data]
  (product (map (partial apply ways-to-beat) data)))

(assert (= (part-1 test-data) 288))

(def ans1 (part-1 data))

(assert (= ans1 74_698))

# Part 2
# The time card had bad kerning and there is only one race.

(defn join-nums [nums]
  (parse (string ;(map string nums))))


(defn part-2 [data] 
  (ways-to-beat ;(map join-nums (zip ;data))))

(assert (= (part-2 test-data) 71503))

(def ans2 (part-2 data))

(assert (= ans2 27_563_421))


(defn main [&]
  (print "Answer1:" ans1)
  (print "Answer2:" ans2))
    


