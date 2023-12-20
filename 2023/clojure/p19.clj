;; # Advent of Code 2023 - Day 19

(ns p19
  (:require [clojure.string :as str]))

(def data-string (slurp "../input/19.txt"))
(def test-string "px{a<2006:qkq,m>2090:A,rfg}
pv{a>1716:R,A}
lnx{m>1548:A,A}
rfg{s<537:gd,x>2440:R,A}
qs{s>3448:A,lnx}
qkq{x<1416:A,crn}
crn{x>2662:A,R}
in{s<1351:px,qqz}
qqz{s>2770:qs,m<1801:hdj,R}
gd{a>3333:R,R}
hdj{m>838:A,pv}

{x=787,m=2655,a=1222,s=2876}
{x=1679,m=44,a=2067,s=496}
{x=2036,m=264,a=79,s=2244}
{x=2461,m=1339,a=466,s=291}
{x=2127,m=1623,a=2188,s=1013}")

(defn process-part [s]
  (let [[_ x m a s] (re-matches #"\{x=(\d+),m=(\d+),a=(\d+),s=(\d+)\}" s)]
    {:x (parse-long x)
     :m (parse-long m)
     :a (parse-long a)
     :s (parse-long s)}))

(defn process-rule [s]
  (let [[_ slot op num then] (re-matches #"(\w+)(<|>)(\d+):(\w+)" s)]
    {:slot (keyword slot) :op (keyword op) :num (parse-long num) :then (keyword then)}))

(defn process-workflow [s]
  (let [[_ name rules else] (re-matches #"(\w+)\{([a-zAR0-9:><,]+),(\w+)\}" s)
        rules (str/split rules #",")]
    [(keyword name) {:rules (map process-rule rules)
                     :else (keyword else)}]))

(defn ->data [s]
  (let [[workflows parts] (str/split s #"\n\n")
        parts (map process-part (str/split-lines parts))
        workflows (map process-workflow (str/split-lines workflows))]
   {:parts parts :workflows (into {} workflows)}))

(def data (->data data-string))
(def test-data (->data test-string))

(defn handle-rule [part {:keys [num op slot then]}]
  (let [ops {:> > :< <}]
    (when ((ops op) (part slot) num)
          then)))

(defn handle-workflow [{rules :rules else :else} part]
    (or
     (some (partial handle-rule part) rules)
     else))

(defn handle-workflows [workflows part]
    (loop [workflow (workflows :in)]
      (let [x (handle-workflow workflow part)]
        (case x
          :A :accept
          :R nil
          (recur (workflows x))))))

(defn sum [col]
  (reduce + col))

(defn part-1 [{:keys [workflows parts]}]
  (let [handler (partial handle-workflows workflows)]
    (transduce
     (comp
      (filter handler)
      (map (comp sum vals)))
     +
     parts)))

(assert (= 19114 (part-1 test-data)))
(defonce ans1 (part-1 data))
(assert (= 346230 ans1))

;; ## Part 2
;; Now for this part we need to figure out how many distinct possibilities are going to be accepted...

;; Let's now process ranges, but let's use ranges that are inclusive on the bottom and exclusive at the top.
(def all-parts
  {:x [1 4001]
   :m [1 4001]
   :a [1 4001]
   :s [1 4001]})

(defn slice-range
  "Cut a range into old and new."
  [[lo hi] op num]
  (case op
     :> [[lo (inc num)] [(inc num) hi]]
     :< [[num hi] [lo num]]))

(defn handle-rule*
  "Adapt to ranges, return the unprocessed and processed examples"
  [parts {:keys [num op slot then]}]
  (let [[old new] (slice-range (parts slot) op num)]
    [(assoc parts slot old)
     [then (assoc parts slot new)]]))

(defn handle-workflow* [{rules :rules else :else} parts]
  (let [[old thens]
        (reduce
         (fn [[cur thens] rule]
           (let [[old new] (handle-rule* cur rule)]
              [old (conj thens new)]))
         [parts []]
         rules)]
    (conj thens [else old])))

(defn sum-range [[lo hi]] (- hi lo))

(defn sum-parts [parts]
    (reduce * (map sum-range (vals parts))))

(defn handle-workflows* [workflows]
    (loop [to-process [[:in all-parts]]
           total 0]
      (if-let [[which parts] (peek to-process)]
        (case which
          :A (recur (pop to-process) (+ total (sum-parts parts)))
          :R (recur (pop to-process) total)
          (recur (into (pop to-process) (handle-workflow* (workflows which) parts)) total))
        total)))

(defn part-2 [data]
  (handle-workflows* (data :workflows)))

(assert (= (part-2 test-data) 167409079868000))
(def ans2 (part-2 data))
(assert (= ans2 124693661917133))


(defn -main []
    (println "Answer 1:" ans1)
    (println "Answer 2:" ans2))
