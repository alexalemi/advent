(ns advent10
  (:require
   [clojure.string :as str]
   [clojure.test :as test]))

(def test-string "[({(<(())[]>[[{[]{<()<>>
[(()[<>])]({[<{<<[]>>(
{([(<{}[<>[]}>{[]{[(<()>
(((({<>}<{<{<>}{[]{[]{}
[[<[([]))<([[{}[[()]]]
[{[{({}]{}}([{[{{{}}([]
{<[[]]>}<{[{[{[]{()[[[]
[<(<(<(<{}))><([]([]()
<{([([[(<>()){}]>(<<{{
<{([{{}}[<[[[<>{}]]]>[]]")
(def data-string (slurp "../input/10.txt"))

(def braces {\( \) \[ \] \{ \} \< \>})

(defn unmatched-brace
  ([] '())
  ([queue] queue)
  ([queue c]
   (cond
     (contains? braces c) (conj queue (get braces c))
     (= c (peek queue)) (pop queue)
     :else (reduced c))))

(let [s "{([(<{}[<>[]}>{[]{[(<()>"]
  (transduce identity unmatched-brace s))

(def points
  {\) 3
   \] 57
   \} 1197
   \> 25137})

(defn part-1 [s]
  (->> (str/split-lines s)
       (map #(transduce identity unmatched-brace %))
       (keep points)
       (reduce +)))

(test/deftest test-part-1
  (test/is (= (part-1 test-string) 26397))
  (test/is (= (part-1 data-string) 299793)))

(time (def ans1 (part-1 data-string)))
(println)
(println "Answer 1:" ans1)

(def points-2
  {\) 1
   \] 2
   \} 3
   \> 4})

(defn score [completion]
  (reduce (fn [acc c] (+ (* 5 acc) (points-2 c))) 0 completion))

(defn median [x] (let [n (count x)] (nth (sort x) (/ (dec n) 2))))

(defn part-2 [s]
  (->> (str/split-lines s)
       (map #(transduce identity unmatched-brace %))
       (filter (complement char?))
       (map score)
       median))

(test/deftest test-part-2
  (test/is (= (part-2 test-string) 288957))
  (test/is (= (part-2 data-string) 3654963618)))

(time (def ans2 (part-2 data-string)))
(println "Answer 2:" ans2)

(test/run-tests)

