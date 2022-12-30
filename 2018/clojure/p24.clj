;; # 🎄 Advent of Code 2018 - Day 24 - Immune System Simulator 20XX
;; This appears to be an involved game sort of thing.
(ns p24
  (:require [clojure.string :as str]
            [clojure.test :as test]))

(def data-string (slurp "../input/24.txt"))
(def test-string "Immune System:
17 units each with 5390 hit points (weak to radiation, bludgeoning) with an attack that does 4507 fire damage at initiative 2
989 units each with 1274 hit points (immune to fire; weak to bludgeoning, slashing) with an attack that does 25 slashing damage at initiative 3

Infection:
801 units each with 4706 hit points (weak to radiation) with an attack that does 116 bludgeoning damage at initiative 1
4485 units each with 2961 hit points (immune to radiation; weak to fire, cold) with an attack that does 12 slashing damage at initiative 4")

;; The immune system and infection each have an army made up of several groups.
;; Each group consists of one or more identical units.
;; The armies fight until only one army has units remaining.
;;
;; Units within a group all have the same hit points, attack damage, attack type, initiative and sometimes weaknesses and immunities.
;; I'll represent each group with a structure like the following:
;;
;; `18 units each with 729 hit points (weak to fire; immune to cold, slashing) with an attack that does 8 radiation damage at initiative 10)`
;;  will map to:

(def example-group
  {:units 18
   :hp 729
   :weaknesses #{:fire}
   :immune #{:cold, :slashing}
   :type :radiation
   :initiative 10
   :attack 8})

;; ## Data

(defn parse-modifier-substring [s]
  (if (some? s)
    (let [[kind types] (str/split s #" to ")
          types (str/split types #", ")]
      {(keyword kind)
       (into #{} (map keyword) types)})
    {}))

(defn parse-modifier-string [s]
  (if (some? s)
    (do
      (assert (= \( (first s)) "doesn't start with an opening parenthesis.")
      (assert (= \  (last s)) "doesn't end with a space.")
      (assert (= \) (last (butlast s))) "doesn't end with a closing parenthesis.")
      (let [s (subs s 1 (- (count s) 2))
            [part1 part2] (str/split s #"; ")]
        (merge
         {:weak #{} :immune #{}}
         (parse-modifier-substring part1)
         (parse-modifier-substring part2))))
    {:weak #{} :immune #{}}))

(defn parse-group-string [s]
  (let [[_ units hp modifiers attack type initiative] (re-matches #"(\d+) units? each with (\d+) hit points (\(.*\) )?with an attack that does (\d+) (\w+) damage at initiative (\d+)" s)]
    (merge
     {:units (parse-long units)
      :hp (parse-long hp)
      :type (keyword type)
      :initiative (parse-long initiative)
      :attack (parse-long attack)}
     (parse-modifier-string modifiers))))

(defn parse [s]
  (let [[immune infection] (str/split s #"\n\n")
        immune-groups (str/split-lines immune)
        infection-groups (str/split-lines infection)]
    (assert (= (first immune-groups) "Immune System:") "Wrong header for Immune groups.")
    (assert (= (first infection-groups) "Infection:") "Wrong header for Infection groups.")
    {:immune-system (mapv (comp (fn [x] (assoc x :which :immune-system)) parse-group-string) (rest immune-groups))
     :infection (mapv (comp (fn [x] (assoc x :which :infection)) parse-group-string) (rest infection-groups))}))

(def test-data (parse test-string))
(def data (parse data-string))

;; ## Logic

(defn effective-power [{units :units attack :attack}]
  (* units attack))

(defn group-sorter [group]
  [(- (effective-power group)) (- (:initiative group))])

(defn damage [attacking-group defending-group]
  (let [power (effective-power attacking-group)
        {attack-type :type} attacking-group
        {immune :immune weak :weak} defending-group]
    (cond
      (immune attack-type) 0
      (weak attack-type) (* 2 power)
      :else power)))

(defn target-sorter [attacking-group]
  (fn [group]
    [(- (damage attacking-group group)) (- (effective-power group)) (- (:initiative group))]))

(defn indexed [xs] (map-indexed vector xs))

(defn target-selection [attacking-groups defending-groups]
  (loop [assignments {}
         attacking-groups (sort-by (comp group-sorter second) (indexed attacking-groups))
         defending-groups (set (indexed defending-groups))]
    (if-let [[id attack-group] (first attacking-groups)]
      (let [[target-id target] (first (sort-by (comp (target-sorter attack-group) second) defending-groups))]
        (if (and target (pos? (damage attack-group target)))
          (recur
           (assoc assignments id target-id)
           (rest attacking-groups)
           (disj defending-groups [target-id target]))
          (recur
           (assoc assignments id nil)
           (rest attacking-groups)
           defending-groups)))
      assignments)))

(defn target-selection-phase [data]
  (let [{:keys [immune-system infection]} data]
    {:infection (target-selection infection immune-system)
     :immune-system   (target-selection immune-system infection)}))

(defn apply-damage [group damage]
  (let [{hp :hp units :units} group
        killed-units (quot damage hp)]
    (assoc group :units (max (- units killed-units) 0))))

(def opposing-side
  {:immune-system :infection
   :infection :immune-system})

(defn attack [data]
  (let [{:keys [immune-system infection]} data
        targets (target-selection-phase data)
        attack-order (sort-by (comp :initiative second) > (concat (indexed immune-system) (indexed infection)))]
    (loop [data data
           attacks attack-order]
      (if-let [[id attack-group] (first attacks)]
        (let [attack-which (:which attack-group)
              defend-which (opposing-side attack-which)
              attack-group (get (data attack-which) id)
              target (get (targets attack-which) id)
              defend-group (get (data defend-which) target)]
          (if (and target (pos? (:units attack-group)))
            (recur
             (assoc-in data [defend-which target] (apply-damage defend-group (damage attack-group defend-group)))
             (rest attacks))
            (recur data (rest attacks))))
        data))))

(defn round [data]
  (-> (attack data)
      (update :immune-system (fn [x] (filterv (comp pos? :units) x)))
      (update :infection (fn [x] (filterv (comp pos? :units) x)))))

;; ## Part 1

(defn complete [data]
  (reduce
   (fn [x y] (if (or (= x y)
                     (empty? (:immune-system x))
                     (empty? (:infection x)))
               (reduced x) y))
   (iterate round data)))

(defn part-1 [data]
  (let [data (complete data)]
    (transduce
     (map :units)
     +
     0
     (concat (:immune-system data) (:infection data)))))

(test/deftest test-part-1
  (test/is (= 5216 (part-1 test-data))))

(def ans1 (time (part-1 data)))

;; ## Part 2

(defn boost [data x]
  (update data :immune-system (fn [groups] (mapv (fn [group] (update group :attack + x)) groups))))

(defn winner [data]
  (let [{:keys [immune-system infection]} data]
    (cond
      (empty? immune-system) :infection
      (empty? infection) :immune-system
      :else :tie)))

(test/deftest test-part-2
  (test/is (= 51 (part-1 (boost test-data 1570)))))

(defn part-2 [data]
  (loop [x 0]
    (let [final (complete (boost data x))]
      (println "Trying out a boost of = " x " winner is = " (winner final))
      (if (= :immune-system (winner final))
        (transduce (map :units) + 0 (:immune-system final))
        (recur (inc x))))))

(def ans2 (time (part-2 data)))

;; ## Main

(defn -test [& _]
  (test/run-tests 'p24))

(defn -main [& _]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
