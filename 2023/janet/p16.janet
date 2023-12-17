# Advent of Code Day 16

(use "./util")
(use judge)

(def data-string (slurp "../input/16.txt"))
(def test-string `.|...\....
|.-.\.....
.....|-...
........|.
..........
.........\
..../.\\..
.-.-/..|..
.|....-|.\
..//.|....`)

(def data-peg
  ~{:empty (set ".\n")
    :elems (group (<- (* (group (* (line) (column))) (set "|-/\\"))))
    :main (some (+ :empty :elems))})

(defn ->data [s]
  (freeze (from-pairs (peg/match data-peg s))))

(def data (->data data-string))
(def test-data (->data test-string))

(defn extent [data]
  [(max-of (map first (keys data)))
   (max-of (map last (keys data)))])

(defn move [[[y x] direction]]
  (case direction
    :right [y (inc x)]
    :left [y (dec x)]
    :up [(dec y) x]
    :down [(inc y) x]))

(defn inside? [data pos]
  (let [[Y X] (extent data)
        [y x] pos]
    (and (<= 1 x X)
         (<= 1 y Y))))

(defn children [data [[y x] direction]]
  (if-let [elem (data [y x])]
    (case elem
      "\\" [[[y x] (case direction
                     :right :down
                     :down :right
                     :up :left
                     :left :up)]]   
      "/" [[[y x] (case direction
                     :right :up
                     :up :right
                     :left :down
                     :down :left)]]
      "-" (if 
            (or (= :right direction) (= :left direction))
            [[[y x] direction]]
            [[[y x] :right]
             [[y x] :left]])
      "|" (if 
            (or (= :up direction) (= :down direction))
            [[[y x] direction]]
            [[[y x] :up]
             [[y x] :down]]))
    [[[y x] direction]]))


(defn fill [data &opt start]
  # we want to fill in the space, we'll start with a frontier and just update
  # the instructions trying to eat anybody we need to
  # the atoms will have their location and direction
  (default start [[1 0] :right])
  (let [children* (partial children data)
        inside?* (partial inside? data)]
    (var frontier @[start])
    (var seen (->set []))
    (while (not (empty? frontier))
      (let [pt (array/pop frontier)
            [x dir] pt
            new-x (move pt)
            kids (children* [new-x dir])]
        (set-add! seen [x dir]) 
        (if (and (inside?* new-x) kids)
          (loop [child :in kids :when (not (set-has? seen child))]
            (array/push frontier child)))))
    (set-remove! seen start)
    (set-keys seen)))

(defn part-1 [data &opt start]
  (default start [[1 0] :right])
  (let [seen (fill data start)]
    (length (->set (map first seen)))))

(test (part-1 test-data) 46)
(def ans1 (part-1 data))
(test ans1 7728)

## Part 2

(defn part-2 [data]
  (let [[Y X] (extent data)
        energized (partial part-1 data)]
    (var best 0)
    (defn doit [start]
      (let [n (energized start)]
        (when (> n best)
          (print "NEW BEST @ " start " with n=" n)
          (set best n))))
    (loop [x :range-to [1 X]]
      (doit [[0 x] :down])
      (doit [[(inc Y) x] :up]))
    (loop [y :range-to [1 Y]]
      (doit [[y 0] :right])
      (doit [[y (inc X)] :left]))
    best))

(test (part-2 test-data) 51)
(def ans2 (part-2 data))
(test ans2 8061)


(defn main [&]
  (print "Answer1: " ans1)
  (print "Answer2: " ans2))
