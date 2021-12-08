(ns advent08
  (:require
   [clojure.test :as test]
   [clojure.string :as str]
   [clojure.math.combinatorics :as combo]
   [clojure.set :as set]))

(def data-string (slurp "../input/08.txt"))
(def test-string "be cfbegad cbdgef fgaecd cgeb fdcge agebfd fecdb fabcd edb | fdgacbe cefdb cefbgd gcbe
edbfga begcd cbg gc gcadebf fbgde acbgfd abcde gfcbed gfec | fcgedb cgb dgebacf gc
fgaebd cg bdaec gdafb agbcfd gdcbef bgcad gfac gcb cdgabef | cg cg fdcagb cbg
fbegcd cbd adcefb dageb afcb bc aefdc ecdab fgdeca fcdbega | efabcd cedba gadfec cb
aecbfdg fbg gf bafeg dbefa fcge gcbea fcaegb dgceab fcbdga | gecf egdcabf bgf bfgea
fgeab ca afcebg bdacfeg cfaedg gcfdb baec bfadeg bafgc acf | gebdcfa ecba ca fadegcb
dbcfg fgd bdegcaf fgec aegbdf ecdfab fbedc dacgb gdcebf gf | cefg dcbef fcge gbcadfe
bdfegc cbegaf gecbf dfcage bdacg ed bedf ced adcbefg gebcd | ed bcgafe cdgba cbgef
egadfb cdbfeg cegd fecab cgb gbdefca cg fgcdab egfdb bfceg | gbdfcae bgc cg cgb
gcafb gcf dcaebfg ecagb gf abcdeg gaef cafbge fdbac fegbdc | fgae cfgab fg bagce")

(defn read-set [s]
  (into #{} (map (comp keyword str) s)))

(defn process-line [line]
  (let [[front back] (str/split line #"\|")]
    (letfn [(setter [s] (map read-set (str/split (str/trim s) #" ")))]
      {:signals (setter front)
       :outputs (setter back)})))

(defn process [s] (map process-line (str/split-lines s)))

(def data (process data-string))
(def test-data (process test-string))

(defn part-1 [data]
  (->> data
       (map :outputs)
       (map (fn [outputs] (keep #(#{3 7 4 2} (count %)) outputs)))
       (map count)
       (reduce +)))

(test/deftest test-part-1
  (test/is (= (part-1 test-data) 26))
  (test/is (= (part-1 data) 514)))

(time (def ans1 (part-1 data)))
(println)
(println "Answer 1:" ans1)

(def segments [:a :b :c :d :e :f :g])
(def patterns
  {0 #{:a :b :c :e :f :g}
   1 #{:c :f}
   2 #{:a :c :d :e :g}
   3 #{:a :c :d :f :g}
   4 #{:b :c :d :f}
   5 #{:a :b :d :f :g}
   6 #{:a :b :d :e :f :g}
   7 #{:a :c :f}
   8 #{:a :b :c :d :e :f :g}
   9 #{:a :b :c :d :f :g}})
(def rev-pattern (set/map-invert patterns))

(defn translate
  "Convert a list of sets according to some ordered substitution."
  [ord sets]
  (let [subs (zipmap ord segments)]
    (map #(into #{} (map subs %)) sets)))

(defn translate-map
  "Convert a list of sets according to some ordered substitution."
  [subs sets]
  (map #(into #{} (map subs %)) sets))

(defn test-sub [signal ord]
  (= (set (vals patterns)) (set (translate ord signal))))

(defn solve [signal]
  (first (filter (partial test-sub signal) (combo/permutations segments))))

(defn flatten-num [nums]
  (loop [nums nums out 0]
    (if-let [digit (first nums)]
      (recur (rest nums) (+ (* out 10) digit))
      out)))

(defn solve-line-slow [datum]
  (let [{:keys [signals outputs]} datum
        sol (solve signals)]
    (flatten-num (map rev-pattern (translate sol outputs)))))

(def count-map
  (let [freqs (frequencies (mapcat seq (vals patterns)))]
    (into {} (map (fn [[k v]] [(sort (map freqs v)) k]) patterns))))


(defn solve-line [datum]
  (let [{:keys [signals outputs]} datum
        freqs (frequencies (mapcat seq signals))]
    (flatten-num (map count-map (map sort (map #(map freqs %) outputs))))))

(defn part-2 [data]
  (reduce + (map solve-line data)))

(test/deftest test-part-2
  (test/is (= (part-2 test-data) 61229))
  (test/is (= (part-2 data) 1012272)))

(time (def ans2 (part-2 data)))
(println)
(println "Answer 2:" ans2)

(test/run-tests)

