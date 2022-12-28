;; 🎄 Advent of Code 2018 - Day 21 - Chronal Conversion
;; Another dissembly puzzle
(ns p21
  (:require [clojure.edn :as edn]
            [clojure.string :as str]))

(def data-string (slurp "../input/21.txt"))
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

(def data (process data-string))

;;```
;;  #ip 1  - the instruction pointer is at register 1)
;;0   seti 123 0 5       -  x5 = 123
;;1   bani 5 456 5       -  x5 = x5 & 456
;;2   eqri 5 72 5        -  x5 = x5 == 72
;;3   addr 5 1 1         -  x1 = x1 + x5  (this is a branch depending on the equality check)
;;4   seti 0 0 1         -  x1 = 0  (goto step 1)
;;5   seti 0 6 5         -  x5 = 0
;;6   bori 5 65536 4     - x4 = x5 | 65536
;;7   seti 13431073 4 5  - x5 = 13431073
;;8   bani 4 255 3       - x3 = x4 & 255
;;9   addr 5 3 5         - x5 = x5 + x3
;;10  bani 5 16777215 5  - x5 = x5 & 16777215
;;11  muli 5 65899 5     - x5 = x5 * 65899
;;12  bani 5 16777215 5  - x5 = x5 & 16777215
;;13  gtir 256 4 3       - x3 = 256 > x4
;;14  addr 3 1 1         - x1 = x1 + x3
;;15  addi 1 1 1         - x1 = x1 + 1 (goto step 17)
;;16  seti 27 9 1        - x1 = 27  (goto step 28)
;;17  seti 0 1 3         - x3 = 0
;;18  addi 3 1 2         - x2 = x3 + 1
;;19  muli 2 256 2       - x2 = x2 * 256
;;20  gtrr 2 4 2         - x2 = x2 > x4
;;21  addr 2 1 1         - x1 = x1 + x2
;;22  addi 1 1 1         - x1 = x1 + 1  (goto 24)
;;23  seti 25 4 1        - x1 = 25 (goto step 26)
;;24  addi 3 1 3         - x3 = x3 + 1
;;25  seti 17 8 1        - x1 = 17 (goto step 18)
;;26  setr 3 4 4         - x4 = x3
;;27  seti 7 7 1         - x1 = 7  (goto step 8)
;;28  eqrr 5 0 3         - x3 = x5 == x0  (THIS IS THE ONE TO SET)
;;29  addr 3 1 1         - x1 = x1 + x3
;;30  seti 5 9 1         - x1 = 5 (goto step 6)
;;```

