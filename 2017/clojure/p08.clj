(ns advent08
  (:require
   [clojure.edn :as edn]
   [clojure.string :as str]
   [clojure.test :as test]))

(def test-string "b inc 5 if a > 1
a inc 1 if b < 5
c dec -10 if a >= 1
c inc -20 if c == 10")
(def data-string (slurp "../input/08.txt"))

(def inst-lookup {"inc" + "dec" -})
(def op-lookup {">" > "<" < "<=" <= ">=" >= "==" == "!=" not=})

(defn parse [line]
  (let [[_ reg inst amount test op val]
        (re-matches #"(\w+) (inc|dec) (-?\d+) if (\w+) (>|<|<=|>=|==|!=) (-?\d+)" line)]
    {:reg (keyword reg)
     :inst (inst-lookup inst)
     :amount (edn/read-string amount)
     :treg (keyword test)
     :top (op-lookup op)
     :tval (edn/read-string val)}))

(defn process
  [s]
  (->> (str/split-lines s)
       (map parse)
       (into [])))

(def data (process data-string))
(def test-data (process test-string))

(defn tick [state instr]
  (let [{:keys [reg inst amount treg top tval]} instr]
    (if (top (get state treg 0) tval)
      (update state reg (fnil inst 0) amount)
      state)))

(defn part-1 [data]
  (reduce max (vals (reduce tick {} data))))

(test/deftest test-part-1
  (test/is (= (part-1 test-data) 1))
  (test/is (= (part-1 data) 5946)))

(time (def ans1 (part-1 data)))
(println)
(println "Answer1:", ans1)

(defn part-2-old
  [data]
  (->> (reductions tick {} data)
       (map vals)
       (map (fnil (partial reduce max) [0]))
       (reduce max)))

(defn part-2
  [data]
  (transduce
   (comp (map vals) (map (fnil (partial reduce max) [0])))
   max 0 (reductions tick {} data)))

(test/deftest test-part-2
  (test/is (= (part-2 test-data) 10))
  (test/is (= (part-2 data) 6026)))

(time (def ans2 (part-2 data)))
(println)
(println "Answer2:", ans2)

(test/run-tests)

