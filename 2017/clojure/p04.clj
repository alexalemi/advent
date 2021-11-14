(ns advent04
  (:require
   [clojure.test :as test]
   [clojure.string :as string]))


(def data (string/split-lines (slurp "../input/04.txt")))

(comment "A valid passphrase must not contain any duplicate words.
")

(defn valid-passphrase
  "Doesn't contain repeated words."
  [phrase]
  (let [words (string/split (string/trim phrase) #" ")]
    (= (count (into #{} words)) (count words))))

(test/deftest test-part-1
  (test/are [x y] (= (valid-passphrase x) y)
    "aa bb cc dd ee" true
    "aa bb cc dd aa" false
    "aa bb cc dd aaa" true))

(def ans1 (reduce + (map #(if % 1 0) (map valid-passphrase data))))

(defn valid-passphrase-2
  "Doesn't contain anagrams."
  [phrase]
  (let [words (string/split (string/trim phrase) #" ")]
    (= (count (into #{} (map #(into #{} %) words))) (count words))))

(test/deftest test-part-2
  (test/are [x y] (= (valid-passphrase-2 x) y)
    "abcde fghij" true
    "abcde xyz ecdab" false
    "a ab abc abd abf abj" true
    "iiii oiii ooii oooi oooo" true
    "oiii ioii iioi iiio" false))

(def ans2 (reduce + (map #(if % 1 0) (map valid-passphrase-2 data))))

(test/run-tests)

(println)
(println "Answer1:", ans1)
(println "Answer2:", ans2)
