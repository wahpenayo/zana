(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "palisades dot lakes at gmail dot com"
      :since "2017-10-24"
      :date "2017-11-01"
      :doc "Unit tests for zana.prob.measure."}
    
    zana.test.prob.measure
  
  (:require [clojure.test :as test]
            [zana.prob.measure :as zpm])
  (:import [java.util Arrays]
           [zana.java.prob RealProbabilityMeasure]
           [zana.prob.measure WECDF WEPDF]))
;; mvn -Dtest=zana.test.prob.measure clojure:test
;; TODO: randomized data for larger tests
;;----------------------------------------------------------------
(let [^doubles z0 (double-array 
                    [1.0 2.0 3.0 1.0 3.0 5.0 4.0 1.0 5.0])
      n0 (alength ^doubles z0)
      ^doubles w0 (double-array n0 1.0)
      ^doubles z1 (double-array [1.0 2.0 3.0 4.0 5.0])
      ^doubles w1 (double-array [3.0 1.0 2.0 1.0 2.0])
      ^WEPDF pdf0 (zpm/make-wepdf z0 w0)
      ^WEPDF pdf1 (zpm/make-wepdf z0)
      ^WEPDF pdf2 (zpm/average-wepdfs pdf0 pdf1)
      ^WEPDF pdf3 (zpm/average-wepdfs pdf0 pdf1 pdf2)
      ^WEPDF pdf4 (zpm/average-wepdfs 
                     (zpm/make-wepdf (double-array [1.0 2.0 3.0]))
                     (zpm/make-wepdf (double-array [1.0 3.0 5.0]))
                     (zpm/make-wepdf (double-array [4.0 1.0 5.0])))
      ^WECDF cdf0 (zpm/make-WECDF z0 w0)
      ^WECDF cdf1 (zpm/make-WECDF z0)
      ^WECDF cdf2 (zpm/wepdf-to-wecdf pdf1)
      ^WEPDF pdf5 (zpm/wecdf-to-wepdf cdf1)
      rpms [pdf0 pdf1 pdf2 pdf3 pdf4 cdf0 cdf1 cdf2 pdf4]]
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
    (test/is (= pdf0 pdf1 pdf2 pdf3 pdf4 pdf5)
             (str "\n" pdf0 "\n" pdf1 "\n" pdf2 "\n" pdf3
                  "\n" pdf4 "\n" pdf5))
    (test/is (= cdf0 cdf1 cdf2)
             (str "\n" cdf0 "\n" cdf1 "\n" cdf2)))
  ;;----------------------------------------------------------------
  ;; TODO: better sum algorithm in cdf to get rid of #'zpm/approximately==
  (test/deftest quantile
    (doseq [^RealProbabilityMeasure rpm rpms] 
      #_(dotimes [i 5]
          (let [zi (aget z1 i)
                wi (aget w1 i)
                pi (.cdf rpm zi)]
            (println i zi wi pi (.quantile rpm pi))))
      (test/is (== Double/NEGATIVE_INFINITY (.quantile rpm 0.0)))
      (test/is (== 1.0 (.quantile rpm (/ 1.0 n0))))
      (test/is (== 1.0 (.quantile rpm (/ 2.0 n0))))
      (test/is (== 1.0 (.quantile rpm (/ 3.0 n0))))
      (test/is (== 2.0 (.quantile rpm (/ 3.5 n0))))
      (test/is (== 2.0 (.quantile rpm (/ 4.0 n0))))
      (test/is (== 3.0 (.quantile rpm (/ 5.0 n0))))
      (test/is (== 3.0 (.quantile rpm (/ 6.0 n0))))
      (test/is (== 4.0 (.quantile rpm (/ 6.5 n0))))
      (test/is (== 4.0 (.quantile rpm (/ 7.0 n0))))
      (test/is (== 5.0 (.quantile rpm (/ 8.0 n0))))
      (test/is (== 5.0 (.quantile rpm (/ 9.0 n0))))
      (test/is (== 5.0 (.quantile rpm 1.0)))))
  ;;----------------------------------------------------------------
  ;; TODO: better sum algorithm in cdf to get rid of #'zpm/approximately==
  (test/deftest pointmass
    (doseq [^RealProbabilityMeasure rpm rpms] 
      #_(println rpm)
      (test/is (== 0.0 (.pointmass rpm -1.0)))
      (test/is (== 0.0 (.pointmass rpm 0.0)))
      (test/is (#'zpm/approximately== (/ 3.0 n0) (.pointmass rpm 1.0)))
      (test/is (== 0.0 (.pointmass rpm 1.5)))
      (test/is (#'zpm/approximately== (/ 1.0 n0) (.pointmass rpm 2.0)))
      (test/is (#'zpm/approximately== (/ 2.0 n0) (.pointmass rpm 3.0)))
      (test/is (#'zpm/approximately== (/ 1.0 n0) (.pointmass rpm 4.0)))
      (test/is (#'zpm/approximately== (/ 2.0 n0) (.pointmass rpm 5.0)))
      (test/is (== 0.0 (.pointmass rpm 6.0)))))
  ;;----------------------------------------------------------------
  ;; TODO: better sum algorithm in cdf to get rid of #'zpm/approximately==
  (test/deftest cdf
     (doseq [^RealProbabilityMeasure rpm rpms] 
       (test/is (== 0.0 (.cdf rpm -1.0)))
       (test/is (== 0.0 (.cdf rpm 0.0)))
       (test/is (#'zpm/approximately== (/ 3.0 n0) (.cdf rpm 1.0)))
       (test/is (#'zpm/approximately== (/ 3.0 n0) (.cdf rpm 1.5)))
       (test/is (#'zpm/approximately== (/ 4.0 n0) (.cdf rpm 2.0)))
       (test/is (#'zpm/approximately== (/ 6.0 n0) (.cdf rpm 3.0)))
       (test/is (#'zpm/approximately== (/ 7.0 n0) (.cdf rpm 4.0)))
       (test/is (#'zpm/approximately== (/ 9.0 n0) (.cdf rpm 5.0)))
       (test/is (#'zpm/approximately== (/ 9.0 n0) (.cdf rpm 6.0)))))
  ;;----------------------------------------------------------------
  )
