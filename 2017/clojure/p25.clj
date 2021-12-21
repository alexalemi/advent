(ns advent25
  (:require
   [clojure.test :as test]
   [clojure.string :as str]))

(def data-string (slurp "../input/25.txt"))
(def test-string "Begin in state A.
Perform a diagnostic checksum after 6 steps.

In state A:
  If the current value is 0:
    - Write the value 1.
    - Move one slot to the right.
    - Continue with state B.
  If the current value is 1:
    - Write the value 0.
    - Move one slot to the left.
    - Continue with state B.

In state B:
  If the current value is 0:
    - Write the value 1.
    - Move one slot to the left.
    - Continue with state A.
  If the current value is 1:
    - Write the value 1.
    - Move one slot to the right.
    - Continue with state A.")

(def head-pattern #"Begin in state (\w).
Perform a diagnostic checksum after (\d+) steps.")

(def state-pattern #"In state (\w):
  If the current value is 0:
    - Write the value (\d).
    - Move one slot to the (left|right).
    - Continue with state (\w).
  If the current value is 1:
    - Write the value (\d).
    - Move one slot to the (left|right).
    - Continue with state (\w).")

(defn keywordize [x]
  (if (symbol? x) (keyword x) x))

(defn process-line [vals]
  (map (comp keywordize read-string) vals))

(defn process-state [s]
  (let [[state w0 m0 j0 w1 m1 j1] (process-line (rest (re-matches state-pattern s)))]
    [state [{:write w0 :move m0 :jump j0} {:write w1 :move m1 :jump j1}]]))

(defn process [s]
  (let [s (str/trim s)
        parts (str/split s #"\n\n")
        head (first parts)
        [_ start steps] (process-line (re-matches head-pattern head))
        parts (rest parts)]
    {:start start :steps steps
     :inst (into {} (map process-state parts))}))

(def data (process data-string))
(def test-data (process test-string))

(defn init-store [state]
  {:tape #{} :loc 0 :state state})

(defn to-int [x]
  (if x 1 0))

(defn step
  "Given a spec, iterate the state."
  [spec store]
  (let [{:keys [tape loc state]} store
        x (to-int (tape loc))
        {:keys [write move jump]} (get-in spec [state x])]
    (-> store
        (update :tape (if (= write 1) conj disj) loc)
        (update :loc (if (= move :right) inc dec))
        (assoc :state jump))))


(defn part-1 [data]
  (let [{:keys [start steps inst]} data
        store (init-store start)
        next (partial step inst)
        final (nth (iterate next store) steps)]
    (count (:tape final))))

(test/deftest test-part-1
  (test/is (= (part-1 test-data) 3)))

(time (def ans-1 (part-1 data)))
(println)
(println "Answer 1:" ans-1)

(test/run-tests)
