
(macro time [body]
  `(let [clock# os.clock
         start# (clock#)
         res# ,body
         end# (clock#)]
    (print (.. "Elapsed " (* 1000 (- end# start#)) " ms"))
    res#))


(local data 
  (icollect [line (io.lines "../input/01.txt")]
    (tonumber line)))

(local test-data [199 200 208 210 200 207 240 269 260 263])

(fn part-1 [data]
  (var count 0)
  (each [i v (ipairs data)]
    (let [prev (. data (- i 1))]
      (if (and prev (> v prev))
        (set count (+ count 1)))))
  count)


(fn part-2 [data]
  (var count 0)
  (each [i v (ipairs data)]
    (let [prev (. data (- i 3))]
      (if (and prev (> v prev))
        (set count (+ count 1)))))
  count)

(let [ans-1 (time (part-1 data))
      ans-2 (time (part-2 data))]
  (print)
  (print "Part1:" ans-1)
  (print "Part2:" ans-2))



