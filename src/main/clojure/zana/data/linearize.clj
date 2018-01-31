(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com"
      :date "2018-01-30"
      :doc
      "Convert records with general attribute values to elements
       of linear spaces (ie vectors in <b>R</b><sup>n</sup>
       represented by instances of <code>double[]</code>).

       AKA 'one-hot encoding'."}
    
    zana.data.linearize
  
  (:require [zana.collections.maps :as maps]
            [zana.collections.sets :as sets])
  
  (:import [java.util Arrays Collection Map]
           [java.time.temporal TemporalAccessor]
           [clojure.lang IFn IFn$OD IFn$OL]))
;;----------------------------------------------------------------
(defn- numerical? [^IFn attribute]
  (or (instance? IFn$OD attribute)
      (instance? IFn$OL attribute)))
;;----------------------------------------------------------------
;; TODO: replace this hack with something sensible
;; TODO: think through dates/times etc that aren't categorical or
;; numerical.
(defn- categorical? [^IFn attribute]
  (not (or (numerical? attribute)
           (instance? TemporalAccessor attribute))))
;;----------------------------------------------------------------
;; TODO: in special cases (eg enum valued attributes) the encoding
;; could be determined without refering to a training data set.

(defn- attribute-linear-dimension ^long [attribute training-data]
  (if (categorical? attribute)
    ;; will map distinct values to n+1 corners of n simplex in R^n 
    (dec (sets/count-distinct attribute training-data))
    ;; must be numerical
    1))
;;----------------------------------------------------------------
;; TODO: Returns a function that closes over and returns
;; that same mutable double[] instances. Not safe. Replace with
;; immutable vector objects?
;; TODO: in special cases (eg enum valued attributes) the encoding
;; could be determined without refering to a training data set.

(defn- categorical-attribute-linearizer
  
  "AKA one-hot encoding.

   Return a function that maps each of <code>n</code> distinct
   values of a categorical <code>attribute</code> to the linear
   (vector) coordinates of one of the <code>n+1</code> corners of 
   the unit <code>n</code> simplex (in <b>R</b><sup>n</sup>).
   The coordinates are returned as <code>double[n]</code>.

   Only the values present in <code>training-data</code> are
   explicitly mapped. The most frequent value is mapped to the
   origin (a <code>double[n]</code> whose elements are all
   <code>0.0</code>). The remaining values from <code>data</code>
   are mapped to canonical basis vectors in order of decreasing 
   frequency.

   Any value not occuring in <code>data</code> will also be mapped 
   to the origin. Predictive models using this encoding will treat 
   previously unseen values as though they were the most common 
   value in the training data, a simple form of imputation."
  
  ^IFn [^IFn attribute ^Collection training-data]
  
  ;; TODO: check for too many distinct values?
  (assert (categorical? attribute))
  (let [f (maps/frequencies attribute training-data)
        f (rest (sort-by #(- (val %)) f))
        n (count f)
        _ (assert (== n (attribute-linear-dimension 
                          attribute training-data)))
        origin (double-array n)
        e (fn e ^doubles [^long i]
            (let [ei (double-array n)]
              (aset-double ei i 1.0)
              ei))
        f (into {} (map-indexed (fn [[k v] i] [k (e i)]) f))]
    (fn linearize-value ^doubles [record] 
      (get f (attribute record) origin))))
;;----------------------------------------------------------------
(defn- attribute-linearizer ^IFn [attribute training-data]
  (cond 
    (numerical? attribute) 
    attribute
    
    (categorical? attribute)
    (categorical-attribute-linearizer attribute training-data)
    
    :else
    (throw (IllegalArgumentException.
             (print-str "can't handle" attribute)))))
;;----------------------------------------------------------------
;; TODO: in special cases (eg all numberical or enum valued 
;; attributes) the encoding
;; could be determined without refering to a training data set.

(defn- record-linear-dimension ^long [attributes training-data]
  (reduce + (map #(attribute-linear-dimension % training-data)
                 attributes)))
;;----------------------------------------------------------------
;; TODO: in special cases (eg all numberical or enum valued 
;; attributes) the encoding
;; could be determined without refering to a training data set.

(defn record-linearizer
  
  "AKA one-hot encoding.

   Return a function that maps a the supplied attributes of 
   a record object into an element of a linear space (ie a vector
   in <b>R</b><sup>n</sup> represented by <code>double[]</code>).
   Numerical attributes are mapped to single coordinates of the
   output <code>double[]</code>. Categorical attributes use a 
   simplex corner (one-hot encoding) mapping based on the values
   present in <code>training-data</code>. 
   See [attribute-linearizer] for details.
   Dates and times are currently not supported."
  
  (^IFn [attributes ^Collection training-data]
    (let [n (record-linear-dimension attributes training-data)
          linearizers (mapv 
                        #(attribute-linearizer % training-data)
                        attributes)]
      (fn linearize-record ^doubles [record]
        (let [d (double-array n)]
        (loop [i (int 0)
               attributes attributes]
          (get f k origin)))))
  ;;----------------------------------------------------------------
  