(ns p07
 (:require [clojure.string :as str]))

(def data-string (slurp "../input/07.txt")) 
(def test-string "abba[mnop]qrst
abcd[bddb]xyyx
aaaa[qwer]tyui
ioxxoj[asdfgh]zxcvbn")


(defn remove-brackets 
  "Separate out all of the stuff in square brackets."
  ([s] (remove-brackets s nil nil false))
  ([s out sub-seqs in-bracket]
   (if-let [c (first s)]
     (recur (rest s) 
            (if (or in-bracket (= c \[)) out (conj out c))
            (cond 
              (= c \[) (conj sub-seqs nil)
              (and in-bracket (not= c \])) (conj (rest sub-seqs) (conj (first sub-seqs) c))
              :else sub-seqs)
            (or (= c \[) (and in-bracket (not= c \]))))
     [(apply str (reverse out)) (reverse (map #(apply str (reverse %)) sub-seqs))])))

(defn concat-hypernets [[ip hypernets]]
   [ip (list (apply str (apply concat hypernets)))])

(defn valid-abba? [[one two]] 
  (and (not= (first one) (second one))
       (= one (reverse two))))

(defn contains-abba? [s]
  (let [pairs (partition 2 1 s)]
    (some valid-abba? (map list pairs (drop 2 pairs)))))

(defn supports-tls? [s]
  (let [[ip hypernets] (remove-brackets s)]
   (and (contains-abba? ip)
        (not-any? contains-abba? hypernets))))

(defonce ans1 (count (filter true? (map supports-tls? (str/split-lines data-string)))))
(println "Answer1:" ans1)

(comment
    (count (filter true? (map supports-tls? (str/split-lines test-string)))))

(comment
  (count (str/split-lines data-string)))


(comment 
  (count (filter true? (map supports-tls-2? (str/split-lines data-string)))))
