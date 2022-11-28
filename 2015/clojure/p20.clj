(ns p20)

;; # Day 20
;; It looks like for this day we need to implement what is essentially a prime sieve.
;; Looking at [this blog post](https://cuddly-octo-palm-tree.com/posts/2021-12-05-sieve/) we can learn
;; how to create a decent sieve in clojure.

(def TOP (read-string (slurp "../input/20.txt")))
(def UPTO 1000000)

(defn sieve
  ([s] (sieve s {}))
  ([[x & xs] table]
   (if-let [factors (get table x)]
     (sieve xs (reduce (fn [t prime]
                         (update t (+ x prime) concat [prime]))
                       (dissoc table x)
                       factors))
     (cons x (lazy-seq (sieve xs (assoc table (* x x) [x])))))))

(def primes (sieve (drop 2 (range))))

;; Instead I'll go for a simpler approach, I'm just going to try to take some
;; fixed length sequence and run a reducer to populate a table of all
;; factors.

(defn factor-sum [n]
   (let [xs (range 1 n)
         t (into {} (for [x xs] {x 0}))]
     (letfn [(addin [t x]
               (reduce (fn [t y] (update t y + (* 10 x))) t (range x n x)))]
      (reduce addin t xs))))

(defonce factor-sums (factor-sum UPTO))

(defonce ans1 (first (drop-while #(< (factor-sums %) TOP) (range 1 UPTO))))
(println "Answer1:" ans1)

(defn factor-sum-2 [n]
   (let [xs (range 1 n)
         t (into {} (for [x xs] {x 0}))]
     (letfn [(addin [t x]
               (reduce (fn [t y] (update t y + (* 11 x))) t (take 50 (range x n x))))]
      (reduce addin t xs))))

(defonce factor-sums-2 (factor-sum-2 UPTO))
(def ans2 (first (drop-while #(< (factor-sums-2 %) TOP) (range 1 UPTO))))
(println "Answer2:" ans2)
