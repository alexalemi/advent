;; # 🎄 Advent of Code 2018 - Day 19
(ns p19
  (:require [clojure.string :as str]
            [clojure.edn :as edn]))


(def data-string (slurp "../input/19.txt"))
(def test-string "#ip 0
seti 5 0 1
seti 6 0 2
addi 0 1 0
addr 1 2 3
setr 1 0 0
seti 8 0 4
seti 9 0 5")

(defn s->ints [s]
  (map edn/read-string (re-seq #"\d+" s)))

(defn process-instruction [s]
  (concat [(keyword (re-find #"\w+" s))] (s->ints s)))

(defn process [s]
  (let [lines (str/split-lines s)]
    {:ip (first (s->ints (first lines)))
     :loc 0
     :clock 0
     :registers [0 0 0 0 0 0]
     :commands (mapv process-instruction (rest lines))}))

(def test-data (process test-string))
(def data (process data-string))

;; ## Main Program Logic.

(defn execute! [r [op a b c]]
  (case op
    :addr (assoc! r c (+ (r a) (r b)))
    :addi (assoc! r c (+ (r a) b))

    :mulr (assoc! r c (* (r a) (r b)))
    :muli (assoc! r c (* (r a) b))

    :banr (assoc! r c (bit-and (r a) (r b)))
    :bani (assoc! r c (bit-and (r a) b))

    :borr (assoc! r c (bit-or (r a) (r b)))
    :bori (assoc! r c (bit-or (r a) b))

    :setr (assoc! r c (r a))
    :seti (assoc! r c a)

    :gtir (assoc! r c (if (> a (r b)) 1 0))
    :gtri (assoc! r c (if (> (r a) b) 1 0))
    :gtrr (assoc! r c (if (> (r a) (r b)) 1 0))

    :eqir (assoc! r c (if (= a (r b)) 1 0))
    :eqri (assoc! r c (if (= (r a) b) 1 0))
    :eqrr (assoc! r c (if (= (r a) (r b)) 1 0))

    :else (throw "I don't understand the op!")))


(defn complete [state]
  (let [{:keys [registers loc ip commands]} state]
    (loop [registers! (transient registers)
           loc loc
           clock 0]
      (if-let [command (get commands loc)]
        (let [registers! (-> registers!
                           (assoc! ip loc)
                           (execute! command))]
          (recur registers! (inc (registers! ip)) (inc clock)))
        (-> state
            (assoc :registers (persistent! registers!))
            (assoc :clock clock)
            (assoc :loc loc))))))


;; ## Part 1
(complete test-data)

(defonce final-state (complete data))
(def ans1 (get (:registers final-state) 0))

;; ## Part 2
;; Actually running the machine was not working, so I had to look at the assembly and figure out what the program
;; was doing by hand.  Turns out that besides the setup that happsn from instruction 17 onwards,
;; the main program is a quadratic way to sum the factors of a number.

(defn sum-of-factors [n]
  (transduce
   (filter (fn [x] (= 0 (mod n x))))
   +
   (range 1 (inc n))))

(sum-of-factors 1008)

(def ans2 (sum-of-factors 10551408))

(defn -main [_]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
