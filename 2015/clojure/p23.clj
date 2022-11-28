(ns p23
  (:require [clojure.string :as str]))

(def data-string (slurp "../input/23.txt"))

(defn keywordize [x]
  (if (symbol? x) (keyword x) x))

(defn parse-line [line]
    (map keywordize (read-string (str "(" line ")"))))

(def data (mapv parse-line (str/split-lines data-string)))

(def state {:registers {:a 0 :b 0} :inst data :loc 0})


(defn step [{:keys [registers inst loc] :as state}]
  (if-let [[op x y] (get inst loc)]
    (let [state (update state :loc inc)]
      (case op
         :hlf (update-in state [:registers x] quot 2)
         :tpl (update-in state [:registers x] * 3)
         :inc (update-in state [:registers x] inc)
         :jmp (assoc state :loc (+ loc x))
         :jie (assoc state :loc (if (even? (registers x))
                                  (+ loc y)
                                  (:loc state)))
         :jio (assoc state :loc (if (= 1 (registers x))
                                  (+ loc y)
                                  (:loc state)))))
   (assoc state :done true)))


(defn final-b [state]
  ((comp :b :registers) (first (drop-while (complement :done) (iterate step state)))))

(defonce ans1 (final-b state))
(println "Answer1:" ans1)

(defonce ans2 (final-b (assoc-in state [:registers :a] 1)))
(println "Answer2:" ans2)


(comment
  (let [final (first (drop-while (complement :done) (iterate step state)))]
    final))
