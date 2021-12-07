(ns advent11
  (:require
   [clojure.edn :as edn]
   [clojure.test :as test]))

(def data-string (slurp "../input/11.txt"))

(defn process [s]
  (map keyword (edn/read-string (str "[" data-string "]"))))

(def data (process data-string))

(def origin {:q 0 :r 0})

(defn step [loc dir]
  (case dir
    :n  (update loc :r dec)
    :nw (update loc :q dec)
    :sw (-> loc
            (update :q dec)
            (update :r inc))
    :s  (update loc :r inc)
    :se (update loc :q inc)
    :ne (-> loc
            (update :q inc)
            (update :r dec))))

(defn abs [x] (max x (- x)))

(defn distance [loc]
  (let [{:keys [q r]} loc
        s (- 0 q r)]
    (reduce max [(abs q) (abs r) (abs s)])))

(defn part-1 [data]
  (distance (reduce step origin data)))

(def ans1 (part-1 data))

(defn part-2 [data]
  (->> (reductions step origin data)
      (map distance)
      (reduce max)))

(def ans2 (part-2 data))
