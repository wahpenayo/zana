(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com"
      :date "2018-01-31"
      :doc "Tests for zana.data.datum." }

    zana.test.defs.data.primitive
  
  (:require [zana.api :as z]))
;;----------------------------------------------------------------
#_(with-open [w (clojure.java.io/writer 
                  "src/test/clojure/zana/test/defs/data/primitive1.clj")]
  (binding [*out* w
            *print-meta* true]
    (require '[clojure.pprint]
             '[zana.api :as z])
    (println "(set! *warn-on-reflection* true)")
    (println "(set! *unchecked-math* :warn-on-boxed)")
    (println "(ns zana.test.defs.data.primitive1 
                  (:require [zana.api :as z]))")
    (clojure.pprint/pprint
      (macroexpand
        '(z/define-datum Primitive
           [^boolean tf 
            ^byte b 
            ^short sh 
            ^int i 
            ^long l 
            ^float f 
            ^double d 
            ^char c])))))
;;----------------------------------------------------------------
(z/define-datum Primitive
  [^boolean tf 
   ^byte b 
   ^short sh 
   ^int i 
   ^long l 
   ^float f 
   ^double d 
   ^char c])
;;----------------------------------------------------------------
(defn l2-norm2 ^double [^Primitive p]
  (let [tf (if (.tf p) (int 1) (int 0))
        b (.b p)
        sh (.sh p)
        i (.i p)
        l (.l p) 
        f (.f p)
        d (.d p)
        c (Character/getNumericValue (.c p))]
  (+ (* tf tf) (* b b) (* sh sh) (* i i) 
     (* l l) 
     (* f f) 
     (* d d) 
     (* c c))))
;;----------------------------------------------------------------
