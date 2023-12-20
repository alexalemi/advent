# Advent of Code 2023 - Day 20
 
(use "./util")
(use judge)

(def data-string (slurp "../input/20.txt"))
(def test-string-1 `broadcaster -> a, b, c
%a -> b
%b -> c
%c -> inv
&inv -> a`)
(def test-string-2 `broadcaster -> a
%a -> inv, con
&inv -> b
%b -> con
&con -> output`)

(def data-peg
  ~{:outputs (group (some (+ (/ (<- :w+) ,keyword) ", ")))
    :module (group (* (/ (<- (? (+ "%" "&"))) ,keyword) (/ (<- :w+) ,keyword) " -> " :outputs))
    :main (some (+ :module "\n"))})

(defn comparators [data]
  (map first (filter (fn [[k v]] (= (v :kind) :&)) (pairs data))))

(defn parents [data module]
    (map first (filter (fn [[k v]] (index-of module (v :outputs))) (pairs data))))

(defn initial-memory [data comparator]
  (zipcoll 
    (parents data comparator)
    (repeat-forever :lo)))

(defn initialize-memory [data]
  (let [comparators (comparators data)]
    (loop [comparator :in comparators]
      (put-in data [comparator :memory] (initial-memory data comparator)))
    data))

(defn ->data [s]
  (var modules @{})
  (let [lines (peg/match data-peg s)]
   (loop [[kind name outputs] :in lines]
     (case kind
       :% (put modules name @{:outputs outputs :kind kind :state :off})
       :& (put modules name @{:outputs outputs :kind kind :memory @[]})
       (if (= name :broadcaster)
         (put modules name @{:outputs outputs :kind :broadcaster})
         (put modules name @{:outputs outputs})))))
  (initialize-memory modules))

(def data (->data data-string))
(def test-data-1 (->data test-string-1))
(def test-data-2 (->data test-string-2))

(defn toggle* [state]
  (if (= state :on) :off :on))

(defn hi? [state] (= state :hi))

(defn on? [state] (= state :on))

(defn array/queue [arr v] (array/insert arr 0 v))

(defn process [data [to frm sig]]
    (if-let [module (data to)]
      (let [{:kind kind :state state :outputs outputs :memory memory} module]
        (case (module :kind)
          :% (when (= sig :lo)
               (do
                 (put-in data [to :state] (toggle* state))
                 (seq [out :in outputs]
                   [out to (if (on? state) :lo :hi)])))
          :& (do
               (put-in data [to :memory frm] sig)
               (seq [out :in outputs]
                   [out to (if 
                             (all hi? (values (get-in data [to :memory])))
                             :lo
                             :hi)]))
          :broadcaster
            (seq [out :in outputs]
              [out to sig])))))

(defn deep-copy [x]
  (thaw (freeze x)))

(defn push-button [data]
    (var queue @[[:broadcaster :button :lo]])
    (var los 0)
    (var his 0)
    (while (not (empty? queue))
      (def message (array/pop queue))
      # (pp message)
      (let [[to frm sig] message]
        (if (hi? sig) 
          (++ his) 
          (++ los)))
      (when-let [messages (process data message)]
        (loop [msg :in messages]
          (array/queue queue msg))))
    [los his])

(defn part-1 [data]
  (var los 0)
  (var his 0)
  (let [data (deep-copy data)]
    (loop [_ :range [0 1000]]
      (let [[lo hi] (push-button data)]
        (+= los lo)
        (+= his hi))))
  (* los his))
        
(test (part-1 test-data-1) 32000000)
(test (part-1 test-data-2) 11687500)
(def ans1 (part-1 data)) 
(test ans1 819397964)

# Part 2
# Now we want to know which things feed into the output, rx

(defn push-button-watch [data catch-to catch-fm sentinel]
    (var queue @[[:broadcaster :button :lo]])
    (var seen false)
    (while (not (empty? queue))
      (def message (array/pop queue))
      # (pp message)
      (let [[to frm sig] message]
        (when (and (= sig sentinel) (= to catch-to) (= frm catch-fm))
          (set seen true)))
      (when-let [messages (process data message)]
        (loop [msg :in messages]
          (array/queue queue msg))))
    seen)

(defn count-until [data to fm sentinel]
  (var n 0)
  (var out false)
  (let [data (deep-copy data)]
    (loop [_ :iterate true :until out]
      (set out (push-button-watch data to fm sentinel))
      (++ n)))
  n)

(test (parents data :rx) @[:tg])
(def to-check (parents data :tg))
(test to-check @[:db :ln :tf :vq])

(defn catch [x]
  (let [data (deep-copy data)]
    (count-until data :tg x :hi)))

(def times (map catch to-check))
(test times @[3929 4091 3923 4007])
(def ans2 (product times))
(test ans2 252667369442479)


(defn -main []
  (print "Answer1:" ans1)
  (print "Answer1:" ans1))

              
      

