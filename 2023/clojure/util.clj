(ns util
  (:require [clojure.data.priority-map :refer [priority-map]]))

(import 'java.security.MessageDigest
        'java.math.BigInteger)

(defn range-to [a b]
  (range a (inc b)))

(defn gcd [a b]
  (if (zero? b) a
      (recur b, (mod a b))))

(defn lcm [a b]
  (/ (* a b) (gcd a b)))

(defn seek
  "Returns the first time from coll for which (pred item) returns true.
   Returns nil if no such item is present or the not-found value if supplied."
  ([pred coll] (seek pred coll nil))
  ([pred coll not-found]
   (reduce (fn [_ x] (if (pred x) (reduced x) not-found)) not-found coll)))

(defn md5 [^String s]
  (let [algorithm (MessageDigest/getInstance "MD5")
        raw (.digest algorithm (.getBytes s))]
    (format "%032x" (BigInteger. 1 raw))))

(defn fixed-point [seq]
  (reduce #(if (= %1 %2) (reduced %1) %2) seq))

(def QUEUE clojure.lang.PersistentQueue/EMPTY)

(defn queue? [x] (instance? clojure.lang.PersistentQueue x))

(defn queue
  ([] (QUEUE))
  ([coll] (reduce conj QUEUE coll)))

(deftype Bag [^clojure.lang.IPersistentMap m
              ^long n]
  clojure.lang.IPersistentSet
  (get [_ k]
    (if (contains? m k) k nil))
  (contains [_ k]
    (contains? m k))
  (disjoin [_ k]
    (Bag. (if (= 1 (m k)) (dissoc m k) (update m k dec))
          (dec n)))

  clojure.lang.IPersistentCollection
  (count [_]
    n)
  (empty [_]
    (Bag. (.empty m) 0))
  (cons [_ k]
    (Bag. (assoc m k (inc (get m k 0)))
          (inc n)))
  (equiv [_ o]
    (and (isa? (class o) Bag)
         (= n (.n ^Bag o))
         (.equiv m (.m ^Bag o))))

  clojure.lang.Seqable
  (seq [_] (mapcat repeat (vals m) (keys m))))

(defn bag
  [& keys]
  (Bag. (frequencies keys)
        (count keys)))

(defn distinct-by
  "Returns a stateful transducer that removes elements by calling f on each step as a uniqueness key.
   Returns a lazy sequence when provided with a collection."
  ([f]
   (fn [rf]
     (let [seen (volatile! #{})]
       (fn
         ([] (rf))
         ([result] (rf result))
         ([result input]
          (let [v (f input)]
            (if (contains? @seen v)
              result
              (do (vswap! seen conj v)
                  (rf result input)))))))))
  ([f xs]
   (sequence (distinct-by f) xs)))

(defn reconstruct-path
  "Knowing the parent of each node, construct the full path."
  [came-from current]
  (loop [current current
         path (list current)]
    (let [prev (came-from current)]
      (if prev
        (recur prev (conj path prev))
        path))))

(defn a-star
  "General A-star algorithm."
  [start goal? cost neighbors heuristic]
  (loop [frontier (priority-map start (heuristic start))
         current :start
         came-from (transient {})
         best-score (transient {start 0})
         neighs '()]
    (let [neigh (first neighs)]
      ;; (println "current=" current " frontier=" frontier " neighs=" neighs " neigh=" neigh " best-score=" best-score " came-from=" came-from)
      (cond
        (and (empty? frontier) (empty? neighs)) :failure
        (and (not= current :start) (goal? current)) (reconstruct-path (persistent! came-from) current)
        (empty? neighs)
        (let [[next _] (peek frontier)]
          (recur
           (pop frontier)
           next
           came-from
           best-score
           (neighbors next)))
        :else
        (let [score (best-score current)
              neigh-cost ((cost current) neigh)
              tentative-score (+ neigh-cost score)
              prev-score (get best-score neigh ##Inf)
              f-score (+ tentative-score (heuristic neigh))]
          ;; (println "neigh-cost=" neigh-cost " tentative-score=" tentative-score " prev-score=" prev-score " f-score=" f-score)
          (if (< tentative-score prev-score)
            (recur
             (assoc frontier neigh f-score)
             current
             (assoc! came-from neigh current)
             (assoc! best-score neigh tentative-score)
             (rest neighs))
            (recur
             frontier
             current
             came-from
             best-score
             (rest neighs))))))))

(comment
  (a-star
   :a
   #(= % :d)
   {:a {:b 1 :f 10}
    :b {:a 1 :c 1 :e 10}
    :c {:b 1 :d 1}
    :f {:a 10 :e 1}
    :e {:f 1 :d 1 :b 10}
    :d {:e 1 :c 1}}
   {:a #{:b :f}
    :b #{:a :e :c}
    :c #{:b :d}
    :d #{:c :e}
    :e #{:d :b :f}
    :f #{:a :e}}
   (constantly 1)))
