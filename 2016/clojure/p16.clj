(ns p16
 (:require [clojure.string :as str]))

(def data-string (str/trim (slurp "../input/16.txt")))


(def REVERSE {\0 \1 \1 \0})
(def LOOKUP {true \1 false \0})

(defn grow [a]
  (str a \0 (apply str (map REVERSE (reverse a)))))


(defn checksum [s]
  (loop [s s]
    (let [c (apply str (map #(LOOKUP (apply = %)) (partition 2 s)))]
      (if (odd? (count c)) c (recur c)))))


(defn part-1 [disk initial]
  (checksum (subs (first (drop-while #(< (count %) disk) (iterate grow initial))) 0 disk)))

(comment
  (part-1 20 "10000"))

(defonce ans1 (part-1 272 data-string))
(println "Answer1: " ans1)

(defonce ans2 (part-1 35651584 data-string))
(println "Answer2: " ans2)
