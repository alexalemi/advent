;; # 2018 Day 16
;; For this puzzle, there is a simple virtual machine with
;; 16 different operations. Unfortunately, which operation
;; corresponds to which op-code has been scrambled.
;;
;; Thankfully we are given the observations of a bunch of test
;; cases, which we can use to infer which command is which.
;;
;; In the end, we have to use use knowledge to execute a program
;; and determine its result.
(ns p16
  (:require [clojure.string :as str]
            [clojure.set :as set]))

;; ## Input processing
;; the first thing we'll do is parse the input file and separate out both the
;; observations and the commands.

(defn process-observation [lines]
  (let [[_ & args] (re-matches #"Before: \[(\d+), (\d+), (\d+), (\d+)\]\n(\d+) (\d+) (\d+) (\d+)\nAfter:  \[(\d+), (\d+), (\d+), (\d+)\]" lines)
          args (mapv read-string args)]
       {:before (subvec args 0 4)
        :command (subvec args 4 8)
        :after (subvec args 8)}))

(defn process-example [ex]
  (mapv read-string (str/split ex #" ")))

(def data-string (slurp "../input/16.txt"))

(defn process [s]
  (let [[observations commands] (str/split s #"\n\n\n\n")
        observations (str/split observations #"\n\n")
        observations (mapv process-observation observations)
        commands (str/split-lines commands)
        commands (mapv process-example commands)]
    {:observations observations :commands commands}))


;; So in the end, we have a big list of observations {:before :command :after}
;; and a big list of commands
(def data (process data-string))
(def observations (:observations data))
(def commands (:commands data))

;; ## Virtual Machine
;; Now let's implement the virtual machine itself, the state of the machine is given by
;; four registers, which I'll just store in a vector.

(defn step [r [op a b c]]
  (case op
    :addr (assoc r c (+ (r a) (r b)))
    :addi (assoc r c (+ (r a) b))

    :mulr (assoc r c (* (r a) (r b)))
    :muli (assoc r c (* (r a) b))

    :banr (assoc r c (bit-and (r a) (r b)))
    :bani (assoc r c (bit-and (r a) b))

    :borr (assoc r c (bit-or (r a) (r b)))
    :bori (assoc r c (bit-or (r a) b))

    :setr (assoc r c (r a))
    :seti (assoc r c a)

    :gtir (assoc r c (if (> a (r b)) 1 0))
    :gtri (assoc r c (if (> (r a) b) 1 0))
    :gtrr (assoc r c (if (> (r a) (r b)) 1 0))

    :eqir (assoc r c (if (= a (r b)) 1 0))
    :eqri (assoc r c (if (= (r a) b) 1 0))
    :eqrr (assoc r c (if (= (r a) (r b)) 1 0))

    :else (throw "I don't understand the op!")))


;; ## Part 1
;; For the first part, we need only see how many of the observations
;; are consistent with 3 or more op codes.

(defn consistent-with
  "Check to see which op codes are consistent with an observation."
  [observation]
  (let [{:keys [before command after]} observation
        check (fn [op] (= after (step before (assoc command 0 op))))]
    (into #{} (filter check OPS))))


;; Test case
(let [observation {:before [3 2 1 1] :command [9 2 1 2] :after [3 2 2 1]}]
  (consistent-with observation))

(def ans1
  (count
   (into []
    (comp
     (map consistent-with)
     (map count)
     (filter #(>= % 3)))
    (:observations data))))


;; ## Part 2
;; Now we need to figure out the associations and execute the example program.


;; We start by generating something representing the universe
;; of possiblities, each op code could be each of the commands.
(def possibilities
  (into {}
   (for [i (into #{} (map (comp first :command) observations))]
       [i OPS])))


;; We can then figure out the constraints that each of the observations places
;; on things.

(def constraints
  (reduce
   (fn [possibilities [i constraint]]
     (update possibilities i set/intersection constraint))
   possibilities
   (map (juxt (comp first :command) consistent-with) observations)))


;; Now we need to propogate the known cases to see
;; if we can get this to reduce down to everything
;; being known.
(defn propogate [[known constraints]]
 (let [new (update-vals
             (filter #(= 1 (count (second %))) constraints)
             first)
       fixed (into #{} (vals new))]
   [(into known new) (update-vals constraints
                        (fn [x] (set/difference x fixed)))]))

;; This let's us determine all of the instructions uniquely.
(def instructions
  (->> [{} constraints]
      (iterate propogate)
      (drop-while #(< (count (first %)) (count OPS)))
      first
      first))

;; We've uniquely identified the op-codes so we can transform
;; our program into a useable form
(def program
  (map #(assoc % 0 (instructions (first %))) commands))


;; And then we need only execute it.
(def final-state
 (reduce
  step
  [0 0 0 0]
  program))

;; The final answer is the first register of the result.
(def ans2 (final-state 0))
