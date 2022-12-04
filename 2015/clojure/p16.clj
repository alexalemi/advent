(ns p16
  (:require [clojure.string :as str]))


(def data-string (str/trim (slurp "../input/16.txt")))

(defn process-piece [piece]
  (let [[prop n] (str/split piece #": ")]
    {(keyword prop) (read-string n)}))

(defn process-line [line]
    ;; => "Sue 1: cars: 9, akitas: 3, goldfish: 0"
    (let [[header n] (re-find #"Sue (\d+): " line)
          n (read-string n)
          line (subs line (count header))
          pieces (str/split line #", ")]
      {n (into {} (map process-piece pieces))}))

(defn filter-keys [pred x]
  (reduce-kv (fn [m k v] (if (pred k) (assoc m k v) m)) {} x))

(defn map-vals [f x]
  (reduce-kv (fn [m k v] (assoc m k (f v))) {} x))

;(def data (into {}))
(def data (into {} (map process-line (str/split-lines data-string))))

(def template {:children 3 :cats 7 :samoyeds 2 :pomeranians 3 :akitas 0 :vizslas 0 :goldfish 5 :trees 3 :cars 2 :perfumes 1})

(defn right-sue? [sue]
  (= sue (filter-keys sue template)))

(defonce ans1 (ffirst (filter (fn [[_ v]] (right-sue? v)) data)))
(println "Answer1:" ans1)

(defn right-sue?-2 [sue]
  (let [template (filter-keys sue template)
        eq-sue (filter-keys (complement #{:cats :trees :pomeranians :goldfish}) sue)
        eq-template (filter-keys eq-sue template)]
    (and
     (if (:cats sue)
       (> (:cats sue) (:cats template))
       true)
     (if (:trees sue)
       (> (:trees sue) (:trees template))
       true)
     (if (:pomeranians sue)
       (< (:pomeranians sue) (:pomeranians template))
       true)
     (if (:goldfish sue)
       (< (:goldfish sue) (:goldfish template))
       true)
     (= eq-sue eq-template))))

(defonce ans2 (ffirst (filter (fn [[_ v]] (right-sue?-2 v)) data)))
(println "Answer2:" ans2)

(comment
  (filter-keys template (val (first data)))
  (second data)

  (right-sue? (val (first data)))

  (first (filter (fn [[_ v]] (right-sue? v)) data))
  (first (filter (fn [[_ v]] (right-sue? v)) data)))
