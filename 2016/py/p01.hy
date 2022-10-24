(import utils [data16])
(import hyrule)


(setv data (data16 1))

(defn magnitude [x]
  (int (+ (abs (. x real)) (abs (. x imag)))))

