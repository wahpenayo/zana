(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2018-02-14"
      :doc 
      "Interface between affine/linear functionals and 
      commons math3 LinearObjectiveFunction." }
    
    zana.optimization.math3.cost
  
  (:require [zana.geometry.functionals :as zgf])
  
  (:import [org.apache.commons.math3.optim.linear
            LinearObjectiveFunction]
           [zana.geometry.functionals 
             AffineFunctional LinearFunctional]))
;;----------------------------------------------------------------
;; TODO: generic function/multimethod?

(defn linear-objective-function
  ^LinearObjectiveFunction [f]
  (cond 
    (instance? AffineFunctional f)
    (LinearObjectiveFunction. (zgf/dual (zgf/linear f))
                              (zgf/translation f))
    
    (instance? LinearFunctional f)
    (LinearObjectiveFunction. (zgf/dual f) 
                              0.0)
    
    :else
    (throw 
      (IllegalArgumentException.
        (print-str 
          "can't construct a LinearObjectFunction from:" f)))))
;;----------------------------------------------------------------
