(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "palisades dot lakes at gmail dot com"
      :since "2017-10-24"
      :date "2017-11-06"
      :doc "Unit tests for zana.prob.measure."}
    
    zana.test.prob.measure
  
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [clojure.test :as test]
            [zana.prob.measure :as zpm]
            [zana.api :as z])
  (:import [java.util Arrays]))
;; mvn -Dtest=zana.test.prob.measure clojure:test
;; TODO: randomized data for larger tests
;;----------------------------------------------------------------
(def nss (str *ns*))
(defn setup [tests]
  (def z0 (double-array 
            [1.0 2.0 3.0 1.0 3.0 5.0 4.0 1.0 5.0]))
  (def ^double n0 (double (alength ^doubles z0)))
  (def w0 (double-array n0 1.0))
  (def z1 (double-array [1.0 2.0 3.0 4.0 5.0]))
  (def w1 (double-array [3.0 1.0 2.0 1.0 2.0]))
  (def pdf0 (z/make-wepdf z0 w0))
  (def pdf1 (z/make-wepdf z0))
  (def pdf2 (z/average-wepdfs pdf0 pdf1))
  (def pdf3 (z/average-wepdfs pdf0 pdf1 pdf2))
  (def pdf4 (z/average-wepdfs 
              (z/make-wepdf (double-array [1.0 2.0 3.0]))
              (z/make-wepdf (double-array [1.0 3.0 5.0]))
              (z/make-wepdf (double-array [4.0 1.0 5.0]))))
  (def cdf5 (z/make-wecdf z0 w0))
  (def cdf6 (z/make-wecdf z0))
  (def cdf7 (z/wepdf-to-wecdf pdf1))
  (def pdf8 (z/wecdf-to-wepdf cdf6))
  (def rpms [pdf0 pdf1 pdf2 pdf3 pdf4 cdf5 cdf6 cdf7 pdf8])
  (tests))
(test/use-fixtures :once setup)
;;----------------------------------------------------------------
(test/deftest make
   (test/is (z/approximatelyEqual pdf0 pdf1 pdf2 pdf3 pdf4 pdf8)
            #_(str "\n0 " pdf0 "\n1 " pdf1 
                   "\n2 " pdf2 "\n3 " pdf3
                   "\n4 " pdf4 "\n5 " pdf8
                   ))
   (test/is (z/approximatelyEqual cdf5 cdf6 cdf7)
            #_(str "\n" cdf5 "\n" cdf6 "\n" cdf7)))
;;----------------------------------------------------------------
(defn- serialization-test [rpm i]
  (let [tokens (s/split nss #"\.")
        folder (apply io/file "tst" tokens)
        filename (str (.getSimpleName (class rpm)) i)
        #_(println filename)
        edn-file (io/file folder (str filename ".edn.gz"))
        _ (io/make-parents edn-file)
        _ (z/write-edn rpm edn-file)
        ;;_ (taiga/pprint-model model pretty-file)
        edn-rpm (z/read-edn edn-file)]
    (test/is (= rpm edn-rpm))))
(test/deftest serialization
  (z/mapc serialization-test rpms (range (count rpms))))
;;----------------------------------------------------------------
;; TODO: better sum algorithm in cdf to get rid of z/approximately==
(test/deftest quantile
  (let [n0 (double n0)]
    (doseq [rpm rpms] 
      #_(dotimes [i 5]
          (let [zi (aget z1 i)
                wi (aget w1 i)
                pi (z/cdf rpm zi)]
            (println i zi wi pi (z/quantile rpm pi))))
      (test/is (== Double/NEGATIVE_INFINITY (z/quantile rpm 0.0)))
      (test/is (== 1.0 (z/quantile rpm (/ 1.0 n0))))
      (test/is (== 1.0 (z/quantile rpm (/ 2.0 n0))))
      (test/is (== 1.0 (z/quantile rpm (/ 3.0 n0))))
      (test/is (== 2.0 (z/quantile rpm (/ 3.5 n0))))
      (test/is (== 2.0 (z/quantile rpm (/ 4.0 n0))))
      (test/is (== 3.0 (z/quantile rpm (/ 5.0 n0))))
      (test/is (== 3.0 (z/quantile rpm (/ 6.0 n0))))
      (test/is (== 4.0 (z/quantile rpm (/ 6.5 n0))))
      (test/is (== 4.0 (z/quantile rpm (/ 7.0 n0))))
      (test/is (== 5.0 (z/quantile rpm (/ 8.0 n0))))
      (test/is (== 5.0 (z/quantile rpm (/ 9.0 n0))))
      (test/is (== 5.0 (z/quantile rpm 1.0))))))
;;----------------------------------------------------------------
;; TODO: better sum algorithm in cdf to get rid of z/approximately==
(test/deftest pointmass
  (let [n0 (double n0)]
    (doseq [rpm rpms] 
      #_(println rpm)
      (test/is (== 0.0 (z/pointmass rpm -1.0)))
      (test/is (== 0.0 (z/pointmass rpm 0.0)))
      (test/is (z/approximately== (/ 3.0 n0) (z/pointmass rpm 1.0)))
      (test/is (== 0.0 (z/pointmass rpm 1.5)))
      (test/is (z/approximately== (/ 1.0 n0) (z/pointmass rpm 2.0)))
      (test/is (z/approximately== (/ 2.0 n0) (z/pointmass rpm 3.0)))
      (test/is (z/approximately== (/ 1.0 n0) (z/pointmass rpm 4.0)))
      (test/is (z/approximately== (/ 2.0 n0) (z/pointmass rpm 5.0)))
      (test/is (== 0.0 (z/pointmass rpm 6.0))))))
;;----------------------------------------------------------------
;; TODO: better sum algorithm in cdf to get rid of z/approximately==
(test/deftest cdf
  (let [n0 (double n0)]
    (doseq [rpm rpms] 
      (test/is (== 0.0 (z/cdf rpm -1.0)))
      (test/is (== 0.0 (z/cdf rpm 0.0)))
      (test/is (z/approximately== (/ 3.0 n0) (z/cdf rpm 1.0)))
      (test/is (z/approximately== (/ 3.0 n0) (z/cdf rpm 1.5)))
      (test/is (z/approximately== (/ 4.0 n0) (z/cdf rpm 2.0)))
      (test/is (z/approximately== (/ 6.0 n0) (z/cdf rpm 3.0)))
      (test/is (z/approximately== (/ 7.0 n0) (z/cdf rpm 4.0)))
      (test/is (z/approximately== (/ 9.0 n0) (z/cdf rpm 5.0)))
      (test/is (z/approximately== (/ 9.0 n0) (z/cdf rpm 6.0))))))
;;----------------------------------------------------------------
