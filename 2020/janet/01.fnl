# Day 1

(local target 2020)
(local flname "/home/alemi/projects/advent/2020/input/01.txt")
(local data (map scan-number
               (string/split "\n" (file/read (file/open flname) :all))))

(fn head [lst] (get lst 0))
(fn tail [lst] (slice lst 1))
(fn insert [x val] (table.insert x val :true))

(fn twosum [lst val]
  (let [iter (fn iter [seen left]
               (let [x (head left)
                     rem (- val (head left))]
                 (if (in seen rem) (* rem x)
                   (iter (add seen x) (tail left)))))]
    (iter #{} lst)))

(local ans1 (twosum data target))
(print ans1)
