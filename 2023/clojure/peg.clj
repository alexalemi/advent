;; # PEG
;; Try to implement the [janet language peg parser](https://janet-lang.org/docs/peg.html)
;; in clojure.
;;
;; There is a nice [blog post](https://bakpakin.com/writing/how-janets-peg-works.html) detailing
;; the basic construction of the peg module.

(ns peg
  (:require [clojure.string :as str]
            [clojure.test :as test]))


(defn peg-type [peg _] (type peg))

(defmulti match peg-type)

(defmethod match String
  [peg text]
  (when (str/starts-with? text peg) (count peg)))

(defmethod match :default
  [peg text]
  (println "DEFUALT"))

(comment
  (match `(+ "foo" "bar") "foobar"))

(test/deftest default-tests
  (test/is (= (match '"foo" "foobar") 3))
  (test/is (= (match '"foobar" "foobar") 6))
  (test/is (= (match '"bar" "foobar") nil)))

(defn leading-symbol [peg _]
  (when (symbol? (first peg))
    (name (first peg))))

(defmulti match-seq leading-symbol)

(defmethod match clojure.lang.Cons
  [peg text]
  (match-seq peg text))

(defmethod match-seq "!"
  [[_ x] text]
  (when-not (match x text) 0))

(test/deftest not-tests
  (test/is (= (match `(! "foo") "foobar") nil))
  (test/is (= (match `(! "bar") "foobar") 0)))

(defmethod match-seq "+"
  [[_ & xs] text]
  (some (fn [x] (match x text)) xs))

(test/deftest +-tests
  (test/are [x y] (= (match `(+ "foo" "bar") x) y)
    "qux" nil
    "foobar" 3
    "foofoo" 3
    "bartop" 3)
  (test/are [x y] (= (match `(+ "foo" "bar" "qux") x) y)
    "qux" 3
    "foobar" 3
    "foofoo" 3
    "bartop" 3
    "quxxy" 3
    "qux" 3
    "spaz" nil
    "floofbar" nil))

(defmethod match-seq "*"
  [[_ & xs] text]
  (first
   (reduce
    (fn [[acc text] x]
      (if-let [n (match x text)]
        [(+ acc n) (subs text n)]
        (reduced nil)))
    [0 text]
    xs)))

(test/deftest *-tests
  (test/are [x y] (= (match `(* "foo" "bar") x) y)
    "qux" nil
    "foo" nil
    "bar" nil
    "foobar" 6)
  (test/are [x y] (= (match `(* "f" "oo" "bar") x) y)
    "qux" nil
    "foo" nil
    "bar" nil
    "foobar" 6))


(defmethod match-seq
  "set"
  [[_ chrs] text]
  (when ((set chrs) (first text))
    1))

(test/deftest set-tests
  (test/are [x y] (= (match `(set "abc") x) y)
    "aaa" 1
    "a" 1
    "bar" 1
    "foobar" nil
    "" nil))

(test/deftest iso-date
  (let [digit `(+ "0" (+ "1" (+ "2" (+ "3" (+ "4" (+ "5" (+ "6" (+ "7" (+ "8" "9")))))))))
        year `(* ~digit (* ~digit (* ~digit ~digit)))
        month `(* ~digit ~digit)
        day month
        iso-date `(* ~year (* "-" (* ~month (* "-" ~day))))]
    (test/are [x y] (= (match iso-date x) y)
      "2019-06-10" 10
      "0012-00-00" 10
      "201-06-10" nil
      "201--6-10" nil))
  (let [digit `(set "0123456789")
        year `(* ~digit ~digit  ~digit ~digit)
        month `(* ~digit ~digit)
        day month
        iso-date `(* ~year "-" ~month "-" ~day)]
    (test/are [x y] (= (match iso-date x) y)
      "2019-06-10" 10
      "0012-00-00" 10
      "201-06-10" nil
      "201--6-10" nil)))

(comment
  (match `(set "abcd") "afoo")

  (first `{:a :b})
  (type `(:a :b))

  (type `:green)

  (leading-symbol `{:a "abcde"} "foo"))

(comment
  (test/run-all-tests)

  (remove-all-methods match)
  (remove-all-methods match-seq)

  (methods match))
