(ns p17
  (:require [util]
            [clojure.string :as str]
            [util :as util]))

(def data-string (str/trim (slurp "../input/17.txt")))

(def TARGET [3 3])

(def state
  {:loc [0 0]
   :passcode "hijkl"
   :path ""})

(def OPEN #{\b \c \d \e \f})

(defn motions [{passcode :passcode path :path}]
  (map first
       (filter #(OPEN (second %))
               (apply mapv vector ["UDLR" (util/md5 (str passcode path))]))))

(defn new-loc [move [x y]]
  (case move
    \U [x (dec y)]
    \D [x (inc y)]
    \L [(dec x) y]
    \R [(inc x) y]))

(defn valid? [{[x y] :loc}]
  (and
   (<= 0 x 3)
   (<= 0 y 3)))

(defn neighbors [state]
    (filter valid?
     (for [move (motions state)]
       (-> state
          (update :loc (partial new-loc move))
          (update :path str move)))))

(comment
  (let [new-state (first (neighbors state))]
    (neighbors new-state)))

(defn manhattan [[x1 y1] [x2 y2]]
  (+ (abs (- x2 x1)) (abs (- y2 y1))))

(defn shortest-path [passcode]
  (let [state {:loc [0 0] :passcode passcode :path ""}]
    (:path
     (last
      (util/a-star
       state
       #(= (:loc %) TARGET)
       (constantly (constantly 1))
       neighbors
       #(manhattan TARGET (:loc %)))))))

(comment
  (shortest-path "ihgpwlah")
  (shortest-path "kglvqrro")
  (shortest-path "ulqzkmiv"))

(defonce ans1 (shortest-path data-string))
(println "Answer1:" ans1)

(defn longest-path [passcode]
  (let [state {:loc [0 0] :passcode passcode :path ""}
        goal? (fn [state] (= (:loc state) TARGET))]
    (loop [frontier [state] n 0]
      (if-let [pos (peek frontier)]
        (if (goal? pos)
          (recur (pop frontier) (max n (count (:path pos))))
          (recur (into (pop frontier) (neighbors pos)) n))
        n))))


(comment
  (longest-path "ihgpwlah")
  (longest-path "kglvqrro")
  (longest-path "ulqzkmiv"))

(defonce ans2 (longest-path data-string))
(println "Answer2:" ans2)
