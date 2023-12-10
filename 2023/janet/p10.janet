# Advent of Code 2023 - Day 10

(use "./util")
(use judge)

(def data-string (slurp "../input/10.txt"))
(def test-string `-L|F7
7S-7|
L|7||
-L-J|
L|-JF`)
(def test-string-2 `7-F7-
.FJ|7
SJLL7
|F--J
LJ.LJ`)

(def maze-peg
  ~{:blank (set ".\n")
    :start (<- (* (group (* (line) (column))) "S"))
    :moves (<- (group (* (* (line) (column)) (set "|-LJ7F"))))
    :main (some (+ :start :moves :blank))})
    
(defn ->data [s]
  (freeze (struct ;(peg/match maze-peg s))))

(def data (->data data-string))
(def test-data (->data test-string))
(def test-data-2 (->data test-string-2))

(def pipes 
  "How the symbols connect the edges."
  {"|" [:n :s]
   "-" [:e :w]
   "L" [:e :n]
   "J" [:n :w]
   "7" [:s :w]
   "F" [:e :s]})

(defn pipe-move
  "Given the connections, how a pipe moves an edge."
  [[a b] x] 
  (case x
    a b
    b a))

(def opposite 
  "The opposite of a given edge."
  {:n :s
   :s :n
   :e :w
   :w :e})

(def movements
  "How the directions move on the grid."
  {:n [-1 0]
   :s [1 0]
   :e [0 1]
   :w [0 -1]})

(defn board-move
  "How coordinates move on board."
  [[dx dy] [x y]] [(+ x dx) (+ y dy)])

(defn step
  "Move one step on the map."
  [[pos edge] pipe]
  (when pipe
    (if-let [new-edge (pipe-move (pipes pipe) edge)]
      [(board-move (movements new-edge) pos) 
       (opposite new-edge)])))

(defn round [data [pos edge]]
  (step [pos edge] (data pos)))

(test (step [[0 0] :n] "|") [[1 0] :n])
(test (step [[0 0] :n] "-") nil)
(test (step [[0 0] :n] "J") [[0 -1] :e])

(defn has-match?
  "Tests whether we have a match in the collection of states."
  [pts]
  (let [positions (map first pts)]
    (not= (length (distinct positions)) (length positions))))

(defn process-loop
  "Construct the main loop as well as identify the correct starting symbol."
  [data]
  (let [start ((invert data) "S")
        round* (partial round data)
        starts (filter round* (seq [edge :in [:n :s :e :w]] 
                                [(board-move (movements edge) start) (opposite edge)]))
        one-step (fn [x] (filter some? (map round* x)))
        circuit (take-until has-match? (iterate one-step starts))
        end (first (round* (first (last circuit))))]
    {:loop (tuple start end ;(map first (array/concat ;circuit)))
     :far (inc (length circuit))
     :start start
     :start-sym ((invert pipes) (freeze (sorted (map (comp opposite second) (first circuit)))))}))


(defn part-1 [data]
  ((process-loop data) :far))

(test (part-1 test-data) 4)
(test (part-1 test-data-2) 8)
(def ans1 (part-1 data))
(test ans1 6682)


## Part 2
## Now we need to calculate the area enclosed by the loop.

(def test-data-3 (->data `...........
.S-------7.
.|F-----7|.
.||.....||.
.||.....||.
.|L-7.F-J|.
.|..|.|..|.
.L--J.L--J.
...........`))
(def test-data-4 (->data `.F----7F7F7F7F-7....
.|F--7||||||||FJ....
.||.FJ||||||||L7....
FJL7L7LJLJ||LJ.L-7..
L--J.L7...LJS7F-7L7.
....F-J..F7FJ|L7L7L7
....L7.F7||L7|.L7L7|
.....|FJLJ|FJ|F7|.LJ
....FJL-7.||.||||...
....L---J.LJ.LJLJ...`))

(def test-data-5 (->data `..........
.S------7.
.|F----7|.
.||....||.
.||....||.
.|L-7F-J|.
.|..||..|.
.L--JL--J.
..........`))
(def test-data-6 (->data `FF7FSF7F7F7F7F7F---7
L|LJ||||||||||||F--J
FL-7LJLJ||||||LJL-77
F--JF--7||LJLJ7F7FJ-
L---JF-JLJ.||-FJLJJ7
|F|F-JF---7F7-L7L|7|
|FFJF7L7F-JF7|JL---7
7-L-JL7||F7|L7F-7F7|
L.L7LFJ|||||FJL7||LJ
L7JLJL-JLJLJL--JLJ.L`))

(defn extent [ks]
  [(max-of (map first ks))
   (max-of (map second ks))])

(defn interior [data]
  (let [{:loop loop-locs :start start :start-sym start-sym} (process-loop data)
        walls (->set loop-locs)
        [Y X] (extent (keys data))
        flippers (->set ["|" "J" "L"])]
    # replace the S with the right symbol
    (var data (struct/to-table data))
    (put data start start-sym)
    (var inside (->set []))
    (for y 1 (inc Y)
      (var outside true)
      (for x 1 (inc X)
        (let [in-loop (set-has? walls [y x])
              sym (data [y x])]
          (when (and (not in-loop) (not outside)) 
            (set-add! inside [y x]))
          (if (and in-loop (set-has? flippers sym))
            (toggle outside)))))
    inside))

(defn part-2 [data] (length (interior data)))

(test (part-2 test-data-3) 4)
(test (part-2 test-data-5) 4)
(test (part-2 test-data-4) 8)
(test (part-2 test-data-6) 10)
(def ans2 (part-2 data))
(test ans2 353)

(defn main [&]
  (print "Answer 1:" ans1)
  (print "Answer 2:" ans2))


