(ns p12
 (:require [clojure.data.json :as json]
           [clojure.walk :as w]))

(def data-string (slurp "../input/12.txt"))

(defonce data (json/read-str data-string))

(comment
  (json/read-str "[1,{\"c\":\"red\",\"b\":2},3]"))


(defn total [s]
  (let [counter (atom 0)]
    (w/postwalk (fn [x] (if (number? x) (swap! counter + x) x)) s)
    @counter))

(defonce ans1 (total data))
(println "Answer1:" ans1)

(defn red? [s]
  (and (map? s) (contains? (set (vals s)) "red")))

(defn red-total [s]
  (total (w/postwalk (fn [x] (if (red? x) nil x)) s)))

(defonce ans2 (red-total data))
(println "Answer2:" ans2)

(comment
  (red-total [:b 3 {:a [1 2 3]} {:a "red" :b [1 2 3]}])

  (vals {:a :b :c :d})
  (vals [1 2 3])

  (w/postwalk-demo [1 2 3])
  (w/postwalk-demo [[[3]]])
  (w/postwalk-demo {:a [-1 1]})

  (let [counter (atom 0)]
    (w/postwalk (fn [x] (if (number? x) (swap! counter + x) x)) {:a [-1 10]})
    @counter))
