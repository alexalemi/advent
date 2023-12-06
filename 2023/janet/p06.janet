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
  (peg/match race-peg s))

(def data (->data data-string))
(def test-data (->data test-string))

(defn roots [t d]
  (let [x (math/sqrt (- (* t t) (* 4 d)))]
   [(math/ceil (math/next (/ (- t x) 2) math/inf)) 
    (math/floor (math/next (/ (+ t x) 2) 0))]))

(defn ways-to-beat-slow [time distance]
  (var ways 0)
  (loop [hold :range [0 time]
         :when (> (* hold (- time hold)) distance)]
    (++ ways))
  ways)

(defn ways-to-beat [time distance]
  (let [[lo hi] (roots time distance)]
    (inc (- hi lo))))

(defn part-1 [data]
  (product (map ways-to-beat ;data)))

(assert (= (part-1 test-data) 288))

(def ans1 (part-1 data))

(assert (= ans1 74_698))

# Part 2
# The time card had bad kerning and there is only one race.

(defn join-nums [nums]
  (parse (string ;(map string nums))))


(defn part-2 [data] 
  (ways-to-beat ;(map join-nums data)))

(assert (= (part-2 test-data) 71503))

(def ans2 (part-2 data))

(assert (= ans2 27_563_421))


(defn main [&]
  (print "Answer1:" ans1)
  (print "Answer2:" ans2))
    


