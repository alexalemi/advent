(ns p11
  (:require [clojure.string :as str]))

(def data-string "hxbxwxba")

(def alphabet "abcdefghijklmnopqrstuvwxyz")

(def succ (apply hash-map (flatten (partition 2 1 (str alphabet "a")))))
;; => {\a \b, \b \c, \c \d, \d \e, \e \f, \f \g, \g \h, \h \i, \i \j,
;;     \j \k, \k \l, \l \m, \m \n, \n \o, \o \p, \p \q, \q \r, \r \s,
;;     \s \t, \t \u, \u \v, \v \w, \w \x, \x \y, \y \z, \z \a}


(defn inc-string
  "Increment a lower-case string"
  ([s] (inc-string (reverse s) nil true))
  ([s out carry]
   (if-let [c (first s)]
     (if carry
       (recur (rest s) (conj out (succ c)) (if (= c \z) true false))
       (recur nil (apply conj out s) false))
     (apply str (if carry (conj out \a) out)))))


(defn straight? [s]
  (let [[a b c] s]
    (and (= b (succ a)) (= c (succ b)) (not= a \z) (not= b \z))))

(defn contains-straight? [s]
  (some straight? (partition 3 1 s)))

(defn good-letters? [s]
  (not (some #{\i \o \l} s)))

(defn contains-pairs? [s]
  (->> (partition 2 1 s)
       (filter #(= (first %) (second %)))
       set
       count
       (<= 2)))

(def password? (every-pred contains-straight? good-letters? contains-pairs?))

(defn next-password [s]
  (->> (iterate inc-string s)
       rest
       (filter password?)
       first))

(defonce ans1 (next-password data-string))
(println "Answer1:" ans1)

(defonce ans2 (next-password ans1))
(println "Answer2:" ans2)

(comment
  (inc-string ans1)

  (= "abcdffaa" (first (filter password? (iterate inc-string "abcdefgh"))))
  (= "ghjaabcc" (first (filter password? (iterate inc-string "ghijklmn"))))

  (apply conj nil "abc")

  (count (filter #(= (first %) (second %)) (partition 2 1 "hijklmmn")))
  (password? "abcaaabcdaa")

  (contains-straight? "abd")
  (contains-straight? "abc")
  (contains-straight? "hijklmmn")
  (good-letters? "hijklmn")

  (take 30 (iterate inc-string "a"))

  (conj nil \a)
  (conj (conj nil \a) \b))
