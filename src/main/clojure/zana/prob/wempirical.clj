(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "palisades dot lakes at gmail dot com" 
      :since "2017-10-24"
      :date "2017-10-24"
      :doc "Weighted empirical probability measures over <b>R</b>." }
    
    zana.prob.wempirical
  
  (:import [java.util Arrays]
           [zana.java.arrays Sorter]))
;;----------------------------------------------------------------
;; Weighted empirical probabilty density, a collection of point
;; masses.
;; <ul>
;; <li><code>ws</code> weights.
;; <li><code>zs</code> sorted unique domain values.
;; </ul>
;; Probability of <code>z==z[i]</code> is <code>w[i]</code>.
(deftype PDF [^doubles w 
              ^doubles z])
;;----------------------------------------------------------------
(defn- compact [^doubles w ^doubles z]
  (assert (not (nil? w)))
  (assert (not (nil? z)))
  (let [n (int (alength w))]
    (assert (== n (alength z)))
    (loop [w0 (aget w 0)
           z0 (aget z 0)
           i0 (int 0)
           i1 (int 1)]
      (if (>= i1 n) 
        ;; done, copy into shorter arrays if needed
        (let [nn (inc i0)]
          (if (== nn n)
            [w z]
            [(Arrays/copyOf w nn) (Arrays/copyOf z nn)]))
        ;; check for continuing ties in z
        (let [w1 (aget w i1)
              z1 (aget z i1)]
          (if (== z0 z1) 
            ;; tie, increment weight, move right counter
            (let [w0 (+ w0 w1)]
              (aset-double w i0 w0)
              (recur w0 z0 i0 (inc i1)))
            ;; no tie, increment both counters
            ;; copy to left counter if needed
            (let [i0 (inc i0)]
              (when-not (== i0 i1) 
                (aset-double w i0 w1)
                (aset-double z i0 z1))
              (recur w1 z1 i0 (inc i1)))))))))
;;----------------------------------------------------------------
;; TODO: normalize w?
(defn- make-PDF 
  
  "Create an instance of <code>zana.prob.wempirical.PDF</code>.
  Sorts <code>z</code> and removes ties." 
  
  (^PDF [^doubles w ^doubles z]
    (assert (== (alength w) (alength z)))
    (Sorter/quicksort z w)
    (let [[^doubles w ^doubles z] (compact w z)]
      (PDF. w z)))
  
  (^PDF [^doubles z]
    (let [n (int (alength z))]
      (make-PDF (double-array n (/ 1.0 n)) z))))
;;----------------------------------------------------------------
;; Weighted empirical cumulative probabilty, a non-decreasing step
;; function mapping <b>R</b> to [0,1].
;; <ul>
;; <li><code>ws</code> increasing weights.
;; <li><code>zs</code> sorted unique domain values.
;; </ul>
;; Cumulative probability of <code>z<=z[i]</code> is 
;; <code>w[i]</code>.
(deftype CDF [^doubles w
              ^doubles z])

;;----------------------------------------------------------------