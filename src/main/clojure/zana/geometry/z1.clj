(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-09-12"
      :doc "One-dimensional integer lattice." }

    zana.geometry.z1
  
  (:import [zana.java.geometry.z1 Interval]))
;;------------------------------------------------------------------------------
;; TODO: extend Interval to Partition
;; TODO: where is contains?
;;------------------------------------------------------------------------------
(defn interval ^zana.java.geometry.z1.Interval [^long z0 ^long z1]
  (assert (< z0 z1) (str "(not (< " z0 " " z1 "))"))
  (Interval/make z0 z1))
(defn interval? [x] (instance? Interval x))
;;------------------------------------------------------------------------------
(defmethod clojure.core/print-method Interval [^Interval i
                                               ^java.io.Writer w]
  (let [^String l (str (.z0 i))
        ^String u (str (.z1 i))]
    (.write w "[") (.write w l) (.write w ",") (.write w u) (.write w ")")))
;;------------------------------------------------------------------------------
