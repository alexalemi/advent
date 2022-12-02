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

;; Let's think a bit about how we should best represent our world. We clearly
;; need some way to specify all of the clay locations, so we'll store a set in
;; `:clays` with those coordinates.  We also need access to the bounds,
;; which could be a function as its drived by

(defn bounds [{clay :clay}]
  [[(dec (reduce min (map first clay)))
    (inc (reduce max (map first clay)))]
   [(reduce min (map second clay))
    (reduce max (map second clay))]])

(defn data->state [data]
  (let [clay (reduce into #{} (map line data))]
    {:clay clay
     :still #{}
     :flowing {}
     :sources #{[500 0]}}))

(def test-Ω (data->state test-data))
(def Ω (data->state data))

;; ## Visualization
;; Build a nice visualizer though its probably too expensive to do the full problem input.

(defn in-bounds? [Ω [_ y]]
  (let [[_  [ylo yhi]] (bounds Ω)]
    (<= ylo y yhi)))

(defn total-wet [Ω]
  (count (filter (partial in-bounds? Ω)
                 (set/union (into (:still Ω) (keys (:flowing Ω)))))))

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
   (let [[[xlo xhi] [ylo yhi]] (bounds Ω)]
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


;; Try to render an image
(defn render-image
  ([Ω] (render-image Ω (bounds Ω) 1))
  ([Ω zoom] (render-image Ω (bounds Ω) zoom))
  ([Ω bounds zoom]
   (let [{:keys [clay still flowing sources spawn blocked]} Ω
         [[xlo xhi] [ylo yhi]] bounds
         width (* zoom (- (inc xhi) (dec xlo)))
         height (* zoom (- (inc yhi) (dec ylo)))
         img (BufferedImage. width height BufferedImage/TYPE_3BYTE_BGR)]
     (doseq [xp (range width)
             yp (range height)]
       (let [x (+ (quot xp zoom) (dec xlo))
             y (+ (quot yp zoom) (dec ylo))
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
     img)))

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


(defn mark-spawn [Ω spawn from]
  (-> Ω
      (assoc :spawn spawn)
      (update :flowing assoc spawn from)))

(defn spread
  "Take the spawn and move whichways until we hit clay or there is nothing below us."
  [which Ω]
  ;; If there isn't a spawn, do nothing
  (if-let [spawn (:spawn Ω)]
    (let [Ω (update Ω :sources conj ((:flowing Ω) spawn))] ;; Add the parent as another source
     (loop [Ω Ω]
       (if-let [spawn (:spawn Ω)]
         (let [Ω (dissoc Ω :spawn)
               newspawn (which spawn)]
          (if
            ;; If there is something solid beneath us
            ((solid? Ω) (below spawn))
            ;; If there is nothing in our way,
            (if ((not-solid? Ω) newspawn)
              ;; Then continue in that direction
              (recur (mark-spawn Ω newspawn spawn))
              ;; Otherwise mark as blocked
              (assoc Ω :blocked spawn))
            ;; If there isn't anything below us mark as source
            ;; we must have gone off an edge.
            (update Ω :sources conj spawn)))
         Ω)))
   Ω))


(defn merge-sets-and-maps [m1 m2]
  (merge-with
   (fn [a b] (if (or (map? a) (set? a)) (into a b) b))
   m1 m2))

(defn mark-still [Ω loc]
  (-> Ω
      (update :still conj loc)
      (update :flowing dissoc loc)))

(defn combine
  "We just spread both to the left and right, handle it."
  [lefty righty]
  (let [l (:blocked lefty)
        r (:blocked righty)
        combined (merge-sets-and-maps (dissoc lefty :blocked) (dissoc righty :blocked))]
   (if
     ;; If both directions got blocked, then make those values still.
     (and l r)
     (reduce
      (fn [m x] (mark-still m x))
      combined
      (horizontal-line (:blocked lefty) (:blocked righty)))
     ;; Otherwise, just return the combined thing.
     combined)))


(defn above-bottom? [Ω [_ y]]
  (let [[_ [_ yhi]] (bounds Ω)]
    (<= 0 y yhi)))

(defn fall
  "Pop a source and let it fall."
  [Ω]
  (if-let [loc (first (:sources Ω))]
   (loop [Ω Ω
          loc loc]
      (let [Ω (update Ω :sources disj loc)
            newloc (below loc)]
        (cond
            ;; If there is something solid below us, create a spawn.
            ((solid? Ω) newloc)
            (mark-spawn Ω loc ((:flowing Ω) loc))

            ;; If we are within the bounds of the image, continue falling.
            (above-bottom? Ω newloc)
            (recur (update Ω :flowing assoc newloc loc) newloc)

            ;; Otherwise just pop the source (we must have hit bottom)
            :else Ω)))
   Ω))

(defn fill
  "The main filling logic."
  [Ω]
  ;; To perform the fill, we will start off by spreading
  ;; left and right from every :split, then
  ;; combine the result, finally we'll initiate a new
  ;; :fall
  (if-let [_ (first (:sources Ω))]
    (let [x (fall Ω)]
        (combine (spread left x) (spread right x)))
    (assoc Ω :finished true)))


(defn fill-completely
  "Keep filling until completely finished."
  [Ω]
  (first (drop-while (complement :finished) (iterate fill Ω))))


;; We can test all of the game logic on the test input.

(let [Ω test-Ω
      Ω (fill Ω)]
      ;Ω (fall Ω)]
      ;Ω (nth (iterate fill Ω) 3)]
  [Ω (render Ω)]
  #_(if-let [loc (first (:sources Ω))]
      (loop [Ω Ω
             loc loc]
         (let [Ω (update Ω :sources disj loc)
               newloc (below loc)]
           (cond
               ;; If there is something solid below us, create a spawn.
               ((solid? Ω) newloc)
               (mark-spawn Ω loc ((:flowing Ω) loc))

               ;; If we are within the bounds of the image, continue falling.
               (above-bottom? Ω newloc)
               (recur (update Ω :flowing assoc newloc loc) newloc)

               ;; Otherwise just pop the source (we must have hit bottom)
               :else Ω)))
     Ω))

#_(let [Ω (fill-completely test-Ω)]
    (assert (= 57 (total-wet Ω)))
    (render Ω))

;; That looks good, so we compute the actual answer.

#_(def finished-Ω (time (fill-completely Ω)))

#_(def ans1 (total-wet finished-Ω))

;; 273 is too low!
;; 39210 is too high!




#_(let [Ω (nth (iterate fill Ω) 39)]
    [Ω
     (render-image Ω [[433 550] [0 70]] 8)])

