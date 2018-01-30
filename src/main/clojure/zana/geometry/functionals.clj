(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2018-01-29"
      :doc "Real (double) valued functions on affine/linear 
            spaces." }
    
    zana.geometry.functionals
  
  (:import [clojure.lang IFn IFn$OD]
           [zana.java.arrays Arrays]))
;;----------------------------------------------------------------
(deftype LinearFunctional [^doubles dual]
  IFn$OD (invokePrim ^double [_ v] (Arrays/dot dual v))
  IFn (invoke [this v] (.invokePrim this v)))
;;----------------------------------------------------------------
(deftype AffineFunctional [^LinearFunctional linear
                           ^double translation]
  IFn$OD (invokePrim ^double [_ v] 
           (+ translation (.invokePrim linear v)))
  IFn (invoke [this v] (.invokePrim this v)))
;;----------------------------------------------------------------
(defn linear-functional ^IFn$OD [^doubles dual]
  (LinearFunctional. dual))

(defn affine-functional 
  (^IFn$OD [^doubles dual ^double translation]
    ;; p length array plus separate translation coordinate
    (AffineFunctional. (LinearFunctional. dual) translation))
  (^IFn$OD [^doubles homogeneous]
    ;; p+1 homogeneous coordinates
    (let [n+1 (int (alength homogeneous))
          n (dec n+1)]
      (affine-functional 
        (java.util.Arrays/copyOf homogeneous n)
        (aget homogeneous n)))))
;;----------------------------------------------------------------