(defn execute [r [op a b c]]
  (case op
    :addr (assoc r c (+ (r a) (r b)))
    :addi (assoc r c (+ (r a) b))

    :mulr (assoc r c (* (r a) (r b)))
    :muli (assoc r c (* (r a) b))

    :divr (assoc r c (quot (r a) (r b)))
    :divi (assoc r c (quot (r a) b))

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

(defn execute! [r [op a b c]]
  (case op
    :addr (assoc! r c (+ (r a) (r b)))
    :addi (assoc! r c (+ (r a) b))

    :mulr (assoc! r c (* (r a) (r b)))
    :muli (assoc! r c (* (r a) b))

    :divr (assoc! r c (quot (r a) (r b)))
    :divi (assoc! r c (quot (r a) b))

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

(defn step [state]
  (let [{:keys [registers clock loc ip commands]} state]
    (let [command (get commands loc)
          registers (-> registers
                        (assoc ip loc)
                        (execute command))
          loc (inc (registers ip))]
      (-> state
          (assoc :registers registers)
          (update :clock inc)
          (assoc :loc loc)))))

(defn complete [state max-time]
  (let [{:keys [registers loc ip commands]} state]
    (loop [registers! (transient registers)
           loc loc
           clock 0]
      (if (>= clock max-time)
        (-> state
            (assoc :registers (persistent! registers!))
            (assoc :clock clock)
            (assoc :loc loc))
        (if-let [command (get commands loc)]
          (let [registers! (-> registers!
                               (assoc! ip loc)
                               (execute! command))]
            (recur registers! (inc (registers! ip)) (inc clock)))
          (-> state
              (assoc :registers (persistent! registers!))
              (assoc :clock clock)
              (assoc :loc loc)))))))

;; ## Part 1

(def ans1 (get-in (first (filter (fn [x] (= 28 (:loc x))) (iterate step data))) [:registers 5]))

;; And we'll test that it terminates...
(complete (assoc-in data [:registers 0] ans1) 100000)

;; ## Part 2

;; For Part 2, I think we really need to sort out what is happening with the
;; program.  I have a hunch after looking at it some that the program tries to
;; run something like a linear congruent random number generator thing.
;;
;; When we come out of the initial startup of the machine, we are at location 31
;; with registers as follows:

;;```
;;  #ip 1  - the instruction pointer is at register 1)
;;
;;6   bori 5 65536 4     - x4 = x5 | 65536  (set's the 17th bit)
;;7   seti 13431073 4 5  - x5 = 13431073
;;8   bani 4 255 3       - x3 = x4 & 255  = x4 % 256
;;9   addr 5 3 5         - x5 = x5 + x3   = x5 + x3
;;10  bani 5 16777215 5  - x5 = x5 & 16777215 = x5 % 2**24
;;11  muli 5 65899 5     - x5 = x5 * 65899    = x5 * 65899
;;12  bani 5 16777215 5  - x5 = x5 & 16777215 = x5 % 2**24
;;
;;13  gtir 256 4 3       - x3 = 256 > x4
;;14  addr 3 1 1         - x1 = x1 + x3
;;15  addi 1 1 1         - x1 = x1 + 1 (goto step 17)
;;16  seti 27 9 1        - x1 = 27  (goto step 28)
;;17  seti 0 1 3         - x3 = 0
;;18  addi 3 1 2         - x2 = x3 + 1
;;19  muli 2 256 2       - x2 = x2 * 256
;;
;;20  gtrr 2 4 2         - x2 = x2 > x4
;;21  addr 2 1 1         - x1 = x1 + x2
;;22  addi 1 1 1         - x1 = x1 + 1  (goto 24)
;;23  seti 25 4 1        - x1 = 25 (goto step 26)
;;24  addi 3 1 3         - x3 = x3 + 1
;;25  seti 17 8 1        - x1 = 17 (goto step 18)
;;26  setr 3 4 4         - x4 = x3
;;27  seti 7 7 1         - x1 = 7  (goto step 8)
;;28  eqrr 5 0 3         - x3 = x5 == x0  (THIS IS THE ONE TO SET)
;;29  addr 3 1 1         - x1 = x1 + x3
;;30  seti 5 9 1         - x1 = 5 (goto step 6)
;;```

(nth (iterate step data) 1850)

;;```python
;; x0 = X
;; x2 = 1
;; x3 = 0
;; x4 = 1
;; x5 = 3115806
;;
;; ## label 6
;; x4 = x5 + 2**26 if x5 < 2**26 else x5
;; x5 = 13431073
;; ## label 8
;; x3 = x4 % 256
;; x5 = x5 + x3
;; x5 = x5 % 2**24
;; x5 = x5 * 65899
;; x5 = x5 % 2**24
;;
;; if (256 > x4):
;;      if (x5 == x0):
;;          END
;;      else:
;;          goto 6
;; else:
;;      x3 = 0
;;      # label 18
;;      x2 = x3 + 1
;;      x2 = x2 * 256
;;      if (x2 > x4):
;;          x4 = x3
;;          goto 8
;;      else:
;;          x3 = x3 + 1
;;          goto 18
;;
;;;;```

(def optimized-data-string "#ip 1
seti 123 0 5
bani 5 456 5
eqri 5 72 5
addr 5 1 1
seti 0 0 1
seti 0 6 5
bori 5 65536 4
seti 13431073 4 5
bani 4 255 3
addr 5 3 5
bani 5 16777215 5
muli 5 65899 5
bani 5 16777215 5
gtir 256 4 3
addr 3 1 1
addi 1 1 1
seti 19 9 1
seti 0 1 3
divi 4 256 4
seti 7 7 1
eqrr 5 0 3
addr 3 1 1
seti 5 9 1")

(def optimized-data (process optimized-data-string))

(defn find-recurrence [state]
  (let [{:keys [registers loc ip commands]} state]
    (loop [registers! (transient registers)
           loc loc
           clock 0
           last nil
           seen #{}]
      (let [command (get commands loc)
            registers! (-> registers!
                           (assoc! ip loc)
                           (execute! command))
            new-loc (inc (registers! ip))]
        (if (= new-loc 20)
          (let [x (registers! 5)]
            (if (seen x)
              last
              (recur registers! new-loc (inc clock) x (conj seen x))))
          (recur registers! new-loc (inc clock) last seen))))))

(def ans2 (find-recurrence optimized-data))

;; ## Main

(defn -main [& _]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
