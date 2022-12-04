(ns p19
  (:require [clojure.string :as str]))

(def data-string (slurp "../input/19.txt"))
(def test-string "H => HO
H => OH
O => HH

HOH")

(def UPPER (set "ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
(def LOWER (set "abcdefghijklmnopqrstuvwxyz"))

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

(comment
  test-data-2)

(defn increment-time [data]
  (update data :time (fnil inc 0)))


(defn ready [data]
  (-> data
      (assoc :seeds #{[:e]})
      (dissoc :seed)))

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
  (defonce ans2-original (hunt (ready data) (:seed data)))
  (println "Answer2:" ans2-original))


; # Part 2 part deux
; Let's start this over, this time let's just do a greedy string subsitution thing.

(defn process-line-simple [line]
    (let [[frm to] (str/split line #" => ")]
      [to frm]))

(defn process-simple [s]
  {:seed (last (str/split-lines s))
   :rules (->> (str/split-lines s)
              (drop-last 2)
              (map process-line-simple)
              (into [])
              (sort-by (comp count split-chems first) >))})



(def state
  (-> (process-simple data-string)
      (assoc :alive true)
      (assoc :step 0)))


(defn round [state]
  (let [{:keys [rules seed]} state]
   (if-let [new (first (drop-while #(= seed %) (map #(str/replace-first seed (re-pattern (first %)) (second %)) rules)))]
     (-> state
         (assoc :seed new)
         (update :step inc))
     (assoc state :alive false))))

(defn greedy-round [state]
  (let [{:keys [rules seed]} state
        cands (filter #(and % (not= seed %)) (map #(str/replace-first seed (re-pattern (first %)) (second %)) rules))]
    (if (seq cands)
      (-> state
          (assoc :seed (apply min-key count cands))
          (update :step inc))
      (assoc state :alive false))))


(def ans2 (:step (first (drop-while :alive (iterate greedy-round state)))))
(println "Answer2:" ans2)

(comment
  state

  (let [state (assoc state :alive true)]
    (first (drop-while :alive (iterate greedy-round state))))

  (let [state {:rules (sort-by (comp count first) > [["HH" "O"] ["OH" "H"] ["HO" "H"] ["H" "e"] ["O" "e"]])
               :seed "HOHOHO"
               :alive true
               :step 0}]
    (first (drop-while :alive (iterate greedy-round state)))))
    ; (greedy-round state)))
