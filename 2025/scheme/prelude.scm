;;; prelude.scm — R7RS-compatible AoC utility prelude
;;; Works on s7, Chez Scheme (--r7rs), Gauche, Racket (#!r7rs), etc.
;;; No external dependencies.

;; -- aliases -------------------------------------------------------
(define empty? null?)
(define first  car)
(define rest   cdr)
(define second cadr)
(define third  caddr)
(define (inc x) (+ x 1))
(define (dec x) (- x 1))

;; -- test ---------------------------------------------------------
(define (test got expected)
  (if (equal? got expected)
      (if #f #f)
      (error "test failed" got "expected" expected)))

;; -- I/O -----------------------------------------------------------
(define (slurp path)
  (call-with-input-file path
    (lambda (p)
      (let loop ((chars '()))
        (let ((c (read-char p)))
          (if (eof-object? c)
              (list->string (reverse chars))
              (loop (cons c chars))))))))

;; -- string utils --------------------------------------------------

;; split on a string or char delimiter; empty segments are dropped
(define (string-split str delim)
  (let* ((d    (if (char? delim) (string delim) delim))
         (dlen (string-length d))
         (slen (string-length str)))
    (let loop ((start 0) (acc '()))
      (let scan ((i start))
        (cond
          ((> (+ i dlen) slen)
           (reverse (if (< start slen)
                        (cons (substring str start slen) acc)
                        acc)))
          ((string=? (substring str i (+ i dlen)) d)
           (loop (+ i dlen)
                 (if (< start i)
                     (cons (substring str start i) acc)
                     acc)))
          (else (scan (+ i 1))))))))

;; trim whitespace from both ends
(define (string-trim str)
  (let* ((len (string-length str))
         (i (let lp ((i 0))
               (cond ((= i len) len)
                     ((char-whitespace? (string-ref str i)) (lp (+ i 1)))
                     (else i))))
         (j (let lp ((j (- len 1)))
               (cond ((< j i) (- i 1))
                     ((char-whitespace? (string-ref str j)) (lp (- j 1)))
                     (else j)))))
    (if (> i j) "" (substring str i (+ j 1)))))

;; drop the first n characters of a string (R7RS substring needs explicit end)
(define (string-drop s n) (substring s n (string-length s)))

;; repeat string s n times
(define (string-repeat s n)
  (let loop ((k n) (acc ""))
    (if (= k 0) acc (loop (- k 1) (string-append acc s)))))
