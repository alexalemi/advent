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
            ; (if (or in-bracket (= c \[)) out (conj out c))
            (cond
              (= c \[) (conj out nil)
              (not in-bracket) (conj (rest out) (conj (first out) c))
              :else out)
            (cond 
              (= c \[) (conj sub-seqs nil)
              (and in-bracket (not= c \])) (conj (rest sub-seqs) (conj (first sub-seqs) c))
              :else sub-seqs)
            (or (= c \[) (and in-bracket (not= c \]))))
     [;(apply str (reverse out)) 
      (reverse (map #(apply str (reverse %)) out))
      (reverse (map #(apply str (reverse %)) sub-seqs))])))

(comment
  (remove-brackets "abcd[efgh]ijk[hij]lm"))

(defn valid-abba? [[one two]] 
  (and (not= (first one) (second one))
       (= one (reverse two))))

(defn contains-abba? [s]
  (let [pairs (partition 2 1 s)]
    (some valid-abba? (map list pairs (drop 2 pairs)))))

(defn supports-tls? [s]
  (let [[ip hypernets] (remove-brackets s)]
    ;(and (contains-abba? ip))
    (and (some contains-abba? ip)
         (not-any? contains-abba? hypernets))))

;(defonce ans1 (count (filter true? (map supports-tls? (str/split-lines data-string)))))

(defn bool-to-int [x]
  (if x 1 0))

(comment
  (transduce
    (comp
      (map supports-tls?)
      (filter true?)
      (map bool-to-int))
    +
    (str/split-lines data-string)))

(defonce ans1 (transduce
                (comp
                  (map supports-tls?)
                  (filter true?)
                  (map bool-to-int))
                +
                (str/split-lines data-string)))
(println "Answer1:" ans1)


(defn aba? [[a b c]]
  (and (= a c) (not= a b)))

(defn aba-to-bab [[_ b a]]
  (list b a b))

(defn all-triplets [x]
  (apply concat (map #(partition 3 1 %) x))) 


(defn supports-ssl? [s]
  (let [[ips hypernets] (remove-brackets s)
        abas (into #{} (filter aba? (all-triplets ips)))
        babs (into #{} (filter aba? (all-triplets hypernets)))]
    (some babs (map aba-to-bab abas))))

(comment
  (map (comp some? supports-ssl?) 
       ["aba[bab]xyz"
        "xyx[xyx]xyx"
        "aaa[kek]eke"
        "zazbz[bzb]cdb"]))



(comment  
  (time 
    (transduce
      (comp
        (map supports-ssl?)
        (filter some?)
        (map bool-to-int))
      +
      (str/split-lines data-string)))

  (time (count (filter some? (map supports-ssl? (str/split-lines data-string))))))


;(defonce ans2 (count (filter some? (map supports-ssl? (str/split-lines data-string)))))
(defonce ans2 (transduce
                (comp
                  (map supports-ssl?)
                  (filter some?)
                  (map bool-to-int))
                +
                (str/split-lines data-string)))
(println "Answer2:" ans2)

