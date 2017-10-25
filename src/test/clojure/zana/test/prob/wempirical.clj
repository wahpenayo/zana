(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "palisades dot lakes at gmail dot com"
      :date "2017-10-24"
      :doc "Unit tests for zana.prob.wempirical."}
     
    zana.test.prob.wempirical
  
  (:require [clojure.test :as test]
            [zana.prob.wempirical :as wemp])
  (:import [java.util Arrays]))
;;------------------------------------------------------------------------------
(test/deftest compact
  (let [w0 (double-array 9 1.0)
        z0 (double-array [1.0 1.0 1.0 2.0 3.0 3.0 4.0 5.0 5.0])
        w1 (double-array [3.0 1.0 2.0 1.0 2.0])
        z1 (double-array [1.0 2.0 3.0 4.0 5.0])
        [^doubles w2 ^doubles z2] (#'wemp/compact w0 z0)
        [^doubles w3 ^doubles z3] (#'wemp/compact w1 z1)]
        
    (test/is (Arrays/equals w1 w2))
    (test/is (Arrays/equals z1 z2))
    (test/is (Arrays/equals w1 w3))
    (test/is (Arrays/equals z1 z3))))
;;------------------------------------------------------------------------------