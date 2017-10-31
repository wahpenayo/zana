(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :since "2017-10-24"
      :date "2017-10-27"
      :doc "Probability measures over <b>R</b>." }
    
    zana.prob.measure
  
  (:refer-clojure :exclude [every? ])
  
  (:import [java.util Arrays]
           [com.carrotsearch.hppc DoubleArrayList]
           [clojure.lang IFn$DO IFn$DDO]
           [zana.java.arrays Sorter]))
;;----------------------------------------------------------------
;; TODO: use float arrays but calculate in double to eliminate 
;; Math/ulp in comparisons?
;; Probably want to move interface and classes to Java in that case...
;;----------------------------------------------------------------
;; TODO: move elsewhere
(defn- positive? [^double z] (< 0.0 z))
(defn- non-negative? [^double z] (<= 0.0 z))
(defn- convex-weight? [^double z] (<= 0.0 z 1.0))

(defn- every? 
  ([^IFn$DO f ^doubles z]
    (let [n (int (alength z))]
      (loop [i (int 0)]
        (cond (<= n i) true
              (.invokePrim f (aget z i)) (recur (inc i))
              :else false))))
  ([^IFn$DDO f ^doubles z0 ^doubles z1]
    (assert (== (alength z0) (alength z1)))
    (let [n (int (alength z0))]
      (loop [i (int 0)]
        (cond 
          (<= n i) true
          (.invokePrim f (aget z0 i) (aget z1 i)) (recur (inc i))
          :else false)))))

(defn- increasing? [^doubles z]
  (let [n (int (alength z))]
    (loop [i (int 1)
           z0 (aget z 0)]
      (if (<= n i) 
        true
        (let [z1 (aget z i)]
          (if (>= z0 z1)
            false
            (recur (inc i) z1)))))))

(defn- non-decreasing? [^doubles z]
  (let [n (int (alength z))]
    (loop [i (int 1)
           z0 (aget z 0)]
      (if (<= n i) 
        true
        (let [z1 (aget z i)]
          (if (> z0 z1)
            false
            (recur (inc i) z1)))))))

(defn- approximately== 
  ([^double x ^double y ^double delta]
    (<= (Math/abs (- x y)) delta))
  ([^double x ^double y]
    (approximately== 
      x y (Math/ulp (* 10.0 (+ (Math/abs x) (Math/abs y)))))))

(defn- naive-sum 
  (^double [^doubles z]
    (let [n (int (alength z))]
      (loop [i (int 0)
             s (double 0.0)]
        (if (<= n i)
          s
          (recur (inc i) (+ s (aget z i)))))))
  (^double [^doubles z ^long i0 ^long i1]
    (let [i1 (int i1)]
      (loop [i (int i0)
             s (double 0.0)]
        (if (<= i1 i)
          s
          (recur (inc i) (+ s (aget z i))))))))

(defn- convex? [^doubles z]
  (and (every? convex-weight? z)
       (approximately== 1.0 (naive-sum z))))

(defn- quicksort 
  "Non-destructive sort. <code>z</code> is made non-decreasing
   and <code>w</code> is permuted in the same way."
  [^doubles z ^doubles w]
  (assert (== (alength z) (alength w)))
  (let [z (aclone z)
        w (aclone w)]
    (Sorter/quicksort z w)
    [z w]))
