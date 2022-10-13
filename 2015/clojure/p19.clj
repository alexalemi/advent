(ns p19
  (:require [clojure.string :as str]))

(def data-string (slurp "../input/19.txt"))
(def test-string "H => HO
H => OH
O => HH

HOH")

(defn split-chems
  "Split a long string into a vector of chemical symbols"
  [s]
  (loop [s s
         o []]
    (let [n (first s)
          nn (second s)]
      (cond
        (nil? n) o
        (LOWER nn) (recur (rest (rest s)) (conj o (keyword (str n nn))))
        :else (recur (rest s) (conj o (keyword (str n))))))))

(defn process-line [line]
    (let [[frm to] (str/split line #" => ")]
      [(keyword frm) (split-chems to)]))


(defn process [s]
  {:seed (split-chems (last (str/split-lines s)))
   :rules (->> (str/split-lines s)
              (drop-last 2)
              (map process-line)
              (into []))})

(def UPPER (set "ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
(def LOWER (set "abcdefghijklmnopqrstuvwxyz"))




(def data (process data-string))
(def test-data (process test-string))

(defn mutate [data]
  (let [{state :seed rules :rules} data]
    (loop [left []
           loc (first state)
           right (rest state)
           compounds #{}]
       (if loc
        (recur
          (conj left loc)
          (first right)
          (rest right)
          (if-let [rules (filter #(= loc (first %)) rules)]
            (reduce (fn [compounds [_ to]]
                      (conj compounds (vec (concat left to right))))
                    compounds rules)
            compounds))
        compounds))))

(comment
  (count (mutate test-data)))

(defonce mutations (mutate data))
(def ans1 (count mutations))
(println "Answer1:" ans1)


(def test-string-2 "e => H
e => O
H => HO
H => OH
O => HH

e")

(def test-data-2 (process test-string-2))

(defn increment-time [data]
  (update data :time (fnil inc 0)))

(defn step [data]
  (-> data
      increment-time))


(defn ready [data]
  (-> data
      (assoc :seeds #{[:e]})
      (dissoc :seed)))

(comment
  (let [data (ready test-data-2)]))

(defn step [data]
   (let [{rules :rules seeds :seeds} data]
    (-> data
        (assoc :seeds (reduce (fn [seeds seed] (into seeds (mutate {:seed seed :rules rules})))
                            #{} seeds))
        increment-time)))

(defn movie [data] (iterate step data))

(defn hunt [data target]
   (:time (first (filter #((:seeds %) target) (movie data)))))

(comment
  (hunt (ready test-data-2) [:H :O :H])
  (hunt (ready test-data-2) [:H :O :H :O :H :O]))

(comment
  (defonce ans2 (hunt (ready data) (:seed data)))
  (println "Answer2:" ans2))
