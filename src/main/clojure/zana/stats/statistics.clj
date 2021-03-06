(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2018-04-16"
      :doc "Stats that don't have an obvious home." }
    
    zana.stats.statistics
  
  (:refer-clojure :exclude [min max])
  (:require [zana.geometry.r1 :as r1]
            [zana.geometry.z1 :as z1]
            [zana.collections.generic :as g]
            [zana.stats.accumulators :as accumulators])
  (:import [java.util List Map]
           [clojure.lang IFn IFn$OD IFn$OL]
           [org.apache.commons.math3.stat.descriptive 
            DescriptiveStatistics]
           [zana.java.functions IFnODWithMeta]
           [zana.java.math Statistics]
           [zana.java.prob ApproximatelyEqual]))
;;----------------------------------------------------------------
;; TODO: handle relative and absolute equality epsilons
;; TODO: move somewhere else?
(defn float-approximately== 
  ([^double ulps ^double x ^double y]
    (let [x (float x)
          y (float y)
          delta (* (float ulps) 
                   (Math/ulp 
                     (float (+ 1.0 (Math/abs x) (Math/abs y)))))]
      (<= (Math/abs (float (- x y))) delta)))
  ([^double x ^double y]
    (float-approximately== 1.0 x y)))
;;----------------------------------------------------------------
;; TODO: move somewhere else?
(defn approximately<= 
  ([^double ulps ^double x ^double y]
    (let [delta (* ulps 
                   (Math/ulp (+ 1.0 (Math/abs x) (Math/abs y))))]
      (<= (- x y) delta)))
  ([^double x ^double y]
    (approximately<= 1.0 x y)))

(defn approximately== 
  ([^double ulps ^double x ^double y]
    (let [delta (* ulps 
                   (Math/ulp (+ 1.0 (Math/abs x) (Math/abs y))))]
      (<= (Math/abs (- x y)) delta)))
  ([^double x ^double y] (approximately== 1.0 x y)))

(defn approximately>= 
  ([^double ulps ^double x ^double y]
    (let [delta (* ulps 
                   (Math/ulp (+ 1.0 (Math/abs x) (Math/abs y))))]
      (<= (- y x) delta)))
  ([^double x ^double y]
    (approximately<= 1.0 x y)))

(defn approximatelyEqual 
  ([^ApproximatelyEqual x ^ApproximatelyEqual y]
    (.approximatelyEqual x y))
  ([^ApproximatelyEqual x ^ApproximatelyEqual y & more]
    (and (approximatelyEqual x y)
         (apply approximatelyEqual y (first more) (rest more)))))

(defn doubles-approximately== 
  ([^double ulps ^doubles s0 ^doubles s1]
    (and (== (alength s0) (alength s1))
         (let [n (int (alength s0))]
           (loop [i (int 0)]
             (if (< i n)
               (let [z0 (aget s0 i)
                     z1 (aget s1 i)]
                 (if-not (approximately== ulps z0 z1)
                   false
                   (recur (inc i))))
               true)))))
  ([^doubles s0 ^doubles s1]
    (doubles-approximately== 1.0 s0 s1)))

(defn lists-approximately== 
  ([^double ulps ^List s0 ^List s1]
    (and (== (.size s0) (.size s1))
         (let [i0 (.iterator s0)
               i1 (.iterator s1)]
           (loop []
             (if (.hasNext i0)
               (let [z0 (double (.next i0))
                     z1 (double (.next i1))]
                 (if-not (approximately== ulps z0 z1)
                   false
                   (recur)))
               true)))))
  ([^List s0 ^List s1]
    (lists-approximately== 1.0 s0 s1)))

(defn maps-approximately== 
  ([^double ulps ^Map m0 ^Map m1]
    (and (= (.keySet m0) (.keySet m1))
         (let [it (.iterator (.keySet m0))]
           (loop []
             (if (.hasNext it)
               (let [k (.next it)
                     z0 (double (.get m0 k))
                     z1 (double (.get m1 k))]
                 (if-not (approximately== ulps z0 z1)
                   false
                   (recur)))
               true)))))
  ([^Map m0 ^Map m1]
    (maps-approximately== 1.0 m0 m1)))
