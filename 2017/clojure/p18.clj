(ns advent18
  (:require
   [clojure.test :as test]
   [clojure.string :as str]))

(comment)
  ;; snd X plays a sound with a frequency equal to the value of X.
  ;; set X Y sets register X to the value of Y.
  ;; add X Y increases register X by the value of Y.
  ;; mul X Y sets register X to the result of multiplying the value contained in register X by the value of Y.
  ;; mod X Y sets register X to the remainder of dividing the value contained in register X by the value of Y (that is, it sets X to the result of X modulo Y).
  ;; rcv X recovers the frequency of the last sound played, but only when the value of X is not zero. (If it is zero, the command does nothing.)
  ;; jgz X Y jumps with an offset of the value of Y, but only if the value of X is greater than zero. (An offset of 2 skips the next instruction, an offset of -1 jumps to the previous instruction, and so on.)

(def test-string "set a 1
add a 2
mul a a
mod a 5
snd a
set a 0
rcv a
jgz a -1
set a 1
jgz a -2")
(def data-string (slurp "../input/18.txt"))

(defn process-line [line]
  (map (comp #(if (symbol? %) (keyword %) %)  read-string) (str/split line #" ")))

(def data (mapv process-line (str/split-lines data-string)))
(def test-data (mapv process-line (str/split-lines test-string)))

(defn init-machine [cmds]
  {:loc 0 :cmds cmds :registers {} :in [] :out []})

(defn step [machine]
  (let [{:keys [cmds registers loc out]} machine
        register (get cmds loc)]
    (letfn [(lookup [x] (if (keyword? x) (get registers x 0) x))]
      (let [[cmd X Y] register
            Yval (lookup Y)
            machine (update machine :loc inc)]
        (case cmd
          :snd (update machine :snd (fnil conj []) (lookup X))
          :set (assoc machine :registers (assoc registers X Yval))
          :add (assoc machine :registers (update registers X (fnil + 0) Yval))
          :mul (assoc machine :registers (update registers X (fnil * 0) Yval))
          :mod (assoc machine :registers (update registers X (fnil mod 0) Yval))
          :rcv (assoc machine :out (let [Xval (lookup X)] (if (zero? Xval) out (conj out Xval))))
          :jgz (let [Xval (lookup X)] (if (pos? Xval) (update machine :loc + (- Yval 1)) machine)))))))

(defn part-1 [data]
  (->> (init-machine data)
       (iterate step)
       (drop-while (comp empty? :out))
       first
       :snd
       last))

(test/deftest test-part-1
  (test/is (= (part-1 test-data) 4))
  (test/is (= (part-1 data) 4601)))

(time (def ans1 (part-1 data)))
(println)
(println "Answer1: " ans1)

(defn step-2 [machine]
  (let [{:keys [cmds registers loc in terminated]} machine
        register (get cmds loc)]
    (if (true? terminated) machine
        (letfn [(lookup [x] (if (keyword? x) (get registers x 0) x))]
          (let [[cmd X Y] register
                Yval (lookup Y)
                machine (update machine :loc inc)]
            (case cmd
              :snd (update machine :out conj (lookup X))
              :set (assoc machine :registers (assoc registers X Yval))
              :add (assoc machine :registers (update registers X (fnil + 0) Yval))
              :mul (assoc machine :registers (update registers X (fnil * 0) Yval))
              :mod (assoc machine :registers (update registers X (fnil mod 0) Yval))
              :rcv (if (seq in) (-> machine
                                    (assoc-in [:registers X] (first in))
                                    (update :in (comp vec rest)))
                       (assoc machine :loc loc))
              :jgz (let [Xval (lookup X)]
                     (if (pos? Xval)
                       (update machine :loc + (- Yval 1))
                       machine))
              :else (-> machine
                        (assoc :terminated true)
                        (assoc :loc loc))))))))

(defn step-both [machines]
  (let [machines (mapv step-2 machines)
        out-0 (get-in machines [0 :out])
        out-1 (get-in machines [1 :out])]
    [out-0 out-1]
    (cond-> machines
      (seq out-0) (-> (update-in [1 :in] conj (first out-0))
                      (update-in [0 :out] (comp vec rest)))
      (seq out-1) (-> (update-in [0 :in] conj (first out-1))
                      (update-in [1 :out] (comp vec rest))))))

(def test-data-2 (mapv process-line (str/split-lines "snd 1
snd 2
snd p
rcv a
rcv b
rcv c
rcv d")))

(defn terminated? [machine]
  (get machine :terminated))
(defn stuck? [machine]
  (let [{:keys [loc cmds in]} machine
        cmd (get cmds loc)
        [kind & _] cmd]
    (and (= kind :rcv) (empty? in))))

(defn finished? [machines]
  (or (every? terminated? machines) (every? stuck? machines)))

(defn send-1? [machines]
  (let [machine (get machines 1)
        {:keys [loc cmds in]} machine
        cmd (get cmds loc)
        [kind & args] cmd]
    (= kind :snd)))

(time (def ans2
        (let [machine-0 (assoc-in (init-machine data) [:registers :p] 0)
              machine-1 (assoc-in (init-machine data) [:registers :p] 1)
              machines [machine-0 machine-1]]
          (count (filter send-1? (take-while (complement finished?) (iterate step-both machines)))))))

(test/deftest test-part-2
  (test/is (= ans2 6858)))

(println "Answer 2:" ans2)

(test/run-tests)
