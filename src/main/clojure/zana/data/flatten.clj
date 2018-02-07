(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com"
      :date "2018-02-06"
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
            CategoricalEmbedding NumericalEmbedding 
            AffineEmbedding LinearEmbedding]))
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

(defn linear-embedding
  
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
    (LinearEmbedding/make name attributeValues))
  (^IFn [^String name ^List attributes ^List training-data]
    (let [av (mapv #(attribute-values % training-data) attributes)] 
      #_(pp/pprint av)
      (linear-embedding name av ))))
;;----------------------------------------------------------------

(defn affine-embedding
  
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
    (AffineEmbedding/make name attributeValues))
  (^IFn [^String name ^List attributes ^List training-data]
    (affine-embedding 
      name
      (mapv #(attribute-values % training-data) attributes))))
;;----------------------------------------------------------------
;; EDN io
;;----------------------------------------------------------------
;; NOTE: this requires all the categories to have print-methods

(defn map->CategoricalEmbedding [m] 
  (CategoricalEmbedding/make 
    ^String (:name m)
    ^Map (:categoryIndex m)))
(defn map<-CategoricalEmbedding 
  [^CategoricalEmbedding c] 
  {:name (.name c) 
   :categoryIndex (into {} (.categoryIndex c))})
(defmethod clojurize/clojurize CategoricalEmbedding [this]
  (map<-CategoricalEmbedding this))
(defmethod print-method CategoricalEmbedding
  [^CategoricalEmbedding this ^Writer w]
  (if *print-readably*
    (do
      (.write w 
        " #zana.java.data.CategoricalEmbedding {:name ")
      (.write w (pr-str (.name this)))
      (.write w " :categoryIndex ")
      (.write w (pr-str (into {} (.categoryIndex this))))
      (.write w "} "))
    (.write w 
      (print-str (map<-CategoricalEmbedding this)))))
;;----------------------------------------------------------------
(defn map->NumericalEmbedding [m] 
  (NumericalEmbedding/make ^String (:name m)))
(defn map<-NumericalEmbedding 
  [^NumericalEmbedding c] 
  {:name (.name c)})
(defmethod clojurize/clojurize NumericalEmbedding [this]
  (map<-NumericalEmbedding this))
(defmethod print-method NumericalEmbedding
  [^NumericalEmbedding this ^Writer w]
  (if *print-readably*
    (do
      (.write w 
        " #zana.java.data.NumericalEmbedding {:name ")
      (.write w (pr-str (.name this)))
      (.write w "} "))
    (.write w 
      (print-str (map<-NumericalEmbedding this)))))
;;----------------------------------------------------------------
(defn map->LinearEmbedding [m] 
  (LinearEmbedding. 
    ^String (:name m)
    ^List (:attributeEmbeddings m)))
(defn map<-LinearEmbedding [^LinearEmbedding l] 
  {:name (.name l)
   :attributeEmbeddings (into [] (.attributeEmbeddings l))})
(defmethod clojurize/clojurize LinearEmbedding [this]
  (map<-LinearEmbedding this))
(defmethod print-method LinearEmbedding [^LinearEmbedding this ^Writer w]
  (if *print-readably*
    (do
      (.write w " #zana.java.data.LinearEmbedding {:name ")
      (.write w (pr-str (.name this)))
      (.write w " :attributeEmbeddings ")
      (.write w (pr-str (into [] (.attributeEmbeddings this))))
      (.write w "} "))
    (.write w (print-str (map<-LinearEmbedding this)))))
;;----------------------------------------------------------------
(defn map->AffineEmbedding [m] 
  (AffineEmbedding.
    ^String (:name m)
    ^List (:attributeEmbeddings m)))
(defn map<-AffineEmbedding [^AffineEmbedding l] 
  {:name (.name l)
   :attributeEmbeddings (into [] (.attributeEmbeddings l))})
(defmethod clojurize/clojurize AffineEmbedding [this] 
  (map<-AffineEmbedding this))
(defmethod print-method AffineEmbedding [^AffineEmbedding this ^Writer w]
  (if *print-readably*
    (do
      (.write w " #zana.java.data.AffineEmbedding {:name ")
      (.write w (pr-str (.name this)))
      (.write w " :attributeEmbeddings ")
      (.write w (pr-str (into [] (.attributeEmbeddings this))))
      (.write w "} "))
    (.write w (print-str (map<-AffineEmbedding this)))))
;;----------------------------------------------------------------
;; EDN input (output just works?)
;;----------------------------------------------------------------
(zedn/add-edn-readers! 
  {'zana.java.data.CategoricalEmbedding
   map->CategoricalEmbedding
   'zana.java.data.NumericalEmbedding
   map->NumericalEmbedding
   'zana.java.data.LinearEmbedding
   map->LinearEmbedding
   'zana.java.data.AffineEmbedding
   map->AffineEmbedding})
