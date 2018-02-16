(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2018-02-12"
      :doc 
      "Real (double) valued functions on affine/linear spaces." }
    
    zana.geometry.functionals
  
  (:require [zana.commons.core :as commons]
            [zana.collections.clojurize :as clojurize]
            [zana.io.edn :as zedn])
  
  (:import [java.util Map]
           [java.io Serializable Writer]
           [clojure.lang IFn IFn$D IFn$OD]
           [zana.java.arrays Arrays]))
;;----------------------------------------------------------------
(deftype LinearFunctional [^doubles dual]
  Serializable
  IFn$OD 
  (invokePrim ^double [_ v] (Arrays/dot dual v))
  IFn 
  (invoke [this v] (.invokePrim this v))
  Object 
  (equals [_ that] 
    (and (instance? LinearFunctional that)
         (java.util.Arrays/equals 
           dual 
           (doubles (.dual ^LinearFunctional that)))))
  (hashCode [_] (java.util.Arrays/hashCode dual))
  (toString [_] (str "LinearFunctional" (into [] dual))))
;;----------------------------------------------------------------
(defn dual ^doubles [^LinearFunctional lf]
  (.dual lf))
;;----------------------------------------------------------------
(defn linear-functional ^IFn$OD [^doubles dual]
  (LinearFunctional. 
    (java.util.Arrays/copyOf
      dual (int (alength dual)))))
;;----------------------------------------------------------------
(defn linear-functional? [f] (instance? LinearFunctional f))
;;----------------------------------------------------------------
(defn generate-linear-functional ^IFn$OD [^long dim ^IFn$D g]
  "Generate a linear functional whose coordinates come from
   successive calls to `g`, which is typically a pseudo-random
   numbver generator."
  (let [d (double-array dim)]
    (dotimes [i (int dim)] (aset-double d i (.invokePrim g)))
    (linear-functional d)))
;;----------------------------------------------------------------
(deftype AffineFunctional [^LinearFunctional linear
                           ^double translation]
  Serializable
  IFn$OD 
  (invokePrim ^double [_ v] 
    (+ translation (.invokePrim linear v)))
  IFn 
  (invoke [this v] (.invokePrim this v))
  Object 
  (equals [_ that] 
    (and (instance? AffineFunctional that)
         (let [^AffineFunctional that that]
           (and (== translation (.translation that))
                (.equals linear (.linear that))))))
  (hashCode [_]    
    (let [h (int 17)
          h (unchecked-multiply-int h (int 31))
          h (unchecked-add-int h (.hashCode linear))
          h (unchecked-multiply-int h (int 31))
          h (unchecked-add-int h (Double/hashCode translation))]
      h))
  (toString [_]
    (str "AffineFunctional[" linear ", " translation "]")))
;;----------------------------------------------------------------
(defn linear ^LinearFunctional [^AffineFunctional af]
  (.linear af))
(defn translation ^double [^AffineFunctional af]
  (.translation af))
;;----------------------------------------------------------------
(defn affine-functional 
  
  (^IFn$OD [linear ^double translation]
    (let [^LinearFunctional linear 
          (cond (instance? LinearFunctional linear)
                linear
                (commons/double-array? linear)
                (linear-functional linear)
                :else
                (throw (IllegalArgumentException.
                         (print-str 
                           "can't construct an affine functional:"
                           linear translation))))]
      (AffineFunctional. linear translation)))
  
  (^IFn$OD [^doubles homogeneous]
    ;; p+1 homogeneous coordinates
    (let [n+1 (int (alength homogeneous))
          n (dec n+1)]
      (affine-functional 
        (java.util.Arrays/copyOf homogeneous n)
        (aget homogeneous n)))))
;;----------------------------------------------------------------
(defn affine-functional? [f] (instance? AffineFunctional f))
;;----------------------------------------------------------------
(defn generate-affine-functional 
  "Generate a linear functional whose coordinates come from
   successive calls to generator functions, which are typically a 
   pseudo-random number generators. Two arities allow different
  generators for linear and translation components."
  (^IFn$OD [^long dim ^IFn$D gl ^IFn$D gt]
    (affine-functional 
      (generate-linear-functional dim gl)
      (gt)))
  (^IFn$OD [^long dim ^IFn$D g]
    (generate-affine-functional dim g g)))
;;----------------------------------------------------------------
;; generic flat functionals
;;----------------------------------------------------------------
(defn flat-functional? [f] 
  (or (linear-functional? f) (affine-functional? f)))
;;----------------------------------------------------------------
(defn domain-dimension ^long [f]
  (cond (linear-functional? f)
        (alength (dual f))
        (affine-functional? f)
        (domain-dimension (linear f))
        :else
        (throw 
          (IllegalArgumentException.
            (print-str 
              "can't determine the domain-dimension of" f)))))
;;----------------------------------------------------------------
;; EDN io
;;----------------------------------------------------------------
(defn map->LinearFunctional ^LinearFunctional [^Map m] 
  (LinearFunctional. (double-array (:dual m))))
(defn map<-LinearFunctional ^Map [^LinearFunctional lf] 
  {:dual (into [] (.dual lf))})
(defmethod clojurize/clojurize LinearFunctional [this]
  (map<-LinearFunctional this))
(defmethod print-method LinearFunctional
  [^LinearFunctional this ^Writer w]
  (if *print-readably*
    (do
      (.write w " #zana.geometry.functionals.LinearFunctional " )
      (.write w (pr-str (map<-LinearFunctional this))))
    (.write w 
      (print-str (map<-LinearFunctional this)))))
;;----------------------------------------------------------------
(defn map->AffineFunctional ^AffineFunctional [^Map m] 
  (AffineFunctional. (:linear m) (:translation m)))
(defn map<-AffineFunctional 
  ^Map [^AffineFunctional af] 
  {:linear (.linear af) :translation (.translation af)})
(defmethod clojurize/clojurize AffineFunctional [this]
  (map<-AffineFunctional this))
(defmethod print-method AffineFunctional
  [^AffineFunctional this ^Writer w]
  (if *print-readably*
    (do
      (.write w " #zana.geometry.functionals.AffineFunctional " )
      (.write w (pr-str (map<-AffineFunctional this))))
    (.write w (print-str (map<-AffineFunctional this)))))
;;----------------------------------------------------------------
;; EDN input (output just works?)
;;----------------------------------------------------------------
(zedn/add-edn-readers! 
  {'zana.geometry.functionals.LinearFunctional
   map->LinearFunctional 
   'zana.geometry.functionals.AffineFunctional 
   map->AffineFunctional})
;;----------------------------------------------------------------
