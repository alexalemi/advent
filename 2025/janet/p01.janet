# Advent of Code - 2025 - Day 1

(use judge)

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

(test (part-1 test-data) 3)

(def ans-1 (part-1 data))


# ## Part 2
# Now we want to know how many times we pass through zero.

(defn step-zeros [[state zeros] instruction]
  (def step (fn [x] (step x instruction)))
  (def [dir amount] instruction)
  (defn consume [state zeros remaining]
    (if (zero? remaining) [state zeros]
      (let [new (step state)]
        (consume new (if (zero? new) (inc zeros) zeros) (dec remaining)))))
  (consume state zeros amount))

(defn part-2 [data]
  (get (reduce step-zeros [INIT 0] data) 1))

(test (part-2 test-data) 6)

(def ans-2 (part-2 data))

(defn main [& args]
  (printf "Answer1: %d" ans-1)
  (printf "Answer2: %d" ans-2))
