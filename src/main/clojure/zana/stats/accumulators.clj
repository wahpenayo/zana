(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2017-01-04"
      :doc "factory functions for Accumulator classes from 
            zana.java.accumulator." }
    
    zana.stats.accumulators
  
  (:require [zana.commons.core :as cc]))
;;------------------------------------------------------------------------------
;; assuming cost scaled so false-negative-cost + false-positive-cost = 1.0

(defn minimum-expected-cost-class
  "An accumulator that returns the weighted minimum cost (0.0 or 1.0) class."
  ^zana.java.accumulator.Accumulator [^double false-positive-cost] 
  (assert (< 0.0 false-positive-cost 1.0))
  (zana.java.accumulator.BinaryMinimumExpectedCostClass. false-positive-cost))

;; majority vote is equivalent to the min expected cost class when
;; false negative and false positive costs are equal

(defn majority-vote 
  "An accumulator that returns the majority vote (0.0 or 1.0) class."
  ^zana.java.accumulator.Accumulator [] 
  (zana.java.accumulator.BinaryMinimumExpectedCostClass. 0.5))

(defn gini 
  "An accumulator for calculating the 
   <a href=\"https://en.wikipedia.org/wiki/Decision_tree_learning#Gini_impurity\">
   Gini Impurity</a>."
  ^zana.java.accumulator.Accumulator [] 
  (zana.java.accumulator.GiniImpurity.))

(defn positive-fraction 
  "An accumulator that returns the fraction of positive (1.0) cases in binary 
   classification."
  ^zana.java.accumulator.Accumulator [] 
  (zana.java.accumulator.PositiveFraction.))

(defn mean 
  "An accumulator that returns the mean of its values."
  ^zana.java.accumulator.Accumulator [] 
  (zana.java.accumulator.Mean.))

(defn mssn 
  "An accumulator for the mean squared sum of its values. This is equivalent
   to mean squared error for choosing L2 regression split points, but is
   faster to compute."
  ^zana.java.accumulator.Accumulator [] 
  (zana.java.accumulator.MSSN.))

(defn vector-mean 
  "An accumulator that returns the vector mean of its values."
  ^zana.java.accumulator.Accumulator [dim] 
  (zana.java.accumulator.VectorMean. dim))

(defn vector-mssn 
  "An accumulator for the mean squared sum of its values, summed over the 
   coordinates of the input vectors. This is equivalent
   to mean squared error for choosing L2 regression split points, but is
   faster to compute."
  ^zana.java.accumulator.Accumulator [dim] 
  (zana.java.accumulator.VectorMSSN. dim))

;;------------------------------------------------------------------------------

(defn weighted-minimum-expected-cost-class
  "An accumulator that returns the weighted minimum cost (0.0 or 1.0) class."
  ^zana.java.accumulator.Accumulator [^double false-positive-cost] 
  (assert (< 0.0 false-positive-cost 1.0))
  (zana.java.accumulator.weighted.BinaryMinimumExpectedCostClass. false-positive-cost))

;; majority vote is equivalent to the min expected cost class when
;; false negative and false positive costs are equal

(defn weighted-majority-vote 
  "An accumulator that returns the majority vote (0.0 or 1.0) class."
  ^zana.java.accumulator.Accumulator [] 
  (zana.java.accumulator.weighted.BinaryMinimumExpectedCostClass. 0.5))

(defn weighted-gini 
  "An accumulator for calculating the 
   <a href=\"https://en.wikipedia.org/wiki/Decision_tree_learning#Gini_impurity\">
   Gini Impurity</a>."
  ^zana.java.accumulator.Accumulator [] 
  (zana.java.accumulator.weighted.GiniImpurity.))

(defn weighted-positive-fraction 
  "An accumulator that returns the fraction of positive (1.0) cases in binary 
   classification."
  ^zana.java.accumulator.Accumulator [] 
  (zana.java.accumulator.weighted.PositiveFraction.))

(defn weighted-mean 
  "An accumulator that returns the mean of its values."
  ^zana.java.accumulator.Accumulator [] 
  (zana.java.accumulator.weighted.Mean.))

(defn weighted-mssn 
  "An accumulator for the mean squared sum of its values. This is equivalent
   to mean squared error for choosing L2 regression split points, but is
   faster to compute."
  ^zana.java.accumulator.Accumulator [] 
  (zana.java.accumulator.weighted.MSSN.))

;;------------------------------------------------------------------------------

