(ns advent20
  (:require
   [clojure.test :as test]
   [clojure.string :as str]))

(def data-string (slurp "../input/20.txt"))


(defn process-line [line]
  (let [[_ px py pz vx vy vz ax ay az] (re-matches #"p=<(-?\d+),(-?\d+),(-?\d+)>, v=<(-?\d+),(-?\d+),(-?\d+)>, a=<(-?\d+),(-?\d+),(-?\d+)>" line)]
    {:p (mapv read-string [px py pz]) :v (mapv read-string [vx vy vz]) :a (mapv read-string [ax ay az])}))

(defn process [s]
  (mapv process-line (str/split-lines s)))

(def data (process data-string))

(defn step [particle]
  (let [{:keys [p v a]} particle]
    (-> particle
        (assoc :v (mapv + v a))
        (assoc :p (mapv + p (:v particle))))))

(defn abs [x] (max x (- x)))

(defn manhattan [vec]
  (reduce + (map abs vec)))

(defn enumerate [coll]
  (zipmap (range) coll))

(defn map-val [f coll]
  (map (fn [[k v]] [k (f v)]) coll))

(time (def ans1
        (first (apply min-key (comp manhattan :p second) (enumerate (nth (iterate #(map step %) data) 1000))))))

(println)
(println "Answer 1:" ans1)

(defn step-2 [data]
  (let [data (mapv step data)
        badposes (into #{} (map key (filter #(> (val %) 1) (frequencies (map :p data)))))]
     (remove (comp badposes :p) data)))

(count data)

(def test-string "p=<-6,0,0>, v=<3,0,0>, a=<0,0,0>
p=<-4,0,0>, v=<2,0,0>, a=<0,0,0>
p=<-2,0,0>, v=<1,0,0>, a=<0,0,0>
p=<3,0,0>, v=<-1,0,0>, a=<0,0,0>")

(def test-data (process test-string))


(time (def ans2 (count (nth (iterate step-2 data) 100000))))
(println)
(println "Answer 2:" ans2)


