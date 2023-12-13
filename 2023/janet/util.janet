
(use judge)

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
  (tabseq [[k v] :pairs m] k (f v)))

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

(defn iterate 
  "Infinite sequence of function applications. x fx ffx ..."
  [f x]
  (var x x)
  (generate [_ :iterate true :after (set x (f x))] x))

(defn indexed
  "Like python enumerate"
  [col]
  (map tuple (integers) col))

(defn second
 "Returns the second element of a collection."
 [[h x & col]] x)

(def some? "not-nil?" (complement nil?))

## Simple set operations
## implement a set as a table with true keys.

(defn ->set [ks] (tabseq [x :in ks] x true))
(defn set-has? [s k] (true? (s k)))
(defn set-keys [s] (keys s))
(defn set-add! [s k] (put s k true))
(defn set-remove! [s k] (put s k nil))
(defn set-difference! [a b] (eachk x b (set-remove! a x)) a)
(defn set-union! [a b] (eachk x b (set-add! a x)) a)

(defn memoize [f]
  (var memo @{})
  (fn [& args]
    (when-let [ans (in memo args)]
      (break ans))
    (def result (apply f args))
    (put memo args result)
    result))

