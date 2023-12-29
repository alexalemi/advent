# Advent of Code - Day 12

(use "./util")
(use judge)

(def data-string (slurp "../input/12.txt"))
(def test-string `???.### 1,1,3
.??..??...?##. 1,1,3
?#?#?#?#?#?#?#? 1,3,1,6
????.#...#... 4,1,1
????.######..#####. 1,6,5
?###???????? 3,2,1`)


(def data-peg
  ~{:counts (group (some (+ "," (number :d+))))
    :line (<- (some (set ".?#")))
    :main (some (+ "\n" (group (* :line " " :counts))))})

(defn ->data [s]
  (freeze (peg/match data-peg s)))

(def test-data (->data test-string))
(def data (->data data-string))
(test test-data
      [["???.###" [1 1 3]]
       [".??..??...?##." [1 1 3]]
       ["?#?#?#?#?#?#?#?" [1 3 1 6]]
       ["????.#...#..." [4 1 1]]
       ["????.######..#####." [1 6 5]]
       ["?###????????" [3 2 1]]])

(def yay (chr ".")) # 46
(def nay (chr "#")) # 35
(def may (chr "?")) # 63

(defn car [[x y]] x)
(defn cdr [[x y]] y)
(defn conj [l a] [a l])
(defn ->list [xs]
  (var out [])
  (each x (reverse xs) (set out (conj out x)))
  out)
(def rest cdr)
(test (->list [1 2 3]) [1 [2 [3 []]]])
(test (->list "foo") [102 [111 [111 []]]])

(defn <-list [xs]
  (var xs xs)
  (var out @[])
  (while (not (empty? xs))
    (array/push out (car xs))
    (set xs (cdr xs)))
  (freeze out))

(defn consume-n [n patt]
  (let [bad (conj [] :bad)]
    (when (nil? n) (break bad))
    (case (car patt)
      :bad bad
      nil (if (= n 0) patt bad)
      nay (consume-n (dec n) (cdr patt))
      yay (if (= n 0) (cdr patt) bad)
      may (if (= n 0)
            (cdr patt)
            (consume-n (dec n) (cdr patt))))))

(let [show (fn [x] (if (= :bad (car x)) :bad (string/from-bytes ;(<-list x))))
      foo |(show (consume-n $0 (->list $1)))]
  (test (foo 3 "???.#") "#")
  (test (foo 2 "???.#") ".#")
  (test (foo 1 "???.#") "?.#")
  (test (foo 1 ".???.#") :bad)
  (test (foo 0 ".???.#") "???.#")
  (test (foo nil ".???.#") :bad))

(var memo @{})
(defn matches
  "Recursively compute the number of matches."
  [patt counts]
  (let [key [patt counts]]
    (when-let [ans (in memo key)]
      (break ans))

    (def result
      (case (car patt)
        :bad 0
        nil (if (empty? counts) 1 0)
        yay (matches (cdr patt) counts)
        nay (matches (consume-n (car counts) patt) (cdr counts))
        may (+
              # assume its a yay
              (matches (cdr patt) counts)
              # consume the nay
              (matches (consume-n (car counts) patt) (cdr counts)))))
    (put memo key result)
    result))

(defn matches* [patt counts]
  (matches (->list patt) (->list counts)))


(test (map (partial apply matches*) test-data) @[1 4 1 1 4 10])

(defn part-1 [data]
  (sum (map (partial apply matches*) data)))

(test (part-1 test-data) 21)
(def ans1 (part-1 data))
(test ans1 7753)

(defn expand [[patt counts]]
  [(string/join (seq [i :range [0 5]] patt) "?")
   (catseq [i :range [0 5]] counts)])

(defn part-2 [data]
  (part-1 (map expand data)))

(test (part-2 test-data) 525152)
(def ans2 (part-2 data))
(test ans2 280382734828319)

(defn main [&]
  (print "Answer1: " ans1)
  (print "Answer2: " ans2))
