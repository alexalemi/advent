#lang racket

(define raw-data (port->string (open-input-file "../input/01.txt") #:close? #t))
(define data (map string->number (string-split raw-data "\n")))

(displayln "Answer1")
;; (print (answer1 data))
