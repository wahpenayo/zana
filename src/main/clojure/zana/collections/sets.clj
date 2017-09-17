(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-08-31" 
      :doc "Set utilities."}

    zana.collections.sets
  
  (:refer-clojure :exclude [contains? distinct intersection union])
  (:require [zana.collections.generic :as g])
  (:import [java.util Collection Collections HashSet]
           [com.carrotsearch.hppc LongHashSet]
           [com.google.common.collect ImmutableSet Sets]))
;;------------------------------------------------------------------------------
;; TODO: performance testing of guava vs java.util
;; TODO: nil as a set element: follow guava (not allowed) or java.util (allowed)?
;;------------------------------------------------------------------------------
;; Because clojure.core/contains? should really be has-key?
;; Note: allows nil elements
(defn contains? 
  "Does <code>things</code> contain an element equal to <code>thing</code>?<br>
   This function implements the normal English meaning of 'contains?', in 
   contrast to 
   <a href=\"http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/contains?\">
   clojure.core/contains?</a>, which assumes <code>things</code> is an indexed 
   collection of some kind and checks if <code>thing</code> is one of the valid
   keys, leading to one of the more annoying Clojure gotchas:
   ```(clojure.core/contains? [4 5 6] 4) -> false
      (clojure.core/contains? [4 5 6] 0) -> true
      (zana.api/contains? [4 5 6] 4) -> true
      (zana.api/contains? [4 5 6] 0) -> false
    ```"
  [things thing]
  (and
    things
    (cond (instance? java.util.Set things)
          (.contains ^java.util.Set things thing)
          (instance? Iterable things)
          (let [i (g/iterator things)]
            (loop []
              (when (g/has-next? i)
                (if (= thing (g/next-item i)) true (recur)))))
          :else
          (throw
            (IllegalArgumentException.
              (str "don't know how to check if " things " contains " thing))))))
;;------------------------------------------------------------------------------
(defn union 
  "Return a set of the elements in all the <code>Iterable</code>s.
   <em>Note:</em> <b>Does</b> permit <code>nil</code> elements.
   Likely to change for consistency with [[intersection]]."
  ^java.util.Set [& iterables]
  (let [b (HashSet.)]
    (doseq [^Iterable it iterables] (.addAll b it))
    (Collections/unmodifiableSet b)))
;;------------------------------------------------------------------------------
(defn intersection 
  "Return a set of the elements in both <code>^Collection s0</code> and
  <code>^Collection s1</code>.<br>
  <em>Note:</em> <b>Does not</b> permit <code>nil</code> elements.
  Likely to change for consistency with [[intersects?]]."
  ^java.util.Set [^Collection s0 ^Collection s1]
  (let [^Set s0 (if (instance? java.util.Set s0) s0 (java.util.HashSet. s0))
        ^Set s1 (if (instance? java.util.Set s1) s1 (java.util.HashSet. s1))]
    (Sets/intersection s0 s1)))
;;------------------------------------------------------------------------------
(defn intersects? 
  "Test for any elements in common, without computing the intersection.<br>
  <em>Note:</em> <b>Does</b> permit <code>nil</code> elements.
  Likely to change for consistency with [[intersection]]."
  
  [things0 things1]
  (and
    things0
    things1
    (cond (and (instance? java.util.Collection things0)
               (instance? java.util.Collection things1))
          (not
            (java.util.Collections/disjoint 
              ^java.util.Collection things0 
              ^java.util.Collection things1))
          :else
          (throw (IllegalArgumentException.
                   (str "don't know how to check if\n " things0
                        " intersects\n" things1))))))
