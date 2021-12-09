(ns advent17
  (:require
   [clojure.test :as test]))

(def test-data 3)
(def data (read-string (slurp "../input/17.txt")))

(defn shift [buffer shift]
  (let [n (count buffer)]
    (take n (drop shift (cycle buffer)))))

(defn make-stepper [data]
  (fn [buffer]
    (let [new (inc (first buffer))]
      (conj (shift buffer (inc data)) new))))

(def init (list 0))
(def step (make-stepper data))
(def test-step (make-stepper test-data))

(defn part-1 [step]
  (second (nth (iterate step init) 2017)))

(test/deftest test-part-1
  (test/is (= (part-1 test-step) 638)))

(time (def ans1 (part-1 step)))
(println)
(println "Answer 1:" ans1)

(defn after-zero [buffer]
  (second (drop-while (complement zero?) (cycle buffer))))

(defn fast-step [state]
  (let [{:keys [loc size secret]} state
        newloc (mod (+ loc data) size)]
    (-> state
        (assoc :loc (inc newloc))
        (assoc :size (inc size))
        (assoc :secret (if (zero? newloc) size secret)))))

(def init-state {:loc 0 :size 1 :secret 0})

(time (def ans2 (:secret (nth (iterate fast-step init-state) 50000000))))

(println)
(println "Answer 2:" ans2)

(test/run-tests)
