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

;; Here I'll sort of annotate the program:

;;                      x0 = x2 = x3 = x4 = x5 = 0
;;                      x1 = 0
;;      #ip 1
;;       0  addi 1 16 1  GOTO 17
;;       1  seti 1 4 4   x4 = 1
;;       2  seti 1 1 2   x2 = 1
;;       3  mulr 4 2 5   x5 = x4 * x2
;;       4  eqrr 5 3 5   if (x3 == x3): GOTO 8 
;;       5  addr 5 1 1   
;;       6  addi 1 1 1
;;       7  addr 4 0 0   x0 = x4 + x0
;;       8  addi 2 1 2   x2 = x2 + 1
;;       9  gtrr 2 3 5   if (x3 >= x2) GOTO 3 
;;      10  addr 1 5 1   
;;      11  seti 2 4 1
;;      12  addi 4 1 4   x4 = x4 + 1
;;      13  gtrr 4 3 5   if (x3 >= x4) GOTO 2
;;      14  addr 5 1 1   
;;      15  seti 1 1 1
;;      16  mulr 1 1 1   END
;;      17  addi 3 2 3   x3 = x3 + 2
;;      18  mulr 3 3 3   x3 = x3 * x3
;;      19  mulr 1 3 3   x3 = 19 * x3
;;      20  muli 3 11 3  x3 = 11 * x3
;;      21  addi 5 7 5   x5 = x5 + 7
;;      22  mulr 5 1 5   x5 = 22 * x5
;;      23  addi 5 18 5  x5 = x5 + 18
;;      24  addr 3 5 3   x3 = x3 + x5     
;;      25  addr 1 0 1   if (x0 == 0) GOTO 1  (at this point x3 = 1008)
;;      26  seti 0 7 1   
;;      27  setr 1 3 5   x5 = 27
;;      28  mulr 5 1 5   x5 = 28 * x5
;;      29  addr 1 5 5   x5 = x5 + 29
;;      30  mulr 1 5 5   x5 = 30 * x5
;;      31  muli 5 14 5  x5 = 14 * x5
;;      32  mulr 5 1 5   x5 = 32 * x5
;;      33  addr 3 5 3   x3 = x3 + x5
;;      34  seti 0 7 0   x0 = 0
;;      35  seti 0 6 1   GOTO 1  (at this point x3 = 32188416)

;; Working even more on this program and turning it into an equivalent python program we have:

;;      x1 = x2 = x3 = x4 = x5 = 0
;;      if (x0 == 0):
;;        x3 = 1008
;;      else:
;;        x3 = 32_188_416
;;      x4 = 1
;;      while x3 > x4:
;;        x2 = 1
;;        while x3 >= x2:
;;          x5 = x4 * x2
;;          if x5 != x3:
;;            x0 = x0 + x4
;;          x2 = x2 + 1
;;        x4 = x4 + 1
;;      print x0

;; This is clearly computing the sum of the factors of x3.

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
