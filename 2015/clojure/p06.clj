(ns p06
  [:require [clojure.string :as str]])

(def data-string (slurp "../input/06.txt"))

(def pattern #"(turn on|turn off|toggle) (\d+),(\d+) through (\d+),(\d+)")

(defn process-line [s]
  (let [matches (re-matches pattern s)
        [_ which x1 y1 x2 y2] matches]
    {:kind (case which
             "toggle" :toggle
             "turn off" :off
             "turn on" :on)
     :upper-left [(read-string x1) (read-string y1)]
     :lower-right [(read-string x2) (read-string y2)]}))
    

(defn process [s]
  (map process-line (str/split-lines s)))

(def instructions (process data-string))

(defn intersect
 "Find the intersection of two rectagles if any."
 [{ul1 :upper-left lr1 :lower-right}
  {ul2 :upper-left lr2 :lower-right}]
 {:upper-left (into [] (map max ul1 ul2))
  :lower-right (into [] (map min lr1 lr2))})

(defn valid? [{ul :upper-left lr :lower-right}]
   (every? true? (map >= lr ul)))

(def state {:board {}})

;(defn step [state inst]

(def rules
  {:toggle {:on :off :off :on}
   :on {:on :on :off :on}
   :off {:off :off :on :off}})

(defn eat [state [which x]]
  (case which
    :on (conj state x)
    :off (disj state x)))

(defn step [state inst]
  (let [{:keys [kind upper-left lower-right]} inst
        [x1 y1] upper-left
        [x2 y2] lower-right]
    (reduce eat state 
            (for [x (range x1 (inc x2)) 
                  y (range y1 (inc y2))] 
              [((rules kind) (if (contains? state [x y]) :on :off)) [x y]]))))
          

(def ans1 (count (reduce step #{} instructions)))
(println "Answer1: " ans1)

(def rules2
  {:toggle #(+ % 2)
   :on inc
   :off #(max 0 (dec %))})

(defn eat2 [state [x v]]
  (assoc state x v))
          
(defn step2 [state inst]
  (let [{:keys [kind upper-left lower-right]} inst
        [x1 y1] upper-left
        [x2 y2] lower-right]
    (reduce eat2 state 
            (for [x (range x1 (inc x2)) 
                  y (range y1 (inc y2))] 
              [[x y] ((rules2 kind) (get state [x y] 0))])))) 

(def ans2 (reduce + (vals (reduce step2 {} instructions))))
(println "Answer2: " ans2)
