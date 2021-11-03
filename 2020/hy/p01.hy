(import [utils [data20]])

(setv data (data20 1))

(defn ans1 [data]
  "Find the product of numbers that sum to 2020"
  (setv nums (set (map int (.splitlines data))))
  (setv one (next (filter (fn [x] (in (- 2020 x) nums)) nums)))
  (* one (- 2020 one)))
  
(print "answer1:" (ans1 data))
