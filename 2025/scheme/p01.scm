

(load "batteries.scm")
(load "prelude.scm")
(load "judge.scm")

(define raw-data (string-split (slurp "../input/01.txt") "\n"))

(define test-data (string-split "L68
L30
R48
L5
R60
L55
L1
L99
R14
L82" "\n"))

(let ((inst (test-data 0)))
  (string->number (substring inst 1)))


(define (dial insts)
  (define (right x y) (modulo (+ x y) 100))
  (define (left x y) (modulo (- x y) 100))
  (define (iter loc insts zeros)
    (if (empty? insts) zeros
      (let* ((inst (first insts))
             (dir (inst 0))
             (amt (string->number (substring inst 1)))
             (new-zeros (if (= loc 0) (inc zeros) zeros)))
        (cond 
          ((char=? #\L dir) (iter (left loc amt) (rest insts) new-zeros))
          ((char=? #\R dir) (iter (right loc amt) (rest insts) new-zeros))
          (else 'error)))))
  (iter 50 insts 0))

(test (dial test-data) 3)

(define ans-1 (dial raw-data))
(display "Answer 1: ") 
(display ans-1)
(newline)


;; Part 2

(define (new-zeros loc op amt)
  (define (iter zeros loc amt)
    (if (zero? amt) (if (zero? loc) (inc zeros) zeros)
     (iter (if (zero? loc) (inc zeros) zeros)
           (modulo (op loc 1) 100)
           (dec amt))))
  (iter 0 (modulo (op loc 1) 100) (dec amt)))

(define (new-dial insts)
  (define (right x y) (modulo (+ x y) 100))
  (define (left x y) (modulo (- x y) 100))
  (define (iter loc insts zeros)
    (if (empty? insts) zeros
      (let* ((inst (first insts))
             (dir (inst 0))
             (amt (string->number (substring inst 1))))
        (cond 
          ((char=? #\L dir) (iter (left loc amt) (rest insts) (+ zeros (new-zeros loc - amt))))
          ((char=? #\R dir) (iter (right loc amt) (rest insts) (+ zeros (new-zeros loc + amt))))
          (else 'error)))))
  (iter 50 insts 0))

(test (new-dial test-data) 6)

(define ans-2 (new-dial raw-data))
(display "Answer 2: ") 
(display ans-2)
(newline)
