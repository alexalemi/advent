;; Some basic io functionality

(define (read-file filename)
  (with-input-from-file filename
    (lambda ()
      (let loop ((c (read-char)) (cs '()))
        (if (eof-object? c) (reverse cs)
            (loop (read-char) (cons c cs)))))))

(read-file "../input/13.txt")
