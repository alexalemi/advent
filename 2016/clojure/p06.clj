(ns p06
  (:require [clojure.string :as str]))

(def data-string (slurp "../input/06.txt"))

(def test-string "eedadn
drvtee
eandsr
raavrd
atevrs
tsrnev
sdttsa
rasrtv
nssdts
ntnada
svetve
tesnvt
vntsnd
vrdear
dvrsen
enarar")

(defn part-1 [s]
  (apply str (->> s
                str/split-lines
                (apply mapv vector)
                (map frequencies)
                (map #(sort-by val %))
                (map reverse)
                (map first)
                (map first))))
    

(comment
  (part-1 test-string))

(defonce ans1 (part-1 data-string))
(println "Answer1:" ans1)


(defn part-2 [s]
  (apply str (->> s
                str/split-lines
                (apply mapv vector)
                (map frequencies)
                (map #(sort-by val %))
                (map first)
                (map first))))

(comment
 (part-2 test-string))

(defonce ans2 (part-2 data-string))
(println "Answer2:" ans2)
