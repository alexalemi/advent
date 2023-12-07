

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

(defn map-vals
  "Apply a function to each of the values in a map."
  [f m]
  (from-pairs (seq [[k v] :pairs m] [k (f v)])))

# This is the built in always
(defn always
  "Returns the constant value."
  [val]
  (fn [x] val))

(defn zip
  "Zip together many collections into tuples."
  [& cols]
  (map tuple ;cols))

(defn integers
  "Infinite sequence of integers."
  []
  (var x 0)
  (generate [_ :iterate true :after (++ x)] x))

(defn indexed
  "Like python enumerate"
  [col]
  (map tuple (integers) col))

(defn second
 "Returns the second element of a collection."
 [[h x & col]] x)
