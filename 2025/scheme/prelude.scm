
;; The AOC prelude

(define (empty? lst) (null? lst))

(define (first lst) (car lst))
(define (rest lst) (cdr lst))

(define (dec x) (- x 1))
(define (inc x) (+ x 1))

(define-macro (comment . body)
  #<unspecified>)

