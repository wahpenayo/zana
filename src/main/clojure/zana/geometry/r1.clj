(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-09-12"
      :doc "One-dimensional real vector space." }
    
    zana.geometry.r1
  
  (:require [zana.collections.generic :as g])
  (:import [zana.java.geometry.r1 Interval]))
;;------------------------------------------------------------------------------
;; TODO: extend Interval to Partition
;;------------------------------------------------------------------------------
(defn interval ^zana.java.geometry.r1.Interval [^double z0 ^double z1]
  (assert (< z0 z1) (str "(not (< " z0 " " z1 "))"))
  (Interval/make z0 z1))
(defn centered-interval
  ^zana.java.geometry.r1.Interval [^double z ^double dz]
  (interval (- (double z) (double dz)) (+ (double z) (double dz))))
(defn interval? [x] (instance? Interval x))
;;------------------------------------------------------------------------------
(defmethod clojure.core/print-method Interval [^Interval i ^java.io.Writer w]
  (let [l (.z0 i)
        u (.z1 i)
        ^String l (str (if (== (float l) (int l)) (int l) (float l)))
        ^String u (str (if (== (float u) (int u)) (int u) (float u)))]
    (.write w "[") (.write w l) (.write w ",") (.write w u) (.write w ")")))
;;------------------------------------------------------------------------------
(defn cspan
  (^zana.java.geometry.r1.Interval [intervals]
    (if (empty? intervals)
      nil
      (let [[^Interval i & intervals] intervals]
        (loop [intervals intervals
               zmin (.z0 i)
               zmax (.z1 i)]
          (if (empty? intervals)
            (interval zmin zmax)
            (let [[^Interval i & intervals] intervals
                  z0 (.z0 i)
                  zmin (if (< z0 zmin) z0 zmin)
                  z1 (.z1 i)
                  zmax (if (> z1 zmax) z1 zmax)]
              (recur intervals zmin zmax)))))))
  (^zana.java.geometry.r1.Interval [f data]
    (if (empty? data)
      nil
      (let [it (g/iterator data)]
        (loop [zmin Double/POSITIVE_INFINITY
               zmax Double/NEGATIVE_INFINITY]
          (if-not (.hasNext it)
            (interval zmin zmax)
            (let [^Interval i (f (.next it))
                  z0 (.z0 i)
                  zmin (if (< z0 zmin) z0 zmin)
                  z1 (.z1 i)
                  zmax (if (> z1 zmax) z1 zmax)]
              (recur zmin zmax))))))))
;(defmethod generic/cspan Number [^Number z]
;  (interval (double z) (Math/nextUp (double z))))
;
;(defmethod generic/cspan [Number Number] [^Number z0 ^Number z1]
;  (let [z0 (double z0)
;        z1 (double z1)]
;    (if (<= z0 z1)
;      (interval z0 (Math/nextUp z1))
;      (interval z1 (Math/nextUp z0)))))
;
;(defmethod generic/cspan Interval [^Interval i] i)
;
;(defmethod generic/cspan [Interval Interval] [^Interval i0 ^Interval i1]
;  (interval (Math/min (double (.z0 i0)) (double (.z0 i1)))
;            (Math/max (double (.z1 i0)) (double (.z1 i1)))))
;
;(defmethod generic/cspan [Number Interval] [^Number z0 ^Interval z1]
;  (let [z0 (double z0)
;        z00 (Math/nextUp z0)]
;    (interval (Math/min z0 (double (.z0 z1)))
;              (Math/max z00 (double (.z1 z1))))))
;
;(defmethod generic/cspan [Interval Number] [^Interval z0 ^Number z1]
;  (generic/cspan z1 z0))
;;------------------------------------------------------------------------------