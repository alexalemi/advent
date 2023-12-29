(use judge)

(def data-string (slurp "../input/01.txt"))
(def test-string `1abc2
pqr3stu8vwx
a1b2c3d4e5f
treb7uchet`)

(defn calibration-number [s]
  (let [digits (peg/match ~(some (+ (number :d) 1)) s)]
    (+ (* 10 (first digits)) (last digits))))

(defn part-1 [s]
  (sum (map calibration-number (string/split "\n" (string/trim s)))))

(test (part-1 test-string) 142)
(def ans1 (part-1 data-string))
(test ans1 55816)


# Part 2

(def digit-peg
  ~(+
     (/ (if "one" 1) 1)
     (/ (if "two" 1) 2)
     (/ (if "three" 1) 3)
     (/ (if "four" 1) 4)
     (/ (if "five" 1) 5)
     (/ (if "six" 1) 6)
     (/ (if "seven" 1) 7)
     (/ (if "eight" 1) 8)
     (/ (if "nine" 1) 9)
     (number :d)))


(defn extended-calibration-number [s]
  (let [digits (peg/match ~(some (+ ,digit-peg 1)) s)]
    (+ (* 10 (first digits)) (last digits))))

(def test-string-2 `two1nine
eightwothree
abcone2threexyz
xtwone3four
4nineeightseven2
zoneight234
7pqrstsixteen`)

(defn part-2 [s]
  (sum (map extended-calibration-number (string/split "\n" (string/trim s)))))

(test (part-2 test-string-2) 281)
(def ans2 (part-2 data-string))
(test ans2 54980)

(defn main [&]
  (print "Answer1:" ans1)
  (print "Answer2:" ans2))
