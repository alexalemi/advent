

(defn merge-with 
  "Merge many tables with a given function."
  [f & tables]
  (def result @{})
  (each t tables
   (loop [[key val] :pairs t]
      (set (result key) (if (get result key) (f (get result key) val) val))))
  result)

(defn rest 
  "Everything but the head."
  [[h & r]] r)

