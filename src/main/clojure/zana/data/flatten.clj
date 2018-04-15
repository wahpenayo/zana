(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com"
      :date "2018-02-11"
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
            [zana.commons.core :as commons]
            [zana.collections.maps :as maps]
            [zana.collections.sets :as sets]
            [zana.collections.clojurize :as clojurize]
            [zana.io.edn :as zedn])
  
  (:import [java.util Arrays Collection List Map]
           [java.time.temporal TemporalAccessor]
           [java.io Writer]
           [com.google.common.collect ImmutableList ImmutableMap]
           [clojure.lang IFn IFn$OD IFn$OL]
           [zana.java.data 
            CategoricalEmbedding NumericalEmbedding 
            AffineEmbedding FlatEmbedding LinearEmbedding]))
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
    [(commons/name-keyword attribute) Double/TYPE]
    
    (categorical? attribute)
    (let [values (rest 
                   (mapv
                     key
                     (sort-by 
                       #(- (int (val %)))
                       (maps/frequencies 
                         attribute 
                         training-data))))] 
      [(commons/name-keyword  attribute) values])
    
    :else
    (throw (IllegalArgumentException.
             (print-str "can't handle" attribute)))))
;;----------------------------------------------------------------
;; TODO: this belongs sxomewhere else.
(defn attribute-bindings 
  "Return a map from `Keyword` to attribute `IFn`, where the 
   keyword is constructed from the name of the attribute."
  [attributes]
  (into 
    {}
    (map (fn [a] [(commons/name-keyword a) a]) attributes))) 
;;----------------------------------------------------------------

#_(defn affine-embedding
  
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
  
  (^IFn [^AffineEmbedding af]
    ;; coerce affine to linear embedding
    (LinearEmbedding. (.name af) (.attributeEmbeddings af)))
  (^IFn [^String name ^List attributeValues]
    (LinearEmbedding/make name attributeValues))
  (^IFn [^String name ^List attributes ^List training-data]
    (let [av (mapv #(attribute-values % training-data) attributes)] 
      #_(pp/pprint av)
      (linear-embedding name av ))))
;;----------------------------------------------------------------
(defn embedding-dimension ^long [^FlatEmbedding e]
  (.dimension e))
#_(defn linear-part ^LinearEmbedding [^AffineEmbedding a]
   (.linearPart a))
;;----------------------------------------------------------------
;; EDN io
;;----------------------------------------------------------------
;; NOTE: this requires all the categories to have print-methods

(defn map->CategoricalEmbedding ^CategoricalEmbedding [^Map m] 
  (CategoricalEmbedding. 
    ^String (:name m)
    (ImmutableMap/copyOf ^Map (:categoryIndex m))))
(defn map<-CategoricalEmbedding ^Map [^CategoricalEmbedding c] 
  {:name (.name c) 
   :categoryIndex (into {} (.categoryIndex c))})
(defmethod clojurize/clojurize CategoricalEmbedding [this]
  (map<-CategoricalEmbedding this))
(defmethod print-method 
  CategoricalEmbedding [^CategoricalEmbedding this ^Writer w]
  (.write w 
    (if *print-readably*
      (str " #zana.java.data.CategoricalEmbedding "
           (pr-str (map<-CategoricalEmbedding this)))
      (print-str (map<-CategoricalEmbedding this)))))
;;----------------------------------------------------------------
(defn map->NumericalEmbedding [m] 
  (NumericalEmbedding. ^String (:name m)))
(defn map<-NumericalEmbedding 
  [^NumericalEmbedding c] 
  {:name (.name c)})
(defmethod clojurize/clojurize NumericalEmbedding [this]
  (map<-NumericalEmbedding this))
(defmethod print-method 
  NumericalEmbedding [^NumericalEmbedding this ^Writer w]
  (.write w 
    (if *print-readably*
      (str " #zana.java.data.NumericalEmbedding "
           (pr-str (map<-NumericalEmbedding this)))
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
(defmethod print-method 
  LinearEmbedding [^LinearEmbedding this ^Writer w]
  (.write w 
    (if *print-readably*
      (str " #zana.java.data.LinearEmbedding "
           (pr-str (map<-LinearEmbedding this)))
      (print-str (map<-LinearEmbedding this)))))
;;----------------------------------------------------------------
(defn map->AffineEmbedding [m] 
  (AffineEmbedding.
    ^String (:name m)
    (ImmutableList/copyOf ^List (:attributeEmbeddings m))))
(defn map<-AffineEmbedding [^AffineEmbedding l] 
  {:name (.name l)
   :attributeEmbeddings (into [] (.attributeEmbeddings l))})
(defmethod clojurize/clojurize AffineEmbedding [this] 
  (map<-AffineEmbedding this))
(defmethod print-method 
  AffineEmbedding [^AffineEmbedding this ^Writer w]
  (.write w 
    (if *print-readably*
      (str " #zana.java.data.AffineEmbedding " 
           (pr-str (map<-AffineEmbedding this)))
      (print-str (map<-AffineEmbedding this)))))
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
;;----------------------------------------------------------------
