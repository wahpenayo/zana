(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2018-02-07"
      :doc 
      "Real (double) valued functions on affine/linear spaces." }
    
    zana.geometry.functionals
  
  (:require [zana.commons.core :as commons])
  
  (:import [clojure.lang IFn IFn$D IFn$OD]
           [zana.java.arrays Arrays]))
;;----------------------------------------------------------------
(deftype LinearFunctional [^doubles dual]
  IFn$OD 
  (invokePrim ^double [_ v] (Arrays/dot dual v))
  IFn 
  (invoke [this v] (.invokePrim this v)))
;;----------------------------------------------------------------
(defn linear-functional ^IFn$OD [^doubles dual]
  (LinearFunctional. 
    (java.util.Arrays/copyOf
      dual (int (alength dual)))))
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
  IFn$OD 
  (invokePrim ^double [_ v] 
    (+ translation (.invokePrim linear v)))
  IFn 
  (invoke [this v] (.invokePrim this v)))
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
(defn generate-affine-functional 
  "Generate a linear functional whose coordinates come from
   successive calls to generator functions, which are typically a 
   pseudo-random number generators. Two arities allow different
  generators for linear and translation components."
  (^IFn$OD [^long dim ^IFn$D gl ^IFn$D gt]
    (affine-functional 
      (generate-linear-functional gl)
      (gt)))
  (^IFn$OD [^long dim ^IFn$D g]
    (generate-affine-functional dim g g)))
;;----------------------------------------------------------------