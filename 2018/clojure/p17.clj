;; # 🎄 Advent of Code 2018 - Day 17
;; In this challenge we are dropping water
;; into a bunch of clay buckets and have to figure
;; out how the water flows.
(ns p17
  (:require [clojure.string :as str]
            [nextjournal.clerk :as clerk]
            [clojure.set :as set])
  (:import [java.awt.image BufferedImage]))


;; ## Input Processing

(defn s->ints [s]
  (mapv read-string (re-seq #"\d+" s)))

(defn process-one [s]
  (let [[one many] (str/split s #", ")]
    {(keyword (str (first one)))
     (first (s->ints one))
     (keyword (str (first many)))
     (s->ints many)}))

(defn process [s]
  (->> s
       (str/split-lines)
       (map process-one)))

(def data (process (slurp "../input/17.txt")))

(def test-data (process "x=495, y=2..7
y=7, x=495..501
x=501, y=3..7
x=498, y=2..4
x=506, y=1..2
x=498, y=10..13
x=504, y=10..13
y=13, x=498..504"))

(defn horizontal-line
  "All of the coordinates between two horizontal points."
  [[x1 y1] [x2 y2]]
  (assert (= y1 y2) "Can only form a line on between points on the same height.")
  (for [x (range x1 (inc x2))] [x y1]))

(defn vertical-line
  "All of the coordinates between two vertical points."
  [[x1 y1] [x2 y2]]
  (assert (= x1 x2) "Can only form a line on between points at the same x.")
  (for [y (range y1 (inc y2))] [x1 y]))

(defn line [{x :x y :y}]
  (if (coll? x)
    (horizontal-line [(first x) y] [(second x) y])
    (vertical-line [x (first y)] [x (second y)])))

(defn data->state [data]
  (let [clay (reduce into #{} (map line data))]
    {:clay clay
     :still #{}
     :flowing #{}
     :spawned #{}
     :sources #{[500 0]}
     :bounds [[(dec (reduce min (map first clay)))
               (inc (reduce max (map first clay)))]
              [(reduce min (map second clay))
               (reduce max (map second clay))]]}))

(def test-Ω (data->state test-data))
(def Ω (data->state data))

;; ## Visualization
;; Build a nice visualizer though its probably too expensive to do the full problem input.

(defn in-bounds? [[ylo yhi] [x y]] (<= ylo y yhi))

(defn total-wet [Ω]
  (count (filter (partial in-bounds? (second (:bounds Ω))) (set/union (:flowing Ω) (:still Ω)))))

(defn render-cell
  "Render a single square."
  [Ω loc]
  (let [{:keys [clay still flowing sources spawn blocked]} Ω]
    [:div.inline-block
     {:style {:width 16 :height 16
              :background-color (cond
                                  (= spawn loc) "yellow"
                                  (= blocked loc) "green"
                                  (sources loc) "blue"
                                  (clay loc) "brown"
                                  (still loc) "darkblue"
                                  (flowing loc) "steelblue"
                                  :else "white")
              :border "solid 1px black"}}]))


(defn render
  "Custom clerk rendering function for a board."
  [Ω]
  (letfn [(box [val] [:div.flex.flex-col [:div {:style {:width 16 :height 16 :font-size "0.5em"}} val]])]
   (let [[[xlo xhi] [ylo yhi]] (:bounds Ω)]
     (clerk/html
      [:div
       (into [:div.flex.inline-flex
              (into [:div.flex.flex-col (box "") (for [y (range ylo (inc yhi))] (box y))])]
           (for [x (range xlo (inc xhi))]
             (into [:div.flex.flex-col (box x)]
              (for [y (range ylo (inc yhi))]
               (render-cell Ω [x y])))))
       [:div "wet: " (total-wet Ω)]]))))

(render test-Ω)

;; ## Core Logic
;; Let's start to plan out how we'll actually solve the problem now that we've done
;; some minimal processing of the input.

;; Let's say there are some different types of water,
;; there are :flowing squares where water has moved through
;; and there are :still squares that behave as though they are clay
;; There is obviously the :clay squares.  Now in terms of states,
;; we'll have :sources which let water fall down from them,
;; and we'll have :spawns where the :sources first contact ground.


(defn solid? [Ω]
  (some-fn (:clay Ω) (:still Ω)))
(defn not-solid? [Ω] (complement (solid? Ω)))

(defn left [loc] (update loc 0 dec))
(defn right [loc] (update loc 0 inc))
(defn below [loc] (update loc 1 inc))
(defn above [loc] (update loc 1 dec))

;; TODO for some reason this adding of the thing to sources doesn't seem to be sticking.

(defn spread
  "Take the spawn and move whichways until we hit clay or there is nothing below us."
  [which Ω]
  ;; If there isn't a spawn, do nothing
  (if-let [spawn (:spawn Ω)]
    (let [Ω (update Ω :spawned conj (above spawn))
          Ω (update Ω :sources conj spawn)] #_(if ((:flowing Ω) (above spawn)
                                                              (update Ω :sources conj (above spawn))
                                                              Ω))
     (loop [Ω Ω]
       (if-let [spawn (:spawn Ω)]
         (let [Ω (-> Ω
                     (dissoc :spawn)
                     (update :flowing conj spawn))]
          (if
            ;; If there is something solid beneath us
            ((solid? Ω) (below spawn))
            ;; If there is nothing in our way,
            (if ((not-solid? Ω) (which spawn))
              ;; Then continue in that direction
              (recur (assoc Ω :spawn (which spawn)))
              ;; Otherwise mark as blocked
              (assoc Ω :blocked spawn))
            ;; If there isn't anything below us mark as source
            ;; we must have gone off an edge.
            (update Ω :sources conj spawn)))
         Ω)))
   Ω))


(defn merge-sets [m1 m2]
  (merge-with
   (fn [a b] (if (set? a) (into a b) b))
   m1 m2))

(defn combine
  "We just spread both to the left and right, handle it."
  [lefty righty]
  (let [l (:blocked lefty)
        r (:blocked righty)
        combined (merge-sets (dissoc lefty :blocked) (dissoc righty :blocked))]
   (if
     ;; If both directions got blocked, then make those values still.
     (and l r)
     (reduce
      (fn [m x] (update m :still conj x))
      combined
      (horizontal-line (:blocked lefty) (:blocked righty)))
     ;; Otherwise, just return the combined thing.
     combined)))


(defn above-bottom? [[_ yhi] [_ y]] (<= 0 y yhi))

(defn fall
  "Pop a source and let it fall."
  [Ω]
  (let [loc (first (:sources Ω))]
   (loop [Ω Ω
          loc loc]
     (if loc
       (let [Ω (-> Ω
                  (update :sources disj loc)
                  (update :flowing conj loc))
             newloc (below loc)]
        (cond
            ;; If there is something solid below us, create a spawn.
            (and ((solid? Ω) newloc)
                 ((complement (:spawned Ω)) newloc))
            (assoc Ω :spawn loc)

            ;; If we are within the bounds of the image, continue falling.
            (above-bottom? (second (:bounds Ω)) newloc)
            (recur Ω newloc)

            ;; Otherwise just pop the source (we must have hit bottom)
            :else Ω))
       ;; If there are no sources left, we're done
       (assoc Ω :finished true)))))


(defn fill
  "The main filling logic."
  [Ω]
  ;; To perform the fill, we will start off by spreading
  ;; left and right from every :split, then
  ;; combine the result, finally we'll initiate a new
  ;; :fall
  (fall (combine (spread left Ω) (spread right Ω))))


(defn fill-completely
  "Keep filling until completely finished."
  [Ω]
  (first (drop-while (complement :finished) (iterate fill Ω))))


;; We can test all of the game logic on the test input.
(let [Ω (nth (iterate fill test-Ω) 2)]
  [Ω (render Ω)])

#_(let [Ω (fill-completely test-Ω)]
    (assert (= 57 (total-wet Ω)))
    (render Ω))

;; That looks good, so we compute the actual answer.

;(def finished-Ω (time (fill-completely Ω)))

;(def ans1 (total-wet finished-Ω))

;; 273 is too low!


;; Try to render an image
(defn render-image [Ω]
 (let [{:keys [clay still flowing sources spawn blocked bounds]} Ω
       [[xlo xhi] [ylo yhi]] bounds
       width (- (inc xhi) (dec xlo))
       height (- (inc yhi) (dec ylo))
       img (BufferedImage. width height BufferedImage/TYPE_3BYTE_BGR)]
   (doseq [x (range xlo xhi)
           y (range ylo yhi)]
     (let [xp (- x (dec xlo)) yp (- y (dec ylo))
           loc [x y]]
       (.setRGB img xp yp
                (cond
                 (= spawn loc) (.getRGB (java.awt.Color. 255 0 0))
                 (= blocked loc) (.getRGB java.awt.Color/GREEN)
                 (sources loc) (.getRGB java.awt.Color/BLUE)
                 (clay loc) (.getRGB (java.awt.Color. 165 42 42))
                 (still loc) (.getRGB (java.awt.Color. 0 0 139))
                 (flowing loc) (.getRGB (java.awt.Color. 70 130 180))
                 :else (.getRGB java.awt.Color/WHITE)))))
   img))


#_(let [Ω (nth (iterate fill Ω) 300)]
    [Ω (render-image Ω)])
