;; # Advent of Code 2025 - Day 2

(load "batteries.scm")
(load "judge.scm")

(define data-string (string-trim (slurp "../input/02.txt")))
(define test-string "11-22,95-115,998-1012,1188511880-1188511890,222220-222224,1698522-1698528,446443-446449,38593856-38593862,565653-565659,824824821-824824827,2121212118-2121212124")

(define (formatter str) 
  (map (lambda (s) (map string->number (string-split s "-"))) 
       (string-split str ",")))

(define data (formatter data-string))
(define test-data (formatter test-string))

(define (invalid? num)
  (let* ((s (number->string num))
         (n (length s)))
    (and (even? n) 
         (string=? (substring s 0 (/ n 2))
                   (substring s (/ n 2))))))


(define (inc x) (+ x 1))
(define (dec x) (- x 1))

(define (invalid-sum segment)
  (let ((lo (car segment))
        (hi (cadr segment)))
    (define (iter cur acc)
      (if (> cur hi) acc
          (iter (inc cur) (if (invalid? cur) (+ acc cur) acc))))
    (iter lo 0)))

(define (invalid-id-total data)
  (apply + (map invalid-sum data)))


(test (invalid-id-total test-data) 1227775554)

(define ans-1 (invalid-id-total data))

(begin
  (display "Answer 1: ")
  (display ans-1)
  (newline))


;; ## Part 2

;; We have to redefine our notion of invalid to include any repeated substring.

(define (repeated? s x)
  (string=? s (string-repeat (substring s 0 x) (/ (length s) x))))

(define (invalid? num)
  (let* ((s (number->string num))
         (n (length s)))
    (define (iter x)
      (if (zero? x) #f
          (or (and (zero? (modulo n x))
                   (repeated? s x))
              (iter (dec x)))))
    (iter (floor (/ n 2)))))

(test (invalid-id-total test-data) 4174379265)

(define ans-2 (invalid-id-total data))

(begin
  (display "Answer 2: ")
  (display ans-2)
  (newline))