;;----------------------------------------------------------------
(defn- compact [^doubles z ^doubles w]
  (assert (non-decreasing? z))
  (assert (every? positive? w))
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
(defn- normalize
  "Return a new array which is an element-wise scaler multiple of
   <code>w</code>, whose elements sum to 1.
   Elements of <code>w</code> must be non-negaitve."
  (^doubles [^doubles w]
    (assert (every? non-negative? w))
    (let [s (/ 1.0 (naive-sum w))
          n (int (alength w))
          ^doubles u (double-array n)]
      (dotimes[i n] 
        (aset-double 
          u i 
          (Math/min (Math/max 0.0 (* (aget w i) s)) 1.0)))
      u)))
;;----------------------------------------------------------------
;; TODO: replace naive sum with something more accurate
(defn- normalized-sums
  "Return a new array whose ith element is the sum from 0 to i
   (inclusive) of the elements of <code>w</code>,
   divided by the sum of all the elements."
  (^doubles [^doubles w]
    (assert (every? non-negative? w))
    (let [s (/ 1.0 (naive-sum w))
          n (int (alength w))
          ^doubles u (double-array n)
          u0 (* s (aget w 0))
          u0 (Math/min (Math/max 0.0 u0) 1.0) ]
      (aset-double u 0 u0)
      (loop [i (int 1)
             u0 u0]
        (if (>= i n) 
          u
          (let [u1 (+ u0 (* s (aget w i)))
                u1 (Math/min (Math/max 0.0 u1) 1.0)]
            (aset-double u i u1)
            (recur (inc i) u1)))))))
;;----------------------------------------------------------------
;; TODO: replace naive sum with something more accurate
;; TODO: ensure convex?
(defn- difference
  "Return a new array whose ith element is the difference 
   <code>(- (aget w i) (aget w (dec i)))</code>
   where <code>(aget w -1)</code> is treated as zero."
  (^doubles [^doubles w]
    (let [n (int (alength w))
          ^doubles dw (double-array n)
          w0 (aget w 0)]
      (aset-double dw 0 w0)
      (loop [i (int 1)
             w0 w0]
        (if (>= i n) 
          dw
          (let [w1 (aget w i)]
            (aset-double dw i (- w1 w0))
            (recur (inc i) w1)))))))
;;----------------------------------------------------------------
(defn- dmapcat 
  "Assume f returns <code>double[]</code>. Concatenate the arrays
   into one big array."
  ^doubles [f ^Iterable objects]
  (let [arrays (mapv f objects)
        m (count arrays)
        n (int (reduce + 0 (map #(alength ^doubles %) arrays)))
        out (double-array n)]
    (loop [i 0
           arrays arrays] 
      (if (empty? arrays)
        out
        (let [^doubles zi (first arrays)
              ni (alength zi)]
          (System/arraycopy zi 0 out i ni)
          (recur (+ i ni) (rest arrays)))))))
;;----------------------------------------------------------------
;; Probability measure &mu; on the real line, <b>R</b>.
;; Not supporting mass at +/-&infin;.
(definterface RealProbabilityMeasure
  ;; TODO: probability of more general sets
  
  ;; Probability of the singleton &mu;({z})
  (^double pointmass [^double z])
  
  ;; Probability of the half line &mu;((-&infin;,z]).
  (^double cdf [^double z])
  
  ;; Pseudo inverse of <code>cdf</code>.
  ;; <code>(quantile p)</code> is infimum of the <code>z</code
  ;; where <code>(>= (cdf z) p)</code>.
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
          (Math/min (Math/max 0.0 (naive-sum w 0 k)) 1.0)))))
  
  (^double quantile [this ^double p]
    (assert (convex-weight? p) p)
    (double
      (cond (== 0.0 p) Double/NEGATIVE_INFINITY
            (== 1.0 p) (aget z (dec (alength z)))
            :else 
            (let [n (int (alength z))]
              (loop [i0 (int 0)
                     s0 (aget w 0)]
                ;; shouldn't be possible to go outside array bounds 
                ;; with valid <code>z,w</code>.
                (assert (< i0 n))
                (if (< (- p s0) (Math/ulp 1.0)) 
                  (aget z i0)
                  (let [i1 (inc i0)
                        s1 (+ s0 (aget w i1))
                        s1 (Math/min (Math/max 0.0 s1) 1.0)]
                    (recur i1 s1))))))))
  
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
         (every? approximately== z ^doubles (.z ^WEPDF that))
         (every? approximately== w ^doubles (.w ^WEPDF that))))
  
  ;; TODO: fix for large sample arrays
  (toString [this]
    (str "(WEPDF " (vec z) " " (vec w) ")")))
