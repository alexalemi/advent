;; # 🎄 Advent of Code 2022 - Day 23 - Unstable Diffusion
;; For today's puzzle we have to diffuse some elves according to some
;; strange rules.
(ns p23
  (:require [clojure.string :as str]
            [nextjournal.clerk :as clerk]
            [clojure.test :as test])
  (:import [java.awt.image BufferedImage]))

(def data-string (slurp "../input/23.txt"))

(def test-string "..............
..............
.......#......
.....###.#....
...#...#.#....
....#...##....
...#.###......
...##.#.##....
....#..#......
..............
..............
..............")

(defn indexed [xs] (map-indexed vector xs))

(defn process [s]
  {:turn 0
   :directions [:N :S :W :E]
   :elves
   (into #{}
         (for [[y line] (indexed (str/split-lines s))
               [x c] (indexed line)
               :when (= c \#)]
           [x y]))})

(def data (process data-string))
(def test-data (process test-string))

;; ## Visualization

(defn bounds [{elves :elves}]
  (reduce
   (fn [[[xlo xhi] [ylo yhi]] [x y]]
     [[(min xlo x) (max xhi x)]
      [(min ylo y) (max yhi y)]])
   [[##Inf ##-Inf]
    [##Inf ##-Inf]]
   elves))

(defn render-image
  "Render the puzzle as a raw image."
  ([Ω] (render-image Ω (bounds Ω) 1))
  ([Ω zoom] (render-image Ω (bounds Ω) zoom))
  ([Ω bounds zoom]
   (let [{:keys [elves]} Ω
         [[xlo xhi] [ylo yhi]] bounds
         width (* zoom (- (inc (inc xhi)) (dec xlo)))
         height (* zoom (- (inc yhi) ylo))
         img (BufferedImage. width height BufferedImage/TYPE_3BYTE_BGR)]
     (doseq [xp (range width)
             yp (range height)]
       (let [x (+ (quot xp zoom) (dec xlo))
             y (- yhi (+ (quot yp zoom) ylo))
             loc [x y]]
         (.setRGB img xp (- (dec height) yp)
                  (cond
                    (elves loc) (.getRGB (java.awt.Color. 42 194 42))
                    :else (.getRGB java.awt.Color/WHITE)))))
     img)))

(defn render-cell
  "Render a single square."
  [Ω loc]
  (let [{:keys [elves]} Ω]
    [:div.inline-block
     {:style {:width 16 :height 16
              :background-color (cond
                                  (elves loc) "green"
                                  :else "white")
              :border "solid 1px black"}}]))

(defn render
  "Custom clerk rendering function for a board."
  [Ω]
  (letfn [(box [val] [:div.flex.flex-col [:div {:style {:width 16 :height 16 :font-size "0.5em"}} val]])]
    (let [[[xlo xhi] [ylo yhi]] (bounds Ω)]
      (clerk/html
       [:div
        (into [:div.flex.inline-flex
               (into [:div.flex.flex-col (box "") (for [y (range ylo (inc yhi))] (box y))])]
              (for [x (range xlo (inc xhi))]
                (into [:div.flex.flex-col (box x)]
                      (for [y (range ylo (inc yhi))]
                        (render-cell Ω [x y])))))
        [:div "turn: " (:turn Ω)  " directions: " (apply str (:directions Ω))]]))))

(render-image data 5)

(render test-data)

;; ## Logic

(defn north [[x y]] [x (dec y)])
(defn south [[x y]] [x (inc y)])
(defn west [[x y]] [(dec x) y])
(defn east [[x y]] [(inc x) y])
(def northeast (comp north east))
(def northwest (comp north west))
(def southwest (comp south west))
(def southeast (comp south east))

(def look-north (juxt north northwest northeast))
(def look-east (juxt east northeast southeast))
(def look-south (juxt south southeast southwest))
(def look-west (juxt west southwest northwest))
(def direction->look {:N look-north :S look-south :W look-west :E look-east})

(def neighbors (juxt north south east west northwest northeast southwest southeast))

(defn propose [{directions :directions elves :elves}]
  (into {}
        (for [elf elves]
          (if
           ;; If no-one around, do nothing
           (not-any? elves (neighbors elf))  [elf nil]
           [elf (ffirst (filter (partial not-any? elves) (for [dir directions] ((direction->look dir) elf))))]))))

#_(def small-test (process ".....
..##.
..#..
.....
..##.
....."))

(defn resolve [{elves :elves} proposal]
  (let [counts (frequencies (vals proposal))]
    (into #{}
          (for [elf elves]
            (if-let [move (proposal elf)]
              (if (= 1 (counts move)) move elf) elf)))))

#_(let [proposal (propose small-test)]
    (resolve small-test proposal))

(defn rotate [xs] (concat (rest xs) [(first xs)]))

(defn round [state]
  (let [{:keys [elves turn directions]} state]
    (let [proposal (propose state)
          new-elves (resolve state proposal)]
      (-> state
          (update :turn inc)
          (update :directions rotate)
          (assoc :elves new-elves)))))

(defn fixed-point [seq]
  (reduce #(if (= (:elves %1) (:elves %2)) (reduced %1) %2) seq))

(defn complete [state]
  (fixed-point (iterate round state)))

(defn empty-ground [state]
  (let [[[xlo xhi] [ylo yhi]] (bounds state)]
    (- (* (inc (- yhi ylo)) (inc (- xhi xlo)))
       (count (:elves state)))))

(defn part-1 [state]
  (empty-ground (nth (iterate round state) 10)))

(test/deftest test-part-1
  (test/is (= 110 (part-1 test-data))))

(def ans1 (part-1 data))

;; ## Part 2

(defn part-2 [state]
  (inc (:turn (complete state))))

(test/deftest test-part-2
  (test/is (= 20 (part-2 test-data))))

(def ans2 (part-2 data))

;; ## Main

(defn -test [& _]
  (test/run-tests 'p23))

(defn -main [& _]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
