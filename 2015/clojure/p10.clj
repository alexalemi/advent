(ns p10)

(def test-string (seq "1"))
(def data-string (seq "1113122113"))


(defn to-int [c]
  (read-string (str c)))

(defn to-char [i]
  (first (str i)))


(defn speak-and-say [s]
  (flatten (map (fn [c] (list (to-char (count c)) (to-char (first c)))) (partition-by identity s))))

(defonce step-40 (nth (iterate speak-and-say data-string) 40))
(defonce ans1 (count step-40))
(println "Answer1:" ans1)

(defonce step-50 (nth (iterate speak-and-say step-40) 10))
(defonce ans2 (count step-50))
(println "Answer2:" ans2)

(comment
  data-string

  (read-string (str \1))

  (map (fn [c] (to-char (count c)) (to-char (first c))) (partition-by identity data-string))
  (flatten (map (fn [c] (list (to-char (count c)) (to-char (first c)))) (partition-by identity "1122")))

  (nth (iterate speak-and-say data-string 5)))