;;----------------------------------------------------------------
;; TODO: normalize w?
(defn make-wepdf 
  
  "Create an instance of <code>zana.prob.measure.WEPDF</code>.
  Sorts <code>z</code> and removes ties." 
  
  (^zana.prob.measure.WEPDF [z w]
    (assert (not (nil? z)))
    (assert (not (nil? w)))
    (let [^doubles z (if (instance? DoubleArrayList z)
                       (.toArray ^DoubleArrayList z)
                       z)
          ^doubles w (if (instance? DoubleArrayList w)
                       (.toArray ^DoubleArrayList w)
                       w)
          [^doubles z ^doubles w] (quicksort z w)
          [^doubles z ^doubles w] (compact z w)
          w (normalize w)]
      (assert (increasing? z))
      (assert (convex? w))
      (WEPDF. z w)))
  
  (^zana.prob.measure.WEPDF [z]
    (assert (not (nil? z)))
    (let [^doubles z (if (instance? DoubleArrayList z)
                       (.toArray ^DoubleArrayList z)
                       z)
          n (int (alength z))
          w (double-array n 1.0)]
      (make-wepdf z w))))
;;----------------------------------------------------------------
(defn average-wepdfs
  "Return the mean probability measure."
  (^zana.prob.measure.WEPDF [& wepdfs]
    (let [zs (dmapcat #(.z ^WEPDF %) wepdfs)
          ws (dmapcat #(.w ^WEPDF %) wepdfs)]
      (make-wepdf zs ws))))
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
    (assert (convex-weight? p))
    (double
      (cond (== 0.0 p) Double/NEGATIVE_INFINITY
            (== 1.0 p) (aget z (dec (alength z)))
            :else 
            (let [i (int (Arrays/binarySearch w p))
                  ii (int (if (<= 0 i) i (- -1 i)))
                  zii (aget z ii)
                  zii-1 (if (< 0 ii) (aget z (dec ii)) Double/NEGATIVE_INFINITY)
                  pii-1 (.cdf this zii-1)]
              #_(println p i ii zii zii-1)
              #_(println (.cdf this zii))
              #_(println (.cdf this zii-1))
              (if (< (- p pii-1) (Math/ulp 1.0))
                zii-1
                zii)))))
  
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
         (every? approximately== z ^doubles (.z ^WECDF that))
         (every? approximately== w ^doubles (.w ^WECDF that))))
  
  ;; TODO: fix for large sample arrays
  (toString [this]
    (str "(WECDF " (vec z) " " (vec w) ")")))
;;----------------------------------------------------------------
;; TODO: normalize w?
(defn make-WECDF 
  
  "Create an instance of <code>zana.prob.measure.WECDF</code>.
  Sorts <code>z</code> and removes ties." 
  
  (^zana.prob.measure.WECDF [z w]
    (assert (not (nil? z)))
    (assert (not (nil? w)))
    (let [^doubles z (if (instance? DoubleArrayList z)
                       (.toArray ^DoubleArrayList z)
                       z)
          ^doubles w (if (instance? DoubleArrayList w)
                       (.toArray ^DoubleArrayList w)
                       w)
          [^doubles z ^doubles w] (quicksort z w)
          #_(println (vec z) (vec w))
          [^doubles z ^doubles w] (compact z w)
          #_(println (vec z) (vec w))
          ^doubles w (normalized-sums w)]
      #_(println (vec z) (vec w))
      (assert (increasing? z) (str (vec z)))
      (assert (increasing? w) (str (vec w)))
      (assert (every? convex-weight? w) (str (vec w)))
      (assert (approximately== 1.0 (aget w (dec (alength w))))
              (str (vec w)))
      
      (WECDF. z w)))
  
  (^zana.prob.measure.WECDF [z]
    (let [^doubles z (if (instance? DoubleArrayList z)
                       (.toArray ^DoubleArrayList z)
                       z)
          n (int (alength z))
          w (double-array n 1.0)]
      (make-WECDF z w))))
;;----------------------------------------------------------------
(defn wepdf-to-wecdf
  "Convert a point mass density representation to a cumulative one."
  (^zana.prob.measure.WECDF [^zana.prob.measure.WEPDF pdf]
    (make-WECDF (.z pdf) (.w pdf))))
(defn wecdf-to-wepdf
  "Convert a cumulative representation to a point mass density one."
  (^zana.prob.measure.WEPDF [^zana.prob.measure.WECDF cdf]
    (make-wepdf (.z cdf) (difference (.w cdf)))))
;;----------------------------------------------------------------
(defn quantile ^double [^RealProbabilityMeasure rpm ^double p]
  (.quantile rpm p))
(defn cdf ^double [^RealProbabilityMeasure rpm ^double z]
  (.cdf rpm z))
;;----------------------------------------------------------------
