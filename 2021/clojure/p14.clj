(ns advent14
  (:require
   [clojure.string :as str]
   [clojure.test :as test]))

(def test-string "NNCB

CH -> B
HH -> N
CB -> H
NH -> C
HB -> C
HC -> B
HN -> C
NN -> C
BH -> H
NC -> B
NB -> B
BN -> B
BB -> N
BC -> B
CC -> N
CN -> C")
(def data-string (slurp "../input/14.txt"))

(defn map-keys [f m]
  (reduce-kv (fn [m k v] (assoc m (f k) v)) {} m))

(defn map-vals [f m]
  (reduce-kv (fn [m k v] (assoc m k (f v))) {} m))

(defn process [s]
  (let [[init rules] (str/split s #"\n\n")
        rules (str/split-lines rules)]
    {:polymer (frequencies (partition 2 1 init))
     :init init
     :final (last init)
     :rules (map-vals first (map-keys seq (into {} (map #(str/split % #" -> ") rules))))}))

(defn s-to-lets [s]
  (frequencies (partition 2 1 s)))

(def data (process data-string))
(def test-data (process test-string))

(defn expand-one [rules polymer entry]
  (let [[k v] entry
        [a b] k
        c (rules k)]
    (-> polymer
        (update (list a b) - v)
        (update (list a c) (fnil + 0) v)
        (update (list c b) (fnil + 0) v))))

(defn remove-zeros [m]
  (reduce-kv (fn [m k v] (if (pos? v) (assoc m k v) m)) {} m))

(defn round [data]
  (let [{:keys [rules polymer]} data
        expander (partial expand-one rules)]
    (assoc data :polymer
           (remove-zeros (reduce expander polymer (:polymer data))))))

(defn letter-counts [polymer]
  (reduce-kv
   (fn [m k v] (update m (first k) (fnil + 0) v))
   {}
   polymer))

(defn gap [steps data]
  (let [data (nth (iterate round data) steps)
        {:keys [polymer final]} data
        lets (letter-counts polymer)
        lets (update lets final inc)
        counts (vals lets)
        big (apply max counts)
        little (apply min counts)]
    (- big little)))

(defn part-1 [data] (gap 10 data))

(test/deftest test-part-1
  (test/is (= (:polymer (round test-data))
              (s-to-lets "NCNBCHB")))
  (test/is (= (:polymer (round (round test-data)))
            (s-to-lets "NBCCNBBBCBHCB")))
  (test/is (= (:polymer (nth (iterate round test-data) 3))
            (s-to-lets "NBBBCNCCNBBNBNBBCHBHHBCHB")))
  (test/is (= (:polymer (nth (iterate round test-data) 4))
            (s-to-lets "NBBNBNBBCCNBCNCCNBBNBBNBBBNBBNBBCBHCBHHNHCBBCBHCB")))
  (test/is (= (part-1 test-data) 1588))
  (test/is (= (part-1 data) 2988)))

(time (def ans1 (part-1 data)))
(println)
(println "Answer 1:" ans1)

(defn part-2 [data] (gap 40 data))

(test/deftest test-part-2
  (test/is (= (part-2 test-data) 2188189693529))
  (test/is (= (part-2 data) 3572761917024)))

(time (def ans2 (part-2 data)))
(println)
(println "Answer 2:" ans2)

(test/run-tests)
