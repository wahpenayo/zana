(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com"
      :date "2018-02-01"
      :doc
      "Convert records with general attribute values to elements
       of affine spaces (ie points in <b>E</b><sup>n</sup>
       represented by instances of <code>double[n+1]</code>),
       holding homogeneous coordinates, with the last element of
       the array being the redundant weight/constant term.

       AKA 'one-hot encoding'."}
    
    zana.data.homogenize
  
  (:require [zana.data.linearize :as linearize])
  (:import [clojure.lang IFn]))
;;----------------------------------------------------------------
;; TODO: in special cases (eg all numberical or enum valued 
;; attributes) the encoding could be determined without refering 
;; to a training data set.
;; TODO: linear models will need some way to serialize a 
;; linearizers, so maybe they should be built from instances of 
;; explicitly serializable classes, rather than closures.

(defn record-homogenizer
  
  "AKA one-hot encoding.

   Return a function that maps a the supplied attributes of 
   a record object into an element of an affine space (ie a point
   in <b>E</b><sup>n</sup> represented by homogeneous coordinates
   in a <code>double[n+1]</code>, with the last coordinate
   representing the redundant weight/constant term).
   Numerical attributes are mapped to single coordinates of the
   output <code>double[n+1]</code>. Categorical attributes use a 
   simplex corner (one-hot encoding) mapping based on the values
   present in <code>training-data</code>. 
   See [attribute-linearizer] for details.
   Dates and times are currently not supported."
  
  ^IFn [attributes training-data]
  ;; TODO: save a temp array allocate and reclaim by implementing 
  ;; this from first principles.
  ;; TODO: implement fn as instance of serializable class
  (let [linearizer (linearize/record-linearizer 
                     attributes training-data)]
    (fn homogenize-record ^doubles [record]
      (let [^doubles v (linearizer record)
            n (int (alength v))
            p (double-array (inc n))]
        (aset-double p n 1.0)
        (System/arraycopy v 0 p 0 n)
        p))))
;;----------------------------------------------------------------
  