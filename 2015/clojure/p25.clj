(ns p25)

(def data [3010 3019])

(defn index [x y]
  (loop [x x y y n 1]
    (cond
      (> y 1) (recur (inc x) (dec y) (inc n))
      (> x 1) (recur 1 (dec x) (inc n))
      :else n)))


(def pk (apply index data))

(defn step [x]
  (rem (* 252533 x) 33554393))


(nth (iterate step 20151125) (dec (index 4 2)))


(defonce ans1 (nth (iterate step 20151125) (dec (apply index data))))
(println "Answer1: " ans1)
