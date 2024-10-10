; Some Utility Functions

(define (readlines filename)
  (call-with-input-file filename
    (lambda (p)
      (let loop ((line (read-line p))
                 (result '()))
         (if (eof-object? line)
             (reverse result)
             (loop (read-line p) (cons line result)))))))

