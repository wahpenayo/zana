(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "palisades dot lakes at gmail dot com"
      :since "2017-10-24"
      :date "2017-10-25"
      :doc "Unit tests for zana.prob.measure."}
    
    zana.test.prob.measure
  
  (:require [clojure.test :as test]
            [zana.prob.measure :as zpm])
  (:import [java.util Arrays]
           [zana.prob.measure WECDF WEPDF]))
;; mvn -Dtest=zana.test.prob.measure clojure:test
;;----------------------------------------------------------------
;; TODO: move elsewhere
(defn- approximately== 
  ([^double x ^double y ^double delta]
    (<= (Math/abs (- x y)) delta))
  ([^double x ^double y]
    (approximately== 
      x y (Math/ulp (* 2.0 (+ (Math/abs x) (Math/abs y)))))))
;;----------------------------------------------------------------
(let [^doubles z0 (double-array 
                    [1.0 2.0 3.0 1.0 3.0 5.0 4.0 1.0 5.0])
      n0 (alength ^doubles z0)
      ^doubles w0 (double-array n0 (/ 1.0 n0))
      ^doubles z1 (double-array [1.0 2.0 3.0 4.0 5.0])
      ^doubles w1 (double-array [(/ 3.0 n0) (/ 1.0 n0) (/ 2.0 n0) 
                                 (/ 1.0 n0) (/ 2.0 n0)])
      ^WEPDF pdf0 (#'zpm/make-WEPDF z0 w0)
      ^WEPDF pdf1 (#'zpm/make-WEPDF z0)
      ^WECDF cdf0 (#'zpm/make-WECDF z0 w0)
      ^WECDF cdf1 (#'zpm/make-WECDF z0)]
  ;;----------------------------------------------------------------
  (test/deftest compact
    (let [[^doubles z0 ^doubles w0] (#'zpm/quicksort z0 w0)
          [^doubles z1 ^doubles w1] (#'zpm/quicksort z1 w1)
          [^doubles z2 ^doubles w2] (#'zpm/compact z0 w0)
          [^doubles z3 ^doubles w3] (#'zpm/compact z1 w1)]
      (test/is (Arrays/equals w1 w2))
      (test/is (Arrays/equals z1 z2))
      (test/is (Arrays/equals w1 w3))
      (test/is (Arrays/equals z1 z3))))
  ;;----------------------------------------------------------------
  (test/deftest make
    (test/is (= pdf0 pdf1))
    (test/is (= cdf0 cdf1)))
  ;;----------------------------------------------------------------
  ;; TODO: better sum algorithm in cdf to get rid of approximately==
  (test/deftest pointmass
    (test/is (== 0.0 (.pointmass pdf0 -1.0)))
    (test/is (== 0.0 (.pointmass pdf0 0.0)))
    (test/is (== (/ 3.0 n0) (.pointmass pdf0 1.0)))
    (test/is (== 0.0 (.pointmass pdf0 1.5)))
    (test/is (== (/ 1.0 n0) (.pointmass pdf0 2.0)))
    (test/is (== (/ 2.0 n0) (.pointmass pdf0 3.0)))
    (test/is (== (/ 1.0 n0) (.pointmass pdf0 4.0)))
    (test/is (== (/ 2.0 n0) (.pointmass pdf0 5.0)))
    (test/is (== 0.0 (.pointmass pdf0 6.0)))
    
    (test/is (== 0.0 (.pointmass pdf1 -1.0)))
    (test/is (== 0.0 (.pointmass pdf1 0.0)))
    (test/is (== (/ 3.0 n0) (.pointmass pdf1 1.0)))
    (test/is (== 0.0 (.pointmass pdf1 1.5)))
    (test/is (== (/ 1.0 n0) (.pointmass pdf1 2.0)))
    (test/is (== (/ 2.0 n0) (.pointmass pdf1 3.0)))
    (test/is (== (/ 1.0 n0) (.pointmass pdf1 4.0)))
    (test/is (== (/ 2.0 n0) (.pointmass pdf1 5.0)))
    (test/is (== 0.0 (.pointmass pdf1 6.0)))
    
    (test/is (== 0.0 (.pointmass cdf0 -1.0)))
    (test/is (== 0.0 (.pointmass cdf0 0.0)))
    (test/is (== (/ 3.0 n0) (.pointmass cdf0 1.0)))
    (test/is (== 0.0 (.pointmass cdf0 1.5)))
    (test/is (== (/ 1.0 n0) (.pointmass cdf0 2.0)))
    (test/is (== (/ 2.0 n0) (.pointmass cdf0 3.0)))
    (test/is (approximately== (/ 1.0 n0) (.pointmass cdf0 4.0)))
    (test/is (== (/ 2.0 n0) (.pointmass cdf0 5.0)))
    (test/is (== 0.0 (.pointmass cdf0 6.0)))
    
    (test/is (== 0.0 (.pointmass cdf1 -1.0)))
    (test/is (== 0.0 (.pointmass cdf1 0.0)))
    (test/is (== (/ 3.0 n0) (.pointmass cdf1 1.0)))
    (test/is (== 0.0 (.pointmass cdf1 1.5)))
    (test/is (== (/ 1.0 n0) (.pointmass cdf1 2.0)))
    (test/is (== (/ 2.0 n0) (.pointmass cdf1 3.0)))
    (test/is (approximately== (/ 1.0 n0) (.pointmass cdf1 4.0)))
    (test/is (== (/ 2.0 n0) (.pointmass cdf1 5.0)))
    (test/is (== 0.0 (.pointmass cdf1 6.0))))
  ;;----------------------------------------------------------------
  ;; TODO: better sum algorithm in cdf to get rid of approximately==
  (test/deftest cdf
    (test/is (== 0.0 (.cdf pdf0 -1.0)))
    (test/is (== 0.0 (.cdf pdf0 0.0)))
    (test/is (== (/ 3.0 n0) (.cdf pdf0 1.0)))
    (test/is (== (/ 3.0 n0) (.cdf pdf0 1.5)))
    (test/is (== (/ 4.0 n0) (.cdf pdf0 2.0)))
    (test/is (== (/ 6.0 n0) (.cdf pdf0 3.0)))
    (test/is (approximately== (/ 7.0 n0) (.cdf pdf0 4.0)))
    (test/is (approximately== (/ 9.0 n0) (.cdf pdf0 5.0)))
    (test/is (approximately== (/ 9.0 n0) (.cdf pdf0 6.0)))
    
    (test/is (== 0.0 (.cdf pdf1 -1.0)))
    (test/is (== 0.0 (.cdf pdf1 0.0)))
    (test/is (== (/ 3.0 n0) (.cdf pdf1 1.0)))
    (test/is (== (/ 3.0 n0) (.cdf pdf1 1.5)))
    (test/is (== (/ 4.0 n0) (.cdf pdf1 2.0)))
    (test/is (== (/ 6.0 n0) (.cdf pdf1 3.0)))
    (test/is (approximately== (/ 7.0 n0) (.cdf pdf1 4.0)))
    (test/is (approximately== (/ 9.0 n0) (.cdf pdf1 5.0)))
    (test/is (approximately== (/ 9.0 n0) (.cdf pdf1 6.0)))
    (test/is (== 0.0 (.cdf pdf0 -1.0)))
    
    (test/is (== 0.0 (.cdf cdf0 0.0)))
    (test/is (== (/ 3.0 n0) (.cdf cdf0 1.0)))
    (test/is (== (/ 3.0 n0) (.cdf cdf0 1.5)))
    (test/is (== (/ 4.0 n0) (.cdf cdf0 2.0)))
    (test/is (== (/ 6.0 n0) (.cdf cdf0 3.0)))
    (test/is (approximately== (/ 7.0 n0) (.cdf cdf0 4.0)))
    (test/is (approximately== (/ 9.0 n0) (.cdf cdf0 5.0)))
    (test/is (approximately== (/ 9.0 n0) (.cdf cdf0 6.0)))
    
    (test/is (== 0.0 (.cdf cdf1 -1.0)))
    (test/is (== 0.0 (.cdf cdf1 0.0)))
    (test/is (== (/ 3.0 n0) (.cdf cdf1 1.0)))
    (test/is (== (/ 3.0 n0) (.cdf cdf1 1.5)))
    (test/is (== (/ 4.0 n0) (.cdf cdf1 2.0)))
    (test/is (== (/ 6.0 n0) (.cdf cdf1 3.0)))
    (test/is (approximately== (/ 7.0 n0) (.cdf cdf1 4.0)))
    (test/is (approximately== (/ 9.0 n0) (.cdf cdf1 5.0)))
    (test/is (approximately== (/ 9.0 n0) (.cdf cdf1 6.0)))
    )
  ;;----------------------------------------------------------------
  )
