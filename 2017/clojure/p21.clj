(ns advent21
  (:require
   [clojure.string :as str]))

(def data-string (slurp "../input/21.txt"))

(def data ".#./..#/###")

(defn enumerate [coll]
  (zipmap (range) coll))


(defn board-width [s]
  (count (first (str/split s #"/"))))

(defn board-height [s]
  (count (str/split s #"/")))

(defn board-dims [s]
  ((juxt board-width board-height) s))

(defn vec-add [a b]
  (let [[x1 y1] a
        [x2 y2] b]
    [(+ x1 x2) (+ y1 y2)]))

(defn str-to-board 
  ([line] 
   {:occupied (set (for [[y line] (enumerate (str/split line #"/"))
                         [x c] (enumerate line)
                         :when (= c \#)]
                      [x y]))
    :width (board-width line)
    :height (board-height line)}))

(defn board-to-str 
  ([s] (board-to-str s [0 0] ((juxt :width :height) s)))
  ([s dims] (board-to-str s [0 0] dims))
  ([s origin dims]
   (let [[w h] dims
         {occupied :occupied} s
         [x0 y0] origin]
      (str/join "/" (for [x (range x0 (+ x0 w))]
                       (apply str (for [y (range y0 (+ y0 h))]
                                     (if (occupied [x y]) "#" "."))))))))

(defn flip-x [board]
  (let [{:keys [occupied width]} board]
    (assoc board :occupied (set (map (fn [[x y]] [(- (dec width) x) y]) occupied)))))

(defn flip-y [board]
  (let [{:keys [occupied height]} board]
    (assoc board :occupied (set (map (fn [[x y]] [x (- (dec height) y)]) occupied)))))

(defn transpose [board]
  (let [{occupied :occupied} board]
    (assoc board :occupied (set (map (fn [[x y]] [y x]) occupied)))))

(defn eight-fold [board]
   (set [board
         (flip-x board)
         (flip-y board)
         (flip-x (flip-y board))
         (transpose board)
         (flip-x (transpose board))
         (flip-y (transpose board))
         (flip-x (flip-y (transpose board)))]))
                 
(def rules 
  (apply merge (for [line (str/split-lines data-string)]
                  (let [[a b] (str/split line #" => ")
                        dims (board-dims a)]
                    (into {} (for [tm (eight-fold (str-to-board a))] 
                                (let [t (board-to-str tm dims)]
                                   [t b])))))))


(defn breakup [data]
  (let [m (str-to-board data)
        {w :width} m
        {h :height} m]
    (if (even? w)
      (for [i (range 0 w 2)
            j (range 0 h 2)]
        (board-to-str m [i j] [2 2]))
      (for [i (range 0 w 3)
            j (range 0 h 3)]
        (board-to-str m [i j] [3 3])))))

(defn transform [boards]
  (map rules boards))
   

(defn join-horizontally [a b]
 (let [{occupied1 :occupied w1 :width} a
       {occupied2 :occupied w2 :width} b]
   (-> a
       (assoc :occupied (into occupied1 (map (partial vec-add [w1 0]) occupied2)))
       (update :width + w2))))

(defn join-vertically [a b]
 (let [{occupied1 :occupied h1 :height} a
       {occupied2 :occupied h2 :height} b]
   (-> a
       (assoc :occupied (into occupied1 (map (partial vec-add [0 h1]) occupied2)))
       (update :height + h2))))

(defn rejoin [xs]
  (let [n (int (Math/sqrt (count xs)))
        bs (map str-to-board xs)]
    (board-to-str 
      (reduce join-vertically 
              (map (partial reduce join-horizontally) (partition n bs))))))

(defn step [data]
  (-> data
      breakup
      transform
      rejoin))

(defn count-cells [s]
  ((frequencies (seq s)) \#))

(defn part-1 [data]
  (count-cells (nth (iterate step data) 5)))

(time (def ans1 (part-1 data)))
(println)
(println "Answer 1:" ans1)

(defn part-2 [data]
  (count-cells (nth (iterate step data) 18)))

(time (def ans2 (part-2 data)))
(println)
(println "Answer 2:" ans2)