;;----------------------------------------------------------------
;; TODO: move to function namespace
(defn numerical?
  "Does <code>f</code> return primitive <code>double</code> or <code>long</code>
   values? <br>
   Does <code>f</code> return a number for every element of <code>data</code>?"
  ([^IFn f]
    (or (instance? IFn$OD f)
        (instance? IFn$OL f)))
  ([^IFn f ^Iterable data]
    (try
      (or (numerical? f)
          (g/every? #(number? (f %)) data))
      (catch Throwable t
        (throw
          (RuntimeException.
            (print-str "numerical?" f (class data) (g/count data))
            t))))))
;;----------------------------------------------------------------
;; TODO: move to function namespace

(defn constantly-0d 
  "An instance of <code>IFn$OD</code> that returns a primitve
  <code>double</code> 0.0, regardless of input."
  (^double [] 0.0)
  (^double [arg] 0.0))

(defn constantly-1d 
  "An instance of <code>IFn$OD</code> that returns a primitve
  <code>double</code> 1.0, regardless of input."
  (^double [] 1.0)
  (^double [arg] 1.0))
;;----------------------------------------------------------------
(defn- singular-longs? [^IFn$OL z ^Iterable data]
  (let [it (g/iterator data)]
    (if (.hasNext it)
      (let [z0 (.invokePrim z (.next it))] 
        (loop []
          (if (.hasNext it)
            (let [z1 (.invokePrim z (.next it))]
              (if (== z0 z1) 
                (recur)
                false))
            true)))
      true)))

(defn- singular-doubles? [^IFn$OD z ^Iterable data]
  (let [it (g/iterator data)
        delta (Math/ulp (double 1.0))]
    (if-not (.hasNext it)
      true
      ;; get the 1st non-NaN, if any
      (let [z0 (double
                 (loop []
                   (if (.hasNext it)
                     (let [di (.next it)
                           zi (.invokePrim z di)]
                       (if (Double/isNaN zi)
                         (recur)
                         zi))
                     Double/NaN)))] 
        ;; if all NaN, then .hasNext is false
        ;; TODO: should this be a relative difference test?
        (loop []
          (if (.hasNext it)
            (let [d1 (.next it)
                  z1 (.invokePrim z d1)]
              (if (> (Math/abs (- z0 z1)) delta)
                false
                ;; else z1 is NaN and/or different from z0
                (recur)))
            true))))))

(defn- singular-objects? [^IFn z ^Iterable data]
  (if (nil? data)
    true
    (let [it (.iterator data)]
      (if-not (.hasNext it)
        true
        ;; get the 1st non-nil, if any
        (let [z0 (loop []
                   (when (.hasNext it)
                     (let [zi (z (.next it))]
                       (if (nil? zi) ;; false is not nil == missing
                         (recur)
                         zi))))] 
          ;; if all nil, then no more
          (loop []
            (if (.hasNext it)
              (let [z1 (z (.next it))]
                (if (or (nil? z1) (= z0 z1))
                  (recur)
                  false))
              true)))))))
;;----------------------------------------------------------------
(defn singular? 
  "Is there more than one distinct value in <code>a</code> or in the values of
   <code>z</code> mapped over <code>data</code>."
  ([^doubles a] (zana.java.arrays.Arrays/isSingular a))
  
  ([^IFn z ^Iterable data]
    (cond (instance? IFn$OD z) (singular-doubles? z data)
          (instance? IFn$OL z) (singular-longs? z data)
          :else (singular-objects? z data))))
;;----------------------------------------------------------------
;; a mess of functions computing bounds of various kinds.
;; TODO: rationalize this, maybe move some to a geometry package.
;; TODO: minmax, bounds, bounding-box, range are all variations on the same thing
;;----------------------------------------------------------------
;; TODO: handle any Comparable, accept comparator function
;; TODO: compare speed to Java implementation.
;; TODO: move to zana/tu.geometry?
(defn- bounds-slow 
  
  (^zana.java.geometry.r1.Interval [^IFn f
                                    ^Iterable data
                                    ^double min0
                                    ^double max0]
    (assert (not (g/empty? data)))
    (let [it (.iterator data)]
      (loop [xmin min0
             xmax max0]
        (if (.hasNext it)
          (let [x (double (f (.next it)))]
            (if (Double/isNaN x)
              (recur xmin xmax)
              (if (>= x xmin)
                (if (<= x xmax)
                  (recur xmin xmax)
                  (recur xmin x))
                (if (<= x xmax)
                  (recur x xmax)
                  (recur x x)))))
          (r1/interval xmin (Math/nextUp xmax)))))))
;;----------------------------------------------------------------
(defn- bounds-fast 
  
  (^zana.java.geometry.r1.Interval [^IFn$OD f
                                    ^Iterable data
                                    ^double min0
                                    ^double max0]
    (assert (not (g/empty? data)))
    (let [^IFn$OD f (if (instance? IFnODWithMeta f) 
                      (.functionOD ^IFnODWithMeta f) 
                      f)
          it (.iterator data)]
      (loop [xmin min0
             xmax max0]
        (if (.hasNext it)
          (let [x (.invokePrim f (.next it))]
            (if (Double/isNaN x)
              (recur xmin xmax)
              (if (>= x xmin)
                (if (<= x xmax)
                  (recur xmin xmax)
                  (recur xmin x))
                (if (<= x xmax)
                  (recur x xmax)
                  (recur x x)))))
          (r1/interval xmin (Math/nextUp xmax)))))))
;;----------------------------------------------------------------
(defn bounds 
  
  (^zana.java.geometry.r1.Interval [^Iterable data 
                                    ^double min0 
                                    ^double max0]
    (when-not (g/empty? data)
      (let [it (.iterator data)]
        (loop [xmin min0
               xmax max0]
          (if (.hasNext it)
            (let [x (double (.next it))]
              (if (Double/isNaN x)
                (recur xmin xmax)
                (if (>= x xmin)
                  (if (<= x xmax)
                    (recur xmin xmax)
                    (recur xmin x))
                  (if (<= x xmax)
                    (recur x xmax)
                    (recur x x)))))
            (r1/interval xmin (Math/nextUp xmax)))))))
  
  (^zana.java.geometry.r1.Interval [^Iterable data]
    (bounds data Double/POSITIVE_INFINITY Double/NEGATIVE_INFINITY))
  
  (^zana.java.geometry.r1.Interval [^IFn f
                                    ^Iterable data
                                    ^double min0
                                    ^double max0]
    (when-not (g/empty? data)
      (let [it (.iterator data)]
        (if (instance? IFn$OD f)
          (bounds-fast f data min0 max0)
          (bounds-slow f data min0 max0)))))
  
  (^zana.java.geometry.r1.Interval [^IFn f 
                                    ^Iterable data]
    (bounds f data Double/POSITIVE_INFINITY Double/NEGATIVE_INFINITY)))
;;----------------------------------------------------------------
(defn- bounding-box-slow 
  
  (^java.awt.geom.Rectangle2D$Double [^IFn xf
                                      ^IFn yf
                                      ^Iterable data]
    (let [it (.iterator data)]
      (loop [xmin Double/POSITIVE_INFINITY 
             xmax Double/NEGATIVE_INFINITY
             ymin Double/POSITIVE_INFINITY 
             ymax Double/NEGATIVE_INFINITY]
        (if (.hasNext it)
          (let [i (.next it)
                _ (assert i "no datum")
                x (xf i)
                _ (assert x (str "x:" xf " " i))
                y (yf i)
                _ (assert y (str "y:" yf " " i))
                x (double x)
                y (double y)
                ;; this will ignore NaNs
                xmin (if (< x xmin) x xmin)
                xmax (if (> x xmax) x xmax)
                ymin (if (< y ymin) y ymin)
                ymax (if (> y ymax) y ymax)]
            (recur xmin xmax ymin ymax))
          (java.awt.geom.Rectangle2D$Double.
            xmin ymin (- xmax xmin) (- ymax ymin)))))))
;;----------------------------------------------------------------
(defn- bounding-box-fast 
  
  (^java.awt.geom.Rectangle2D$Double [^IFn$OD xf
                                      ^IFn$OD yf
                                      ^Iterable data]
    (let [^IFn$OD xf (if (instance? IFnODWithMeta xf) 
                       (.functionOD ^IFnODWithMeta xf) 
                       xf)
          ^IFn$OD yf (if (instance? IFnODWithMeta yf)
                       (.functionOD ^IFnODWithMeta yf)
                       yf)
          it (.iterator data)]
      (loop [xmin Double/POSITIVE_INFINITY
             xmax Double/NEGATIVE_INFINITY
             ymin Double/POSITIVE_INFINITY
             ymax Double/NEGATIVE_INFINITY]
        (if (.hasNext it)
          (let [i (.next it)
                x (.invokePrim xf i)
                y (.invokePrim yf i)
                ;; this will ignore NaNs
                xmin (if (< x xmin) x xmin)
                xmax (if (> x xmax) x xmax)
                ymin (if (< y ymin) y ymin)
                ymax (if (> y ymax) y ymax)]
            (recur xmin xmax ymin ymax))
          (java.awt.geom.Rectangle2D$Double.
            xmin ymin (- xmax xmin) (- ymax ymin)))))))
;;----------------------------------------------------------------
(defn bounding-box 
  (^java.awt.geom.Rectangle2D$Double [^IFn xf
                                      ^IFn yf
                                      ^Iterable data]
    (if (and (instance? IFn$OD xf)
             (instance? IFn$OD yf))
      (bounding-box-fast xf yf data)
      ;;(Statistics/bounds xf yf data)
      (bounding-box-slow xf yf data))))
;;----------------------------------------------------------------
(defn symmetric-bounds
  ^java.awt.geom.Rectangle2D$Double [^IFn$OD xf
                                     ^IFn$OD yf
                                     ^Iterable data]
  (Statistics/symmetricBounds xf yf data))
;;----------------------------------------------------------------
(defn- minmax-long [^IFn$OL z ^Iterable data]
  (assert (not (g/empty? data)))
  (let [i (g/iterator data)
        zz (.invokePrim z (.next i))]
    (loop [z0 zz
           z1 zz]
      (if (.hasNext i)
        (let [zi (.invokePrim z (.next i))]
          (cond (< zi z0) (recur zi z1)
                (> zi z1) (recur z0 zi)
                :else (recur z0 z1)))
        [z0 z1]))))

(defn- minmax-double [^IFn$OD z ^Iterable data]
  (assert (not (g/empty? data)))
  (let [i (g/iterator data)
        zz (.invokePrim z (.next i))]
    (loop [z0 zz
           z1 zz]
      (if (.hasNext i)
        (let [zi (.invokePrim z (.next i))]
          (cond (< zi z0) (recur zi z1)
                (> zi z1) (recur z0 zi)
                :else (recur z0 z1)))
        [z0 z1]))))

(defn- minmax-comparable [^IFn z ^Iterable data]
  (assert (not (g/empty? data)))
  (let [i (g/iterator data)
        ^Comparable zz (z (.next i))]
    (loop [^Comparable z0 zz
           ^Comparable z1 zz]
      (if (.hasNext i)
        (let [^Comparable zi (z (.next i))]
          (cond (< (.compareTo zi z0) (int 0)) (recur zi z1)
                (> (.compareTo zi z1) (int 0)) (recur z0 zi)
                :else (recur z0 z1)))
        [z0 z1]))))


(defn minmax 
  "Return a 2-element vector containing the minimum and maximum values
   of <code>z</code> over <code>data</code>. The values of <code>z</code>
   need to be <code>Comparable</code>, or primitive numbers."
  ([z data]
    (cond (instance? IFn$OL z) (minmax-long z data)
          (instance? IFn$OD z) (minmax-double z data)
          (instance? IFn z) (minmax-comparable z data)
          :else (throw
                  (IllegalArgumentException.
                    (print-str "can't find the minmax values of " (class z)))))))
;;----------------------------------------------------------------
(defn fast-min ^double [^IFn$OD f ^Iterable data]
  (let [it (.iterator data)]
    (loop [xmin Double/POSITIVE_INFINITY]
      (if (.hasNext it)
        (let [x (.invokePrim f (.next it))]
          (if (Double/isNaN x)
            (recur xmin)
            (if (>= x xmin)
              (recur xmin)
              (recur x))))
        xmin))))
;;----------------------------------------------------------------
(defn min
  (^double [^IFn f ^Iterable data]
    (if (instance? IFn$OD f)
      (fast-min f data)
      (let [it (.iterator data)]
        (loop [xmin Double/POSITIVE_INFINITY]
          (if (.hasNext it)
            (let [x (double (f (.next it)))]
              (if (Double/isNaN x)
                (recur xmin)
                (if (>= x xmin)
                  (recur xmin)
                  (recur x))))
            xmin)))))
  (^double [^Iterable data]
    (let [it (.iterator data)]
      (loop [xmin Double/POSITIVE_INFINITY]
        (if (.hasNext it)
          (let [x (double (.next it))]
            (if (Double/isNaN x)
              (recur xmin)
              (if (>= x xmin)
                (recur xmin)
                (recur x))))
          xmin)))))
;;----------------------------------------------------------------
;; return Double/NEGATIVE_INFINITY for empty datasets
(defn fast-max ^double [^IFn$OD f ^Iterable data]
  (let [it (.iterator data)]
    (loop [xmax Double/NEGATIVE_INFINITY]
      (if (.hasNext it)
        (let [x (.invokePrim f (.next it))]
          (if (Double/isNaN x)
            (recur xmax)
            (if (<= x xmax)
              (recur xmax)
              (recur x))))
        xmax))))
;;----------------------------------------------------------------
;; return Double/NEGATIVE_INFINITY for empty datasets
(defn max
  (^double [^IFn f ^Iterable data]
    (if (instance? IFn$OD f)
      (fast-max f data)
      (let [it (.iterator data)]
        (loop [xmax Double/NEGATIVE_INFINITY]
          (if (.hasNext it)
            (let [x (double (f (.next it)))]
              (if (Double/isNaN x)
                (recur xmax)
                (if (<= x xmax)
                  (recur xmax)
                  (recur x))))
            xmax)))))
  (^double [^Iterable data]
    (let [it (.iterator data)]
      (loop [xmax Double/NEGATIVE_INFINITY]
        (if (.hasNext it)
          (let [x (double (.next it))]
            (if (Double/isNaN x)
              (recur xmax)
              (if (<= x xmax)
                (recur xmax)
                (recur x))))
          xmax)))))
;;----------------------------------------------------------------
(defn quantiles
  "Return a vector of the quantiles of the doubles in 
   <code>zs</code>, or the doubles resulting from mapping 
   <code>z</code> over <code>data</code>. 
   Return quantiles corresponding to the <code>ps</code>, which 
   must be numbers between 0.0 and 1.0 (both ends inclusive)."
  ([zs ps]
    (let [^doubles zs (into-array Double/TYPE zs)
          ds (DescriptiveStatistics. zs)]
      (mapv (fn ^double [^double p]
              ;; TODO: better 'equality' test?
              (cond (== 0.0 p) (.getMin ds)
                    (== 1.0 p) (.getMax ds)
                    :else (.getPercentile ds (* 100.0 p))))
            ps)))
  ([z data ps] (quantiles (g/map-to-doubles z data) ps)))
;;----------------------------------------------------------------
;; TODO: Kahan summation; use accumulators?
(defn sum
  "Compute the sum of the elements of an array, or of the 
   values of a function mapped over a data set."
  
  (^double [^doubles zs]
    (let [n (alength zs)]
      (loop [sum 0.0
             i 0]
        (if (< i n) 
          (recur (+ sum (aget zs i)) (inc i))
          sum))))
  
  (^double [^IFn z ^Iterable data]
    (cond 
      (instance? IFn$OD z)
      (let [^IFn$OD z z
            it (g/iterator data)]
        (loop [sum (double 0.0)]
          (if (.hasNext it) 
            (recur (+ sum (.invokePrim z (.next it))))
            sum)))
      
      (instance? IFn$OL z)
      (let [^IFn$OL z z
            it (g/iterator data)]
        (loop [sum (long 0)]
          (if (.hasNext it) 
            (recur (+ sum (.invokePrim z (.next it))))
            (double sum))))
      
      :else
      (let [it (g/iterator data)]
        (loop [sum (double 0.0)]
          (if (.hasNext it) 
            (recur (+ sum (double (z (.next it)))))
            (double sum)))))))
;;----------------------------------------------------------------
(defn mean
  "Compute the mean of the elements of an array, or of the 
   values of a function mapped over a data set."
  (^double [^doubles zs] (/ (sum zs) (alength zs)))
  (^double [^IFn z ^Iterable data]
    (/ (sum z data) (g/count data))))
;;----------------------------------------------------------------
;; TODO: Kahan summation
(defn l1-norm 
  "Compute the sum of absolute value of the elements of an array, 
   or of the values of a function mapped over a data set."
  (^double [^doubles zs]
    (let [n (alength zs)]
      (loop [sum 0.0
             i 0]
        (if (< i n) 
          (recur (+ sum (Math/abs (aget zs i))) (inc i))
          sum))))
  (^double [^IFn$OD z ^Iterable data]
    (let [it (g/iterator data)]
      (loop [sum 0.0]
        (if (.hasNext it) 
          (recur (+ sum (Math/abs (.invokePrim z (.next it)))))
          sum)))))
;;----------------------------------------------------------------
;; TODO: Kahan summation
(defn l1-distance 
  "Compute the sum of absolute differences between 2 arrays, or 
   between the values of 2 functions mapped over a data set."
  (^double [^doubles z0 ^doubles z1]
    (let [n (alength z0)]
      (assert (== n (alength z1)))
      (loop [sum 0.0
             i 0]
        (if (< i n) 
          (recur (+ sum (Math/abs (- (aget z0 i) (aget z1 i)))) 
                 (inc i))
          sum))))
  ;  (^double [^IFn$OD z0 ^IFn$OD z1 ^Iterable data]
  ;    (let [it (g/iterator data)]
  ;      (loop [sum 0.0]
  ;        (if (.hasNext it) 
  ;          (let [datum (.next it)]
  ;            (recur (+ sum (Math/abs 
  ;                            (- (.invokePrim z0 datum)
  ;                               (.invokePrim z1 datum))))))
  ;          sum)))))
  (^double [^IFn$OD z0 ^IFn$OD z1 ^Iterable data]
    (let [absolute-residual (fn absolute-residual ^double [datum]
                              (Math/abs (- (.invokePrim z0 datum)
                                           (.invokePrim z1 datum))))]
      (l1-norm (g/pmap-doubles absolute-residual data)))))
;;----------------------------------------------------------------
(defn mean-absolute-difference
  "Compute the mean absolute difference between the elements of 2 
   arrays, or between the values of 2 functions mapped over a data 
   set."
  (^double [^doubles z0 ^doubles z1]
    (/ (l1-distance z0 z1) (alength z0)))
  (^double [^IFn$OD z0 ^IFn$OD z1 ^Iterable data]
    (/ (l1-distance z0 z1 data) (g/count data))))
;;----------------------------------------------------------------
(defn qr-cost
  
  "Compute the sum of usual quantile regression costs:
   <code>(if (<= 0 dz) (* p dz) (* (- p 1) dz))</code>,
   where <code>dz == (- z0 z1)</code>."
  
  (^double [^double p 
            ^doubles z0 
            ^doubles z1]
    (let [n (alength z0)
          p- (- p 1.0)]
      (assert (== n (alength z1)))
      (loop [sum 0.0
             i 0]
        (if (< i n) 
          (let [dz (- (aget z0 i) (aget z1 i))
                ds (if (<= 0.0 dz) (* p dz) (* p- dz))]
            (recur (+ sum ds) (inc i)))
          (* 2.0 sum)))))
  
  (^double [^double p
            ^IFn$OD z0 
            ^IFn$OD z1 
            ^Iterable data]
    (let [p- (- p 1.0)
          it (.iterator data)]
      (loop [sum 0.0]
        (if (.hasNext it)
          (let [datum (.next it)
                dz (- (.invokePrim z0 datum)
                      (.invokePrim z1 datum))
                ds (if (<= 0.0 dz) (* p dz) (* p- dz))]
            (recur (+ sum ds)))
          (* 2.0 sum))))))
;;----------------------------------------------------------------
(defn mean-qr-cost
  
  "Compute the mean of usual quantile regression costs:
   <code>(if (<= 0 dz) (* p dz) (* (- p 1) dz))</code>,
   where <code>dz == (- z0 z1)</code>."
  
  (^double [^double p ^doubles z0 ^doubles z1]
    (/ (qr-cost p z0 z1) (alength z0)))
  (^double [^double p ^IFn$OD z0 ^IFn$OD z1 ^Iterable data]
    (/ (qr-cost p z0 z1 data) (g/count data))))
;;----------------------------------------------------------------
(defn rq-cost
  
  "Compute the sum of a better (?) scaling of the usual quantile 
   regression costs: 
   <code>(if (<= 0 dz) (/ dz (- 1.0 p)) (/ (- dz) p))</code>,
   where <code>dz == (- z0 z1)</code>."
  
  (^double [^double p 
            ^doubles z0 
            ^doubles z1]
    (let [n (alength z0)
          p- (- p)
          p+ (- 1.0 p)]
      (assert (== n (alength z1)))
      (loop [sum 0.0
             i 0]
        (if (< i n) 
          (let [dz (- (aget z0 i) (aget z1 i))
                ds (if (<= 0.0 dz) (/ dz p+) (/ dz p-))]
            (recur (+ sum ds) (inc i)))
          (* 0.5 sum)))))
  
  (^double [^double p
            ^IFn$OD z0 
            ^IFn$OD z1 
            ^Iterable data]
    (let [p- (- p)
          p+ (- 1.0 p)
          it (.iterator data)]
      (loop [sum 0.0]
        (if (.hasNext it)
          (let [datum (.next it)
                dz (- (.invokePrim z0 datum)
                      (.invokePrim z1 datum))
                ds (if (<= 0.0 dz) (/ dz p+) (/ dz p-))]
            (recur (+ sum ds)))
          (* 0.5 sum))))))
;;----------------------------------------------------------------
(defn mean-rq-cost
  
  "Compute the mean of alternate quantile regression costs:
   <code>(if (<= 0 dz) (/ dz (- 1 p)) (/ (- dz) p)</code>,
   where <code>dz == (- z0 z1)</code>."
  
  (^double [^double p ^doubles z0 ^doubles z1]
    (/ (rq-cost p z0 z1) (alength z0)))
  (^double [^double p ^IFn$OD z0 ^IFn$OD z1 ^Iterable data]
    (/ (rq-cost p z0 z1 data) (g/count data))))
;;----------------------------------------------------------------
;; TODO: more accurate summation
(defn l2-norm 
  "Compute the sum of squares of the elements of an array, or of the values
   of a function mapped over a data set."
  (^double [^doubles zs]
    (let [n (alength zs)]
      (loop [sum 0.0
             i 0]
        (if (< i n) 
          (let [zi (aget zs i)]
            (recur (+ sum (* zi zi)) (inc i)))
          sum))))
  (^double [^IFn$OD z ^Iterable data]
    (let [it (g/iterator data)]
      (loop [sum 0.0]
        (if (.hasNext it) 
          (let [zi (.invokePrim z (.next it))]
            (recur (+ sum (* zi zi))))
          sum)))))
;;----------------------------------------------------------------
;; TODO: more accurate summation. g/reduce-double
(defn l2-distance 
  "Compute the L1 distance between 2 arrays, or between the values of
   2 functions mapped over a data set."
  (^double [^doubles z0 ^doubles z1]
    (let [n (alength z0)]
      (assert (== n (alength z1)))
      (loop [sum 0.0
             i 0]
        (if (< i n) 
          (let [dz (- (aget z0 i) (aget z1 i))]
            (recur (+ sum (* dz dz)) (inc i)))
          sum))))
  (^double [^IFn$OD z0 ^IFn$OD z1 ^Iterable data]
    (let [n (g/count data)
          it (g/iterator data)]
      (loop [sum 0.0]
        (if (.hasNext it) 
          (let [datum (.next it)
                dz (- (.invokePrim z0 datum) (.invokePrim z1 datum))]
            (recur (+ sum (* dz dz))))
          sum)))))
;;----------------------------------------------------------------
(defn rms-difference
  "Return the square root of the [[l2-distance]]."
  (^double [^doubles z0 ^doubles z1]
    (Math/sqrt (/ (l2-distance z0 z1) (alength z0))))
  (^double [^IFn$OD z0 ^IFn$OD z1 ^Iterable data]
    (Math/sqrt (/ (l2-distance z0 z1 data) (g/count data)))))
;;----------------------------------------------------------------