(defn make-calculator 
  "Return a function wrapping an accumulator factory. 
   Used for one-pass calculation of accumulator statistics, it computes a
   (weighted) statistic for a data set by making a new accumulator and
   adding all the data values to it."
  (^clojure.lang.IFn$OD [accumulator-factory]
    (fn calculator 
      (^double [d]
        (let [^zana.java.accumulator.Accumulator a (accumulator-factory)]
          (cond (cc/double-array? d) 
                (let [^doubles d (doubles d)
                      n (alength d)]
                  (dotimes [i n] (.add a (aget d i))))
                ;;(instance? Iterable d) (.add a ^Iterable d)
                :else (throw 
                        (IllegalArgumentException.
                          (print-str "Can't compute the mean of " d))))
          (.doubleValue a)))
      (^double [d w] 
        (let [^zana.java.accumulator.Accumulator a (accumulator-factory)]
          (cond (cc/double-array? d) 
                (let [^doubles d (doubles d)
                      ^doubles w (doubles w)
                      n (alength d)]
                  (assert (== n (alength w)))
                  (dotimes [i n] (.add a (aget d i) (aget w i))))
                ;;(instance? Iterable d) (.add a ^Iterable d ^Iterable w)
                :else (throw 
                        (IllegalArgumentException.
                          (print-str "Can't compute the mean of " d))))
          (.doubleValue a)))))
  
  (^clojure.lang.IFn$OD [accumulator-factory 
                         ^clojure.lang.IFn$OD z 
                         ^clojure.lang.IFn$OD w]
    (fn calculator ^double [^java.util.List data]
      (assert (instance? java.util.RandomAccess data))
      (let [^zana.java.accumulator.Accumulator a (accumulator-factory)
            n (.size data)]
        (dotimes [i n] 
          (let [di (.get data i)]
            (.add a (.invokePrim z di) (.invokePrim w di))))
        (.doubleValue a))))
  
  (^clojure.lang.IFn$OD [accumulator-factory 
                         ^clojure.lang.IFn$OD z]
    (fn calculator ^double [^java.util.List data]
      (assert (instance? java.util.RandomAccess data))
      (let [^zana.java.accumulator.Accumulator a (accumulator-factory)
            n (.size data)]
        (dotimes [i n] 
          (let [di (.get data i)]
            (.add a (.invokePrim z di))))
        (.doubleValue a)))))

;;------------------------------------------------------------------------------

(defn make-object-calculator 
  "Return a function wrapping an accumulator factory. 
   Used for one-pass calculation of accumulator statistics, it computes a
   (weighted) statistic for a data set by making a new accumulator and
   adding all the data values to it."
  #_(^clojure.lang.IFn [accumulator-factory]
      (fn calculator 
        ([d]
            (let [^zana.java.accumulator.Accumulator a (accumulator-factory)]
              (cond (cc/double-array? d) (.add a (doubles d))
                    ;;(instance? Iterable d) (.add a ^Iterable d)
                    :else (throw 
                            (IllegalArgumentException.
                              (print-str "Can't compute the mean of " d))))
              (.value a)))
        ([d w] 
            (let [^zana.java.accumulator.Accumulator a (accumulator-factory)]
              (cond (cc/double-array? d) (.add a (doubles d) (doubles w))
                    ;;(instance? Iterable d) (.add a ^Iterable d ^Iterable w)
                    :else (throw 
                            (IllegalArgumentException.
                              (print-str "Can't compute the mean of " d))))
              (.value a)))))
  
  (^clojure.lang.IFn [accumulator-factory 
                      ^clojure.lang.IFn z 
                      ^clojure.lang.IFn$OD w]
    (fn calculator [^java.util.List data]
      (assert (instance? java.util.RandomAccess data))
      (let [^zana.java.accumulator.Accumulator a (accumulator-factory)
            n (.size data)]
        (dotimes [i n] 
          (let [di (.get data i)]
            (.add a (.invoke z di) (.invokePrim w di))))
        (.value a))))
  
  (^clojure.lang.IFn [accumulator-factory 
                      ^clojure.lang.IFn z]
    (fn calculator [^java.util.List data]
      (assert (instance? java.util.RandomAccess data))
      (let [^zana.java.accumulator.Accumulator a (accumulator-factory)
            n (.size data)]
        (dotimes [i n] 
          (let [di (.get data i)]
            (.add a (.invoke z di))))
        (.value a)))))

;------------------------------------------------------------------------------