(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2018-04-04"
      :doc 
      "Generic function composition." }
    
    zana.geometry.compose
  
  #_(:require )
  (:import [zana.java.geometry.functions 
            AffineDual AffineFunctional Composition2 Function 
            LinearFunctional LinearRows Sample]))
;;----------------------------------------------------------------
;; TODO: https://github.com/palisades-lakes/faster-multimethods

(defmulti compose 
  "Return an instance of <code>zana.java.geometry.Function</code> 
   which is the composition of the arguments.<br> 
   <code>((compose f0 f1) x)</code> is almost equivalent to 
   <code>(f0 (f1 x))</code>. The order of operations may be 
   different, and therefore the exact value may differ 
   slightly. There is no 
   guarantee that external side effects will be the same."
  {:arglists '([f0 f1] [f0 f1 & fs])}
  (fn compose-dispatch ([& fs] (mapv class fs))))
;;----------------------------------------------------------------
(defmethod compose 
  [Function Function] 
  [^Function f0 ^Function f1]
  (Composition2/compose f0 f1))

(defmethod compose 
  [LinearFunctional LinearRows] 
  [^LinearFunctional f0 ^LinearRows f1]
  (.compose f0 f1))

(defmethod compose 
  [Sample AffineDual] 
  [^Sample f0 ^AffineDual f1]
  (.compose f0 f1))
;;----------------------------------------------------------------
  