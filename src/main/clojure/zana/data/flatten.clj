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
            [zana.collections.sets :as sets]
            [zana.collections.clojurize :as clojurize]
            [zana.io.edn :as zedn])
  
  (:import [java.util Arrays Collection List Map]
           [java.time.temporal TemporalAccessor]
           [java.io Writer]
           [com.google.common.collect ImmutableMap]
           [clojure.lang IFn IFn$OD IFn$OL]
           [zana.java.data 
            CategoricalAttributeLinearizer 
            NumericalAttributeLinearizer 
            Homogenizer Linearizer]))
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
;; NOTE: this requires all the categories to have print-methods

(defn map->CategoricalAttributeLinearizer [m] 
  (CategoricalAttributeLinearizer/make 
    ^String (:name m)
    ^Map (:categoryIndex m)))
(defn map<-CategoricalAttributeLinearizer 
  [^CategoricalAttributeLinearizer c] 
  {:name (.name c) 
   :categoryIndex (into {} (.categoryIndex c))})
(defmethod clojurize/clojurize CategoricalAttributeLinearizer [this]
  (map<-CategoricalAttributeLinearizer this))
(defmethod print-method CategoricalAttributeLinearizer
  [^CategoricalAttributeLinearizer this ^Writer w]
  (if *print-readably*
    (do
      (.write w 
        " #zana.java.data.CategoricalAttributeLinearizer {:name ")
      (.write w (.name this))
      (.write w " :categoryIndex ")
      (.write w (pr-str (into {} (.categoryIndex this))))
      (.write w "} "))
    (.write w 
      (print-str (map<-CategoricalAttributeLinearizer this)))))
;;----------------------------------------------------------------
(defn map->NumericalAttributeLinearizer [m] 
  (NumericalAttributeLinearizer/make ^String (:name m)))
(defn map<-NumericalAttributeLinearizer 
  [^NumericalAttributeLinearizer c] 
  {:name (.name c)})
(defmethod clojurize/clojurize NumericalAttributeLinearizer [this]
  (map<-NumericalAttributeLinearizer this))
(defmethod print-method NumericalAttributeLinearizer
  [^NumericalAttributeLinearizer this ^Writer w]
  (if *print-readably*
    (do
      (.write w 
        " #zana.java.data.NumericalAttributeLinearizer {:name ")
      (.write w (.name this))
      (.write w "} "))
    (.write w 
      (print-str (map<-NumericalAttributeLinearizer this)))))
;;----------------------------------------------------------------
(defn map->Linearizer [m] 
  (Linearizer/make 
    ^String (:name m)
    ^List (:attributeLinearizers m)))
(defn map<-Linearizer [^Linearizer l] 
  {:name (.name l)
   :attributeLinearizers (into {} (.attributeLinearizers l))})
(defmethod clojurize/clojurize Linearizer [this]
  (map<-Linearizer this))
(defmethod print-method Linearizer [^Linearizer this ^Writer w]
  (if *print-readably*
    (do
      (.write w " #zana.java.data.Linearizer {:name ")
      (.write w (.name this))
      (.write w " :attributeLinearizers ")
      (.write w (pr-str (into {} (.attributeLinearizers this))))
      (.write w "} "))
    (.write w (print-str (map<-Linearizer this)))))
;;----------------------------------------------------------------
(defn map->Homogenizer [m] 
  (Homogenizer/make 
    ^String (:name m)
    ^List (:attributeLinearizers m)))
(defn map<-Homogenizer [^Homogenizer l] 
  {:name (.name l)
   :attributeLinearizers (into {} (.attributeLinearizers l))})
(defmethod clojurize/clojurize Homogenizer [this] 
  (map<-Homogenizer this))
(defmethod print-method Homogenizer [^Homogenizer this ^Writer w]
  (if *print-readably*
    (do
      (.write w " #zana.java.data.Homogenizer {:name ")
      (.write w (.name this))
      (.write w " :attributeLinearizers ")
      (.write w (pr-str (into {} (.attributeLinearizers this))))
      (.write w "} "))
    (.write w (print-str (map<-Homogenizer this)))))
;;----------------------------------------------------------------
;; EDN input (output just works?)
;;----------------------------------------------------------------
(zedn/add-edn-readers! 
  {'zana.java.data.CategoricalAttributeLinearizer
   map->CategoricalAttributeLinearizer
   'zana.java.data.NumericalAttributeLinearizer
   map->NumericalAttributeLinearizer
   'zana.java.data.Linearizer
   map->Linearizer
   'zana.java.data.Homogenizer
   map->Homogenizer})
