(ns advent23
  (:require
   [clojure.string :as str]))

(def data-string (slurp "../input/23.txt"))

(defn keywordize [x]
  (if (number? x) x
      (keyword x)))

(defn reader [line]
  (let [form (read-string (str "(" line ")"))]
    (mapv keywordize form)))

(defn process [s]
  [(mapv reader (str/split-lines s))
   {:loc 0
    :memory {:a 0 :b 0 :c 0 :d 0 :e 0 :f 0 :g 0 :h 0}}])

(def data (process data-string))

(defn get-value [state loc]
  (if (number? loc)
    loc
    (get (:memory state) loc 0)))

(defn step [registers state]
  (let [{:keys [loc memory]} state
        inst (get registers loc)
        [call to val] inst
        val (get-value state val)
        state (update state :loc inc)]
    (case call
      nil  :halted
      :set (assoc-in state [:memory to] val)
      :sub (update-in state [:memory to] - val)
      :mul (update-in state [:memory to] * val)
      :jnz (if (not= (get-value state to) 0)
             (update state :loc + (dec val))
             state))))

(defn get-inst [registers state]
  (let [{:keys [loc]} state]
    (first (get registers loc [nil]))))

(defn part-1 [registers state]
  (let [stepper (partial step registers)]
    (count (filter #{:mul} (map (partial get-inst registers) (take-while #(not= % :halted) (iterate stepper state)))))))

(time (def ans1 (apply part-1 data)))
(println)
(println "answer 1:" ans1)

(comment
  "For part 2 we need to figure out what would happen if :a is set to 1.
   looking at the program, the difference is at the start.")
(comment
  0 set b 99
  1 set c b
  2 jnz a 2
  3 jnz 1 5
  A 4 mul b 100
  5 sub b -100000
  6 set c b
  7 sub c -17000
  B 8 set f 1
  9 set d 2
  10 set e 2
  11 set g d
  12 mul g e
  13 sub g b
  14 jnz g 2
  15 set f 0
  16 sub e -1
  17 set g e
  18 sub g b
  19 jnz g -8
  20 sub d -1
  21 set g d
  22 sub g b
  23 jnz g -13
  24 jnz f 2
  25 sub h -1
  26 set g b
  27 sub g c
  28 jnz g 2
  29 jnz 1 3
  30 sub b -17
  31 jnz 1 -23)

(comment
  (let [[registers state] data
        stepper (partial step registers)]
    (println "----Regular run")
    (println (str/join "\n" (take 5 (take-while #(not= % :halted) (iterate stepper state)))))
    (println (last (take-while #(not= % :halted) (iterate stepper state))))
    (println "----New run")
    (println (str/join "\n" (take 8 (take-while #(not= % :halted) (iterate stepper (assoc-in state [:memory :a] 1))))))))

(comment
  "For part b, it seems the program is counting the number of composite numbers
between b and c in incremenets of 17.")

(defn prime? [x]
  (if (some #(= (mod x %) 0)
            (range 2 (inc (int (Math/sqrt x)))))
    false
    true))

(time (def ans2 (let [cands (range 109900 (inc 126900) 17)]
                  (count (filter (complement prime?) cands)))))
(println)
(println "Answer 2:" ans2)





