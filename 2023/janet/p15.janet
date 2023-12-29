# Advent of Code 2023 - Day 15

(use "./util")
(use judge)

(def data-string (slurp "../input/15.txt"))
(def test-string `rn=1,cm-,qp=3,cm=2,qp-,pc=4,ot=9,ab=5,pc-,pc=6,ot=7`)

(defn hash-fn [s]
  (var x 0)
  (loop [c :in s]
    (+= x c)
    (*= x 17)
    (%= x 256))
  x)

(test (hash-fn "HASH") 52)

(defn part-1 [data-string]
  (sum (map hash-fn (string/split "," (string/replace-all "\n" "" data-string)))))


(test (part-1 test-string) 1320)
(def ans1 (part-1 data-string))
(test ans1 514394)

(def data-peg
  ~{:remove (group (* (constant :rem) (<- :w+) "-"))
    :add (group (* (constant :add) (<- :w+) "=" (number :d+)))
    :main (some (+ :remove :add "," "\n"))})

(defn ->data [s]
  (freeze (peg/match data-peg s)))

(def data (->data data-string))
(def test-data (->data test-string))

(defn maybe [x v]
  (if (nil? x) v x))

(defn add [boxes elem]
  (let [[_ x v] elem]
    (array/push (boxes (hash-fn x)) [x v])))

(defn rem [boxes elem]
  (let [[_ x v] elem
        box (maybe (boxes (hash-fn x)) @[])
        pk (find-index |(= $0 [x v]) box)]
    (when pk
      (array/remove box pk))))

(defn part-2 [data]
  (var boxes @{})
  (loop [i :range [0 256]]
    (put boxes i @[]))
  (loop [inst :in data]
    (let [[which x v] inst
          id (hash-fn x)
          box (boxes id)
          pk (find-index |(= (first $0) x) box)]
      (case which
        :add (do
               (if pk
                 (do
                   (array/remove (boxes id) pk)
                   (array/insert (boxes id) pk [x v]))
                 (array/push (boxes id) [x v])))
        :rem (when pk
               (array/remove (boxes id) pk)))))
  # add up focusing power
  (var tot 0)
  (loop [id :range [0 256]]
    (each [i [x v]] (indexed (boxes id))
      (+= tot (* (inc id) (inc i) v))))
  tot)

(test (part-2 test-data) 145)
(def ans2 (part-2 data))
(test ans2 236358)

(defn main [&]
  (print "Answer1: " ans1)
  (print "Answer2: " ans2))
