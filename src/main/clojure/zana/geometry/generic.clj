(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-10-10"
      :doc "Generic functions related to multiple geometries." }
    
    zana.geometry.generic)
;;------------------------------------------------------------------------------
(defn interval-min ^double [i]
  (cond (instance? zana.java.geometry.r1.Interval i)
        (.z0 ^zana.java.geometry.r1.Interval i)
        (instance? zana.java.geometry.z1.Interval i)
        (double (.z0 ^zana.java.geometry.z1.Interval i))
        :else
        (throw (IllegalArgumentException. (print-str "not an interval:" i)))))

(defn interval-max ^double [i]
  (cond (instance? zana.java.geometry.r1.Interval i)
        (.z1 ^zana.java.geometry.r1.Interval i)
        (instance? zana.java.geometry.z1.Interval i)
        (double (.z1 ^zana.java.geometry.z1.Interval i))
        :else
        (throw (IllegalArgumentException. (print-str "not an interval:" i)))))

(defn interval-length ^double [i] (- (interval-max i) (interval-min i)))

(defn interval-contains? [i x] 
  (cond (instance? zana.java.geometry.r1.Interval i)
        (.contains ^zana.java.geometry.r1.Interval i (double x))
        (instance? zana.java.geometry.z1.Interval i)
        (.contains ^zana.java.geometry.z1.Interval i (long x))
        :else
        (throw (IllegalArgumentException. (print-str "not an interval:" i)))))
;;------------------------------------------------------------------------------