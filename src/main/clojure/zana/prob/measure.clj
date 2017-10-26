(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "palisades dot lakes at gmail dot com" 
      :since "2017-10-24"
      :date "2017-10-25"
      :doc "Probability measures over <b>R</b>." }
    
    zana.prob.measure
  
  (:import [java.util Arrays]
           [org.apache.commons.math3.stat StatUtils]
           [org.apache.commons.math3.util MathArrays
            MathArrays$OrderDirection]
           [zana.java.arrays Sorter]))
;;----------------------------------------------------------------
;; TODO: move elsewhere
(defn- quicksort 
  "Non-destructive sort. <code>z</code> is made non-decreasing
   and <code>w</code> is permuted in the same way."
  [^doubles z ^doubles w]
  (let [z (aclone z)
        w (aclone w)]
    (Sorter/quicksort z w)
    [z w]))
;;----------------------------------------------------------------
(defn- compact [^doubles z ^doubles w]
  (MathArrays/checkOrder 
    z MathArrays$OrderDirection/INCREASING false)
  (MathArrays/checkPositive w)
  (let [n (int (alength w))]
    (assert (== n (alength z)))
    (loop [z0 (aget z 0)
           w0 (aget w 0)
           i0 (int 0)
           i1 (int 1)]
      (if (>= i1 n) 
        ;; done, copy into shorter arrays if needed
        (let [nn (inc i0)]
          (if (== nn n)
            [^doubles z ^doubles w]
            [(Arrays/copyOf z nn) (Arrays/copyOf w nn)]))
        ;; check for continuing ties in z
        (let [z1 (aget z i1)
              w1 (aget w i1)]
          (if (== z0 z1) 
            ;; tie, increment weight, move right counter
            (let [w0 (+ w0 w1)]
              (aset-double w i0 w0)
              (recur z0 w0 i0 (inc i1)))
            ;; no tie, increment both counters
            ;; copy to left counter if needed
            (let [i0 (inc i0)]
              (when-not (== i0 i1) 
                (aset-double z i0 z1)
                (aset-double w i0 w1))
              (recur z1 w1 i0 (inc i1)))))))))
;;----------------------------------------------------------------
;; TODO: replace naive sum with something more accurate
(defn- sums
  "Return a new array whose ith element is the sum from 0 to i
   (inclusive) of the elements of <code>w</code>."
  [^doubles w]
  (let [n (int (alength w))
        ^doubles w (aclone w)]
    (loop [i (int 1)
           w0 (aget w 0)]
      (if (>= i n) 
        w
        (let [wi (+ w0 (aget w i))]
          (aset-double w i wi)
          (recur (inc i) wi))))))
;;----------------------------------------------------------------
;; Probability measure &mu; on the real line, <b>R</b>.
(definterface RealProbabilityMeasure
  ;; TODO: probability of more general sets
  
  ;; Probability of the singleton &mu;({z})
  (^double pointmass [^double z])
  
  ;; Probability of the half line &mu;((-&infin;,z]).
  (^double cdf [^double z])
  
  ;; Pseudo inverse of <code>cdf</code>.
  ;; <code>(quantile p)</code> is maximum <code>z</code such that
  ;; <code>(<= (cdf z) p)</code>.
  ;; Must have <code>(<= 0.0 p 1.0).
  ;; May return +/- &infin;. 
  (^double quantile [^double p]))
;;----------------------------------------------------------------
;; Weighted empirical probabilty density, a collection of point
;; masses.
;; <ul>
;; <li><code>ws</code> weights.
;; <li><code>zs</code> sorted unique domain values.
;; </ul>
;; Probability of <code>z==z[i]</code> is <code>w[i]</code>.

(deftype WEPDF [^doubles z
                ^doubles w]
  
  RealProbabilityMeasure
  
  (^double pointmass [this ^double x]
    (double
      (let [i (Arrays/binarySearch z x)]
        (if (> 0 i) 
          (double 0.0) 
          (double (aget w i))))))
  
  (^double cdf [this ^double x]
    (double
      (let [i (Arrays/binarySearch z x)
            k (int (if (<= 0 i) 
                     (inc i) 
                     (- -1 i)))]
        (assert (<= 0 k))
        (if (== 0 k)
          0.0
          (StatUtils/sum w 0 k)))))
  
  (^double quantile [this ^double p]
    (throw (UnsupportedOperationException.)))
  
  Object
  
  (hashCode [this] 
    (unchecked-add-int
      (unchecked-multiply-int
        37
        (unchecked-add-int
          (unchecked-multiply-int 37 17)
          (Arrays/hashCode w)))
      (Arrays/hashCode z)))
  
  (equals [this that]
    (and (instance? WEPDF that)
         (Arrays/equals z ^doubles (.z ^WEPDF that))
         (Arrays/equals w ^doubles (.w ^WEPDF that))))
  
  ;; TODO: fix for large sample arrays
  (toString [this]
    (str "(WEPDF " (vec z) " " (vec w) ")")))
