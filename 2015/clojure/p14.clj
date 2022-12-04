(ns p14
  (:require [clojure.string :as str]))

(def data-string (slurp "../input/14.txt"))

(def test-string "Comet can fly 14 km/s for 10 seconds, but then must rest for 127 seconds.
Dancer can fly 16 km/s for 11 seconds, but then must rest for 162 seconds.")



(defn process-line [line]
    (let [[_ name speed active sleep] (re-matches #"(\w+) can fly (\d+) km/s for (\d+) seconds, but then must rest for (\d+) seconds." line)]
      {:name name :speed (read-string speed) :active (read-string active) :sleep (read-string sleep)}))

(def data (map process-line (str/split-lines data-string)))
(def test-data (map process-line (str/split-lines test-string)))

(defn distance [t reindeer]
 (let [{:keys [active sleep speed]} reindeer
       period (+ active sleep)
       periods (quot t period)
       remainder (rem t period)]
   (* speed (+ (* periods active) (min active remainder)))))

(defn furthest [data t]
  (apply max (map (partial distance t) data)))

(def TIME 2503)

(defonce ans1 (furthest data TIME))
(println "Answer1:" ans1)

(defn winners [data t]
   (let [scores (into {} (for [x data] {(:name x) (distance t x)}))
         max-score (apply max (vals scores))]
      (reduce-kv (fn [x k v] (if (= v max-score) (conj x k) x)) #{} scores)))

(defn award-points [leaderboard winners]
    (reduce (fn [o x] (update o x (fnil inc 0))) leaderboard winners))

(defn part-2 [data t]
  (second (apply max-key second (reduce award-points {} (map (partial winners data) (range 1 (inc t)))))))

(defonce ans2 (part-2 data TIME))
(println "Answer2:" ans2)

(comment
  (max-key (partial distance 1) test-data)
  (award-points {} (winners test-data 1000))

  (part-2 test-data 1000)

  (second (apply max-key second (reduce award-points {} (map (partial winners test-data) (range 1 1001)))))

  (let [leaderboard {"Comet" 0}
        winners #{"Comet" "Vixen"}]
     (update leaderboard "Comet" (fnil inc 0))

     (reduce (fn [o x] (update o x (fnil inc 0))) leaderboard winners)))
