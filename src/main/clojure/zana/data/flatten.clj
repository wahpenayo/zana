(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com"
      :date "2018-02-05"
      :doc
      "Convert records with general attribute values to elements
       of linear or affine spaces: 
       vectors in <b>R</b><sup>n</sup>
       represented by instances of <code>double[n]</code>),
       or points in <b>E</b><sup>n</sup>
       represented by <code>double[n+1]</code>) holding 
       homogeneous coordinates.
       
       AKA 'one-hot encoding'."}
    
    zana.data.flatten
  
  (:require [clojure.pprint :as pp]
            [zana.collections.maps :as maps]
            [zana.collections.sets :as sets])
  
  (:import [java.util Arrays Collection List Map]
           [java.time.temporal TemporalAccessor]
           [clojure.lang IFn IFn$OD IFn$OL]
           [zana.java.data Homogenizer Linearizer]))
;;----------------------------------------------------------------
(defn- numerical? [attribute]
  (or (instance? IFn$OD attribute)
      (instance? IFn$OL attribute)))
;;----------------------------------------------------------------
;; TODO: replace this hack with something sensible
;; TODO: think through dates/times etc that aren't categorical or
;; numerical.
(defn- categorical? [attribute]
  (not (or (numerical? attribute)
           (instance? TemporalAccessor attribute))))
;;----------------------------------------------------------------
;; TODO: in special cases (eg enum valued attributes) the encoding
;; could be determined without refering to a training data set.

#_(defn- attribute-linear-dimension ^long [attribute training-data]
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

#_(defn- categorical-attribute-linearizer
    
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
    
    ^IFn [attribute training-data]
    
    ;; TODO: check for too many distinct values?
    (assert (categorical? attribute))
    (let [n (count f)
          _ (assert (== n (attribute-linear-dimension 
                            attribute training-data)))
          origin (double-array n)
          e (fn e ^doubles [^long i]
              (let [ei (double-array n)]
                (aset-double ei i 1.0)
                ei))
          f  (into {} (map-indexed (fn [i [k _]] [k (e i)]) f))]
      (fn linearize-value ^doubles [record] 
        (get f (attribute record) origin))))
;;----------------------------------------------------------------
#_(defn- attribute-linearizer ^IFn [attribute training-data]
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

#_(defn record-linear-dimension ^long [attributes training-data]
    (reduce + (map #(attribute-linear-dimension % training-data)
                   attributes)))
;;----------------------------------------------------------------
#_(defn- linearize-numerical ^long [linearizer record ^long i d]
    (aset-double d i (double (linearizer record)))
    (inc i))

#_(defn- linearize-categorical ^long [linearizer record ^long i d]
    (let [^doubles tmp (linearizer record)
          n (int (alength tmp))]
      (System/arraycopy tmp 0 d i n)
      (+ i n)))

#_(defn- linearize1 ^long [linearizer record ^long i d]
    (cond 
      (numerical? linearizer) 
      (linearize-numerical linearizer record i d)
      
      (categorical? linearizer)
      (linearize-categorical linearizer record i d)
      
      :else
      (throw (IllegalArgumentException.
               (print-str "can't handle" linearizer)))))
;;----------------------------------------------------------------
(defn- attribute-values ^List [attribute training-data]
  (cond 
    (numerical? attribute) 
    [attribute 
     Double/TYPE]
    
    (categorical? attribute)
    [attribute
     ;; drop the most frequent value, so it maps to the origin
     (let [freq (mapv
                  key
                  (sort-by 
                    #(- (int (val %)))
                    (maps/frequencies attribute training-data)))]
       #_(pp/pprint freq)
       (rest freq))]
    
    :else
    (throw (IllegalArgumentException.
             (print-str "can't handle" attribute)))))
;;----------------------------------------------------------------

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
  
  (^IFn [^String name ^List attributeValues]
    (Linearizer/make name attributeValues))
  (^IFn [^String name ^List attributes ^List training-data]
    (let [av (mapv #(attribute-values % training-data) attributes)] 
      #_(pp/pprint av)
      (record-linearizer name av ))))
;;----------------------------------------------------------------

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
  
  (^IFn [^String name ^List attributeValues]
    (Homogenizer/make name attributeValues))
  (^IFn [^String name ^List attributes ^List training-data]
    (record-homogenizer 
      name
      (mapv #(attribute-values % training-data) attributes))))
;;----------------------------------------------------------------
;; EDN io
;;----------------------------------------------------------------
;(defn map->CategoricalAttributeLinearizer [m] 
;  (CategoricalAttributeLinearizer/make 
;    ^String (:name m)
;    ^List (:))
;(defn map<-Linearizer [^Linearizer l] 
;  {:class :leaf :score (.score l)})
;(defmethod z/clojurize Leaf [this] (map<-Leaf this))
;(defmethod print-method Leaf [^Leaf this ^java.io.Writer w]
;  (if *print-readably*
;    (do
;      (.write w " #taiga.tree.leaf.double.Leaf{:score ")
;      (.write w (Double/toString (.score this)))
;      (.write w "} "))
;    (.write w (print-str (map<-Leaf this)))))
;;----------------------------------------------------------------
;(defn map->Linearizer [m] 
;  (Linearizer/make (:score m)))
;(defn map<-Linearizer [^Linearizer l] 
;  {:class :leaf :score (.score l)})
;(defmethod z/clojurize Leaf [this] (map<-Leaf this))
;(defmethod print-method Leaf [^Leaf this ^java.io.Writer w]
;  (if *print-readably*
;    (do
;      (.write w " #taiga.tree.leaf.double.Leaf{:score ")
;      (.write w (Double/toString (.score this)))
;      (.write w "} "))
;    (.write w (print-str (map<-Leaf this)))))
;;----------------------------------------------------------------
;; EDN input (output just works?)
;;----------------------------------------------------------------
;(z/add-edn-readers! 
;  {'zana.java.data.Linearizer map->Linearizer})