;;----------------------------------------------------------------
;; TODO: normalize w?
(defn- make-WEPDF 
  
  "Create an instance of <code>zana.prob.measure.WEPDF</code>.
  Sorts <code>z</code> and removes ties." 
  
  (^zana.prob.measure.WEPDF [^doubles z ^doubles w]
    (assert (== (alength z) (alength w)))
    (assert (not (nil? z)))
    (assert (not (nil? w)))
    (let [[^doubles z ^doubles w] (quicksort z w)
          [^doubles z ^doubles w] (compact z w)]
      (MathArrays/checkOrder 
        z MathArrays$OrderDirection/INCREASING true)
      (MathArrays/checkPositive w)
      (WEPDF. z w)))
  
  (^zana.prob.measure.WEPDF [^doubles z]
    (let [n (int (alength z))
          w (double-array n (/ 1.0 n))]
      (make-WEPDF z w))))
;;----------------------------------------------------------------
;; Weighted empirical cumulative probabilty, a non-decreasing step
;; function mapping <b>R</b> to [0,1].
;; <ul>
;; <li><code>ws</code> increasing weights.
;; <li><code>zs</code> sorted unique domain values.
;; </ul>
;; Cumulative pointmass of <code>z<=z[i]</code> is 
;; <code>w[i]</code>.
(deftype WECDF [^doubles z
                ^doubles w]
  
  RealProbabilityMeasure
  
  (^double pointmass [this ^double x]
    (double
      (let [i (Arrays/binarySearch z x)]
        (cond
          (== 0 i) (aget w i)
          (< 0 i) (- (aget w i) (aget w (dec i)))
          :else (double 0.0)))))
  
  (^double cdf [this ^double x]
    (double
      (let [i (Arrays/binarySearch z x)
            k (int (if (<= 0 i) 
                     i
                     (- -2 i)))]
        (if (== -1 k) 0.0 (aget w k)))))
  
  (^double quantile [this ^double p]
    (throw (UnsupportedOperationException.)))
  
  Object
  
  (hashCode [this] 
    (unchecked-add-int
      (unchecked-multiply-int
        37
        (unchecked-add-int
          (unchecked-multiply-int 37 17)
          (Arrays/hashCode w)))
      (Arrays/hashCode z)))
  
  (equals [this that]
    (and (instance? WECDF that)
         (Arrays/equals z ^doubles (.z ^WECDF that))
         (Arrays/equals w ^doubles (.w ^WECDF that))))
  
  ;; TODO: fix for large sample arrays
  (toString [this]
    (str "(WECDF " (vec z) " " (vec w) ")")))
;;----------------------------------------------------------------
;; TODO: normalize w?
(defn- make-WECDF 
  
  "Create an instance of <code>zana.prob.measure.WECDF</code>.
  Sorts <code>z</code> and removes ties." 
  
  (^zana.prob.measure.WECDF [^doubles z ^doubles w]
    (assert (== (alength z) (alength w)))
    (assert (not (nil? z)))
    (assert (not (nil? w)))
    (let [[^doubles z ^doubles w] (quicksort z w)
          #_(println (vec z) (vec w))
          [^doubles z ^doubles w] (compact z w)
          #_(println (vec z) (vec w))
          ^doubles w (sums w)]
      #_(println (vec z) (vec w))
      (MathArrays/checkOrder 
        z MathArrays$OrderDirection/INCREASING true)
      (MathArrays/checkOrder 
        w MathArrays$OrderDirection/INCREASING true)
      (MathArrays/checkPositive w)
      (WECDF. z w)))
  
  (^zana.prob.measure.WECDF [^doubles z]
    (let [n (int (alength z))
          w (double-array n (/ 1.0 n))]
      (make-WECDF z w))))
;;----------------------------------------------------------------
