(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-10-20"
      :doc "Convert attribute functions to corresponding (fractional) ranks
            over some data set." }
    
    zana.stats.ranks
  
  (:require [zana.collections.guava :as guava]
            [zana.collections.generic :as g]
            [zana.functions.wrappers :as wrap])
  (:import [clojure.lang IFn IFn$OD IFn$OL]))
;;------------------------------------------------------------------------------
(defn ranks 
  "Return a function whose domain is some data set, and whose range is the 
  integer ranks over of the elements of that data set, or 
  <code>Long/MIN_VALUE</code> if applied to anything else.
  (Should we throw an exception if applied to something outside the domain
  data set?)
  <dl>
  <dt><code>^clojure.lang.IFn$OL [^Iterable data]</code></dt>
  <dd>Return a function that maps the elements of <code>data</code> to their
  zero-based rank in whatever order the data is presented. Throw an exception
  if the same element occurs twice in <code>data</code>.
  </dd>
  <dt><code>^clojure.lang.IFn$OL [^clojure.lang.IFn z ^Iterable data]</code></dt>
  <dd>Equivalent to <code>(ranks (sort-by z data))</code></dd>
  </dl>
  **TODO:** accept a Comparator?
  "
  (^clojure.lang.IFn$OL [^Iterable data]
    (let [m (com.carrotsearch.hppc.ObjectLongHashMap. (g/count data))]
      (g/mapc 
        (fn [i xi] 
          (assert (> (long 0) (.getOrDefault m xi Long/MIN_VALUE))
                  (print-str "Duplicate element in data:\n" xi))
          (.put m xi (long i))) 
        (range (g/count data)) 
        data)
      (wrap/lookup-function m)))
  (^clojure.lang.IFn$OL [^clojure.lang.IFn z ^Iterable data]
    (ranks (guava/sort-by z data))))
;;------------------------------------------------------------------------------
(defn franks
  "Return a function whose domain is some data set, and whose range is the 
  fractional ranks, equivalent to:
  <pre>
  <code>(fn [x] 
          (/ ((ranks z data) x)
             (- (count data) 1)))
  </code>
  </pre>
  We decrement the data set size so the fractional rank goes from 0 to 1.<br>
  See [[ranks]]."
  (^clojure.lang.IFn$OD [^Iterable data]
    (let [n (g/count data)
          n-1 (double (- n 1))
          m (com.carrotsearch.hppc.ObjectDoubleHashMap. n)]
      (g/mapc
        (fn [i xi] 
          (assert (Double/isNaN (.getOrDefault m xi Double/NaN))
                  (print-str "Duplicate element in data:\n" xi))
          (.put m xi (/ (double i) n-1)) 
          (range (g/count data))
          data)
        (wrap/lookup-function m))))
  (^clojure.lang.IFn$OD [^clojure.lang.IFn z ^Iterable data]
    (franks (guava/sort-by z data))))
;;------------------------------------------------------------------------------
