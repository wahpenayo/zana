(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2018-02-14"
      :doc 
      "Interface between linear/affine functionals and 
       commons math3 LinearConstraint." }
    
    zana.optimization.math3.constraint
  
  (:require [zana.geometry.functionals :as zgf])
  
  (:import [org.apache.commons.math3.optim.linear
            LinearConstraint Relationship]
           [zana.geometry.functionals 
            AffineFunctional LinearFunctional]))
;;----------------------------------------------------------------
;; TODO: accept symbols/keywords regardless of namespace?

(defn relationship ^Relationship [r]
  (cond 
    (or (= clojure.core/== r)
        (= clojure.core/= r)
        (= #'clojure.core/== r)
        (= #'clojure.core/= r)
        (= "==" r)
        (= "=" r))
    Relationship/EQ 
    
    (or (= clojure.core/<= r)
        (= #'clojure.core/<= r)
        (= "<=" r))
    Relationship/LEQ 
    
    (or (= clojure.core/>= r)
        (= #'clojure.core/>= r)
        (= ">=" r))
    Relationship/GEQ 
    
    :else
    (throw 
      (IllegalArgumentException.
        (print-str 
          "can't construct a Relationship from:" r)))))
;;----------------------------------------------------------------
;; TODO: generic function/multimethod?

(defn linear-constraint ^LinearConstraint [f r]
  (cond 
    (instance? AffineFunctional f)
    (let [lhs (zgf/dual (zgf/linear f))
          rhs (- (zgf/translation f))]
      (LinearConstraint. lhs (relationship r) rhs))
    
    (instance? LinearFunctional f)
    (LinearConstraint. (zgf/dual f) (relationship r) 0.0)
    
    :else
    (throw 
      (IllegalArgumentException.
        (print-str 
          "can't construct a LinearConstraint from:" 
          f "and" r)))))
;;----------------------------------------------------------------