;;------------------------------------------------------------------------------
;; collecting distinct values
;; TODO: significant performance gain with primitive specializations?
;;------------------------------------------------------------------------------
(defn distinct
  "Like <code>(into #{} (filter identity things))</code> or
        <code>(into #{} (filter identity (map f things)))</code>.
   <em>Note:</em> <b>Does</b> permit <code>nil</code> elements."
  (^java.util.Set [things]
    (let [s (java.util.HashSet.)
          it (g/iterator things)]
      (while (g/has-next? it)
        (when-let [xi (g/next-item it)] (.add s xi)))
      (java.util.Collections/unmodifiableSet s)))
  (^java.util.Set [f things]
    (assert (ifn? f) (print-str "Not a function:" f))
    (let [s (java.util.HashSet.)
          it (g/iterator things)]
      (while (g/has-next? it)
        (when-let [fi (f (g/next-item it))] (.add s fi)))
      (java.util.Collections/unmodifiableSet s))))
;;------------------------------------------------------------------------------
;; counting distinct values
;;------------------------------------------------------------------------------
(defn- count-distinct-doubles 
  "Return the number of the distinct double values of f over data."
  ^long [^clojure.lang.IFn$OD f ^Iterable data]
  ;; see http://issues.carrot2.org/browse/HPPC-144
  (let [hs (LongHashSet.)]
    (g/mapc #(.add hs (Double/doubleToLongBits (.invokePrim f %))) data)
    (.size hs)))
;;------------------------------------------------------------------------------
(defn- count-distinct-longs 
  "Return the number of distinct long values of f over data."
  ^long [^clojure.lang.IFn$OL f ^Iterable data]
  (let [hs (LongHashSet.)]
    (g/mapc #(.add hs (.invokePrim f %)) data)
    (.size hs)))
;;------------------------------------------------------------------------------
;; Note: allows nil elements
(defn- count-distinct-objects 
  "Return the number of distinct values of f over data."
  ^long [^clojure.lang.IFn f ^Iterable data]
  (assert (ifn? f) (print-str "Not a function:" f))
  (let [hs (java.util.HashSet.)]
    (g/mapc #(.add hs (f %)) data)
    (.size hs)))
;;------------------------------------------------------------------------------
;; Note: allows nil elements for object-valued functions
(defn count-distinct
  "Return the number of distinct values of f over data."
  ^long [^clojure.lang.IFn f ^Iterable data]
  (assert (ifn? f) (print-str "Not a function:" f))
  (cond (instance? clojure.lang.IFn$OD f) (count-distinct-doubles f data)
        (instance? clojure.lang.IFn$OL f) (count-distinct-longs f data)
        :else (count-distinct-objects f data)))
;;------------------------------------------------------------------------------
;; Note: does not allow nil elements
(defn distinct-by 
  "Picks an arbitrary representative from <code>things</code> for every distinct
   value of <code>(f thing)</code>"
  ^Iterable [f things]
  (assert (ifn? f) (print-str "Not a function:" f))
  (let [m (java.util.HashMap.)
        it (g/iterator things)]
    (while (g/has-next? it)
      (let [xi (g/next-item it)
            fi (f xi)]
        (when fi (.put m fi xi))))
    (Collections/unmodifiableCollection (.values m))))
;;------------------------------------------------------------------------------
;; eq/==/identical? 'sets'
;;------------------------------------------------------------------------------
;; Note: does not allow nil elements
(defn distinct-identity
  "Reduce to a set of the value of x or (f x), for x in things, which are 
   different in the sense of identical? (java ==)."
  (^Iterable [things]
    (let [ihm (java.util.IdentityHashMap.)
          it (g/iterator things)]
      (while (g/has-next? it)
        (let [xi (g/next-item it)] (.put ihm xi xi)))
      (Collections/unmodifiableCollection (.values ihm))))
  (^Iterable [f things]
    (assert (ifn? f) (print-str "Not a function:" f))
    (let [ihm (java.util.IdentityHashMap. (int (count things)))
          it (g/iterator things)]
      (while (g/has-next? it)
        (let [fi (f (g/next-item it))]
          (when fi (.put ihm fi fi))))
      (Collections/unmodifiableCollection (.values ihm)))))
;;------------------------------------------------------------------------------
;; Note: does not allow nil elements
(defn distinct-identity-by ^Iterable [f things]
  "Reduce to a collection of the things where the values of (f x) are
   different in the sense of identical? (java ==). Skip nil values of f."
  (assert (ifn? f) (print-str "Not a function:" f))
  (let [ihm (java.util.IdentityHashMap.)
        it (g/iterator things)]
    (while (g/has-next? it)
      (let [xi (g/next-item it)
            fi (f xi)]
        (when fi (.put ihm fi xi))))
    (Collections/unmodifiableCollection (.values ihm))))
;;------------------------------------------------------------------------------
