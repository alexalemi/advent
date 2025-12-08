# Advent of Code - 2025 - Day 1

# ## Load data

(def data-string (slurp "../input/01.txt"))
(def test-string `L68
L30
R48
L5
R60
L55
L1
L99
R14
L82`)

(defn read-line [s]
  [(keyword (string/slice s 0 1))
   (scan-number (string/slice s 1))])

(defn split-lines [s]
  (string/split "\n" (string/trim s)))

(defn parse [s]
  (map read-line (split-lines s)))


(def data (parse data-string))
(def test-data (parse test-string))


# ## Part 1
# We want to implement a simple state transformation function.

(def SIZE 100)
(def INIT 50)

(defn step [state instruction]
  (let [[dir amount] instruction
        op (case dir :L - :R +)]
    (mod (op state amount) SIZE)))


(defn reductions [f init coll]
  "Like reduce, but returns all intermediate values (scan operation)"
  (var acc init)
  (def result @[init])
  (each item coll
    (set acc (f acc item))
    (array/push result acc))
  result)


(defn part-1 [data]
  (->> data
       (reductions step INIT)
       (filter |(zero? $))
       length))

(assert (= (part-1 test-data) 3))

(def ans-1 (part-1 data))


# ## Part 2
# Now we want to know how many times we pass through zero.

(defn extend-states [states instruction]
  (let [[dir amount] instruction
        current (last states)
        op (case dir :L - :R +)]
    (array/concat states
                  (seq [i :range [1 (+ amount 1)]]
                    (mod (op current i) SIZE)))))


(defn part-2 [data]
  (length (filter |(zero? $) (reduce extend-states @[INIT] data))))

(assert (= (part-2 test-data) 6))

(def ans-2 (part-2 data))

(defn main [& args]
  (printf "Answer1: %d" ans-1)
  (printf "Answer2: %d" ans-2))
