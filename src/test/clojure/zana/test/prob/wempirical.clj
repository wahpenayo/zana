(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "palisades dot lakes at gmail dot com"
      :since "2017-10-24"
      :date "2017-10-24"
      :doc "Unit tests for zana.prob.wempirical."}
     
    zana.test.prob.wempirical
  
  (:require [clojure.test :as test]
            [zana.prob.wempirical :as wemp])
  (:import [java.util Arrays]
           [zana.prob.wempirical PDF]))
;;----------------------------------------------------------------
(test/deftest compact
  (let [z0 (double-array [1.0 1.0 1.0 2.0 3.0 3.0 4.0 5.0 5.0])
        w0 (double-array (alength z0) 1.0)
        w1 (double-array [3.0 1.0 2.0 1.0 2.0])
        z1 (double-array [1.0 2.0 3.0 4.0 5.0])
        [^doubles w2 ^doubles z2] (#'wemp/compact w0 z0)
        [^doubles w3 ^doubles z3] (#'wemp/compact w1 z1)]
        
    (test/is (Arrays/equals w1 w2))
    (test/is (Arrays/equals z1 z2))
    (test/is (Arrays/equals w1 w3))
    (test/is (Arrays/equals z1 z3))))
;;----------------------------------------------------------------
(test/deftest make-PDF
  (let [z0 (double-array [1.0 1.0 1.0 2.0 3.0 3.0 4.0 5.0 5.0])
        n0 (alength z0)
        w0 (double-array n0 (/ 1.0 n0))
        ^PDF pdf0 (#'wemp/make-PDF w0 z0)
        ^PDF pdf1 (#'wemp/make-PDF z0)]
    (test/is (= pdf0 pdf1))
    (test/is (== 0.0 (.probability pdf0 -1.0)))
    (test/is (== 0.0 (.probability pdf0 0.0)))
    (test/is (== (/ 3.0 n0) (.probability pdf0 1.0)))
    (test/is (== 0.0 (.probability pdf0 1.5)))
    (test/is (== (/ 1.0 n0) (.probability pdf0 2.0)))
    (test/is (== (/ 2.0 n0) (.probability pdf0 3.0)))
    (test/is (== (/ 1.0 n0) (.probability pdf0 4.0)))
    (test/is (== (/ 2.0 n0) (.probability pdf0 5.0)))
    (test/is (== 0.0 (.probability pdf0 6.0)))
    (test/is (== 0.0 (.probability pdf1 -1.0)))
    (test/is (== 0.0 (.probability pdf1 0.0)))
    (test/is (== (/ 3.0 n0) (.probability pdf1 1.0)))
    (test/is (== 0.0 (.probability pdf1 1.5)))
    (test/is (== (/ 1.0 n0) (.probability pdf1 2.0)))
    (test/is (== (/ 2.0 n0) (.probability pdf1 3.0)))
    (test/is (== (/ 1.0 n0) (.probability pdf1 4.0)))
    (test/is (== (/ 2.0 n0) (.probability pdf1 5.0)))
    (test/is (== 0.0 (.probability pdf1 6.0)))
    ))
;;----------------------------------------------------------------