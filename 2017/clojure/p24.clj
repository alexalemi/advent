(ns advent24
  (:require
   [clojure.test :as test]
   [clojure.string :as str]))


(def data-string (slurp "../input/24.txt"))
(def test-string "0/2
2/2
2/3
3/4
3/5
0/1
10/1
9/10")

(defn splitter [s]
  (mapv read-string (str/split s #"/")))

(defn process [s]
  (->> (str/split-lines s)
       (map splitter)
       set))

(def data (process data-string))
(def test-data (process test-string))

(defn subtract [s vals]
  (if (empty? vals) s
      (recur (disj s (first vals)) (rest vals))))

(defn score [path]
  (reduce + (flatten path)))

(defn next-link [end]
  (fn [x]
    (let [[a b] x]
     (cond
       (= a end) [x b]
       (= b end) [x a]
       :else nil))))

(defn longest-ends [data]
  (loop [partials [{:end 0 :path ()}]
         longest {0 0}]
    (let [front (first partials)
          {:keys [end path]} front
          best-so-far (get longest end 0)
          score-so-far (score path)
          available (subtract data path)
          links (filter
                 (fn [[piece end]] (> (+ score-so-far (apply + piece)) (get longest end 0)))
                 (filter some? (map (next-link end) available)))]
      ; (println "longest = " longest "num partials= " (count partials))
      (cond
        (empty? partials)
        longest

        (empty? links)
        (recur (rest partials)
               (assoc longest
                      end
                      (max best-so-far (score path))))

        :else
        (recur
         (concat (rest partials)
                 (for [[next end] links]
                   {:end end :path (conj path next)}))
         longest)))))

(defn max-depth [data]
  (fn inner-max-depth [front]
    (let [{:keys [end path]} front
          score-so-far (score path)
          available (subtract data path)
          links (filter some? (map (next-link end) available))]
      (if
        (empty? links) score-so-far
        (reduce max (for [[next end] links]
                         (inner-max-depth {:end end :path (conj path next)})))))))

(defn part-1 [data]
  (reduce max (vals (longest-ends data))))

(part-1 test-data)

(test/deftest test-part-1
  (test/is (= (part-1 test-data) 31)))

; (time (def ans1 (part-1 data)))
; (println)
; (println "Answer1:" ans1)

(test/run-tests)
