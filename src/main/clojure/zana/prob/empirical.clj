(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-09-09"
      :doc "Real-valued empirical cdf and quantile functions." }
    
    zana.prob.empirical
  
  (:require [zana.collections.generic :as g]
            [zana.geometry.r1 :as r1]
            [zana.functions.inverse :as inverse]
            [zana.functions.generic :as generic])
  (:import [java.util Arrays]
           [zana.functions.inverse Invertible]
           [zana.java.math Statistics]))
;;------------------------------------------------------------------------------
;; TODO: categorical attributes?
;;------------------------------------------------------------------------------
(declare quantile)
;;------------------------------------------------------------------------------
(defrecord EmpiricalCDF [^doubles zs]
  clojure.lang.Fn
  clojure.lang.IFn$DD
  (invokePrim [this q]
    (assert (not (Double/isNaN q)))
    (Statistics/presortedCDF zs q))
  clojure.lang.IFn
  (invoke [this q] (.invokePrim this (double q)))
  zana.functions.inverse.Invertible
  (inverse [this] (quantile zs)))
;;------------------------------------------------------------------------------
(defmethod generic/support EmpiricalCDF [^EmpiricalCDF f]
  (let [^doubles zs (.zs f)]
    (r1/interval (aget zs 0) (aget zs (dec (alength zs))))))
(defmethod generic/range EmpiricalCDF [^EmpiricalCDF f]
  (r1/interval 0.0 (Math/nextUp 1.0)))
;;------------------------------------------------------------------------------
(defn cdf
  
  "Return a function that maps <code>double</code> to [0.0,1.0], which 
   is the empirical cumulative distribution function of the <code>doubles</code>
   in <code>zs</code> or <code>(map-to-doubles z data)</code>.<br>
   Return nil when there's no data<br>
   **TODO**: handle non-finite data with mass at POSITIVE_INFINITY (and/or NaN)."
  
  (^clojure.lang.IFn$DD [^doubles zs]
    (when (< 0 (alength zs))
      (assert (every? #(not (Double/isNaN %)) zs))
      (let [zs (aclone zs)]
        (Arrays/sort (doubles zs))
        (EmpiricalCDF. zs))))
  (^clojure.lang.IFn$DD [z data]
    (when (< 0 (g/count data))
      (let [zs (g/map-to-doubles z data)]
        (Arrays/sort zs)
        (EmpiricalCDF. zs)))))
;;------------------------------------------------------------------------------
(defrecord EmpiricalQuantileF [^doubles zs]
  clojure.lang.Fn
  clojure.lang.IFn$DD
  (invokePrim [this p]
    (assert (<= 0.0 p 1.0))
    (Statistics/presortedQuantile zs p))
  clojure.lang.IFn
  (invoke [this p] (.invokePrim this (double p)))
  zana.functions.inverse.Invertible
  (inverse [this] (EmpiricalCDF. zs)))
;;------------------------------------------------------------------------------
(defmethod generic/support EmpiricalQuantileF [^EmpiricalQuantileF f]
  (r1/interval 0.0 (Math/nextUp 1.0)))
(defmethod generic/range EmpiricalQuantileF [^EmpiricalQuantileF f]
  (let [^doubles zs (.zs f)]
    (r1/interval (aget zs 0) (aget zs (dec (alength zs))))))
;;------------------------------------------------------------------------------
(defn quantile
  
  "Return a function that maps [0.0,1.0] to <code>double</code>, which 
   is the empirical quantile function (a pseudo-inverse of the cdf) of the 
   <code>doubles</code> in <code>zs</code> or <code>(map-to-doubles z data)</code>.<br>
   Return nil when there's no data<br>
   **TODO**: handle non-finite data with mass at POSITIVE_INFINITY (and/or NaN)."
  
  (^clojure.lang.IFn$DD [^doubles zs]
    (when (< 0 (alength zs))
      (assert (every? #(not (Double/isNaN %)) zs))
      (let [zs (aclone zs)]
        (Arrays/sort (doubles zs)) ;; TODO: non-destructive array sort
        (EmpiricalQuantileF. zs))))
  (^clojure.lang.IFn$DD [z data]
    (when (< 0 (g/count data))
      (let [zs (g/map-to-doubles z data)]
        (Arrays/sort zs);; TODO: non-destructive array sort
        (EmpiricalQuantileF. zs)))))
;;------------------------------------------------------------------------------