(ns advent24
  (:require
   [clojure.test :as test]
   [clojure.string :as str]))

(def data-string (slurp "../input/24.txt"))
(def test-string "0/2
2/2
2/3
3/4
3/5
0/1
10/1
9/10")

(defn splitter [s]
  (mapv read-string (str/split s #"/")))

(defn process [s]
  (->> (str/split-lines s)
       (map splitter)
       set))

(def data (process data-string))
(def test-data (process test-string))

(defn next-link [end]
  (fn [x]
    (let [[a b] x]
      (cond
        (= a end) [x b]
        (= b end) [x a]
        :else nil))))

(defn max-score [data]
  (let [f (fn inner-max-score [inner front]
            (let [inner (fn [a] (inner inner a))
                  {:keys [end available]} front
                  links (filter some? (map (next-link end) available))]
              (if
               (empty? links) 0
               (reduce max (for [[next end] links]
                             (apply + (inner {:end end :available (disj available next)}) next))))))
        memo-f (memoize f)
        memo-f (partial memo-f memo-f)]
    (memo-f {:end 0 :available data})))

(defn part-1 [data]
  (max-score data))

(test/deftest test-part-1
  (test/is (= (part-1 test-data) 31)))

(time (def ans1 (max-score data)))
(println)
(println "Answer1:" ans1)

(defn path-max [[l1 s1] [l2 s2]]
  (cond
    (> l1 l2) [l1 s1]
    (< l1 l2) [l2 s2]
    (= l1 l2)
    (if (> s2 s1) [l1 s2] [l1 s1])))

(defn max-length [data]
  (let [f (fn inner-max-length [inner front]
            (let [inner (fn [a] (inner inner a))
                  {:keys [end available]} front
                  links (filter some? (map (next-link end) available))]
              (if
               (empty? links) [0 0]
               (reduce path-max (for [[next end] links]
                                  (mapv +
                                        (inner {:end end :available (disj available next)})
                                        [1 (reduce + next)]))))))
        memo-f (memoize f)
        memo-f (partial memo-f memo-f)]
    (memo-f {:end 0 :available data})))

(defn part-2 [data]
  (second (max-length data)))

(test/deftest test-part-2
  (test/is (= (part-2 test-data) 19)))

(time (def ans2 (part-2 data)))
(println)
(println "Answer 2:" ans2)

(test/run-tests)
