(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald, Kristina Lisa Klinkner" :date "2016-10-20"
      :doc "Turn Java data structures into Clojure functions.
            Really just specialized versions of <code>memoize</code>." }
    
    zana.functions.wrappers
  
  (:refer-clojure :exclude [with-meta])
  (:require [clojure.string :as s]
            [zana.commons.core :as commons]
            [zana.collections.generic :as g]
            [zana.collections.hppc :as hppc]
            [zana.collections.guava :as guava]
            [zana.functions.generic :as zfg]
            [zana.functions.inverse :as inverse]
            [zana.geometry.z1 :as z1])
  (:import [com.google.common.collect ImmutableMap]
           [zana.java.functions ArrayLookup]))
;;------------------------------------------------------------------------------
;; preserve return type hints with adding meta data

(defn ^:no-doc with-meta [f ^clojure.lang.IPersistentMap m]
  (cond (instance? clojure.lang.IFn$OD f)
        (zana.java.functions.IFnODWithMeta/wrap ^clojure.lang.IFn$OD f m)
        (instance? clojure.lang.IFn$OL f)
        (zana.java.functions.IFnOLWithMeta/wrap ^clojure.lang.IFn$OL f m)
        (instance? clojure.lang.IFn f)
        (zana.java.functions.IFnWithMeta/wrap ^clojure.lang.IFn f m)
        (instance? clojure.lang.IObj f) (clojure.core/with-meta f m)))

;;------------------------------------------------------------------------------
;; array lookup when in range, default value otherwise
;;------------------------------------------------------------------------------
(defn array-lookup? [f] (instance? ArrayLookup f))
(defn array-lookup
  (^clojure.lang.IFn [values ^String name default]
    (ArrayLookup/make (into-array values) name default))
  (^clojure.lang.IFn [values name]
    (array-lookup values name nil))
  (^clojure.lang.IFn [values]
    (array-lookup values "")))
;;------------------------------------------------------------------------------
(defmethod zfg/domain ArrayLookup [^ArrayLookup f] Long/TYPE)
(defmethod zfg/codomain ArrayLookup [^ArrayLookup f] Object)
(defmethod zfg/support ArrayLookup [^ArrayLookup f] (z1/interval 0 (g/count f)))
;; Does this need to be sorted?
(defmethod zfg/range ArrayLookup [^ArrayLookup f] 
  (guava/sort (seq (.getValues f))))
;;------------------------------------------------------------------------------
(defmethod clojure.core/print-method ArrayLookup [^ArrayLookup f
                                                  ^java.io.Writer w]
  (.write w
    (str "A[" (s/join " " (map pr-str (.getValues f)))
         ";" (print-str (.fallback f)) "]")))
;;------------------------------------------------------------------------------
;; map lookup with default value
;;------------------------------------------------------------------------------
;; TODO: force m to be an ImmutableMap?
(deftype MapLookup [^java.util.Map m 
                    ^String nm
                    ^Object default 
                    ^clojure.lang.IPersistentMap mta]
  clojure.lang.Fn
  clojure.lang.IFn (invoke [this k] (.getOrDefault m k default))
  clojure.lang.Named (getName [this] nm)
  clojure.lang.IObj
  (withMeta [this mta] (MapLookup. m default nm mta))
  (meta [this] mta)
  Object
  (equals [this that]
    (and (instance? MapLookup that)
         (.equals m (.m ^MapLookup that))))
;;         (identical? m (.m ^MapLookup that))))
  (hashCode [this] (System/identityHashCode m))
  (toString [this] (str "[" m " " default "]")))
;  Iterable (iterator [this] (assert false "not an iterator"))
;  java.util.Map (get [this k] (assert false "not a map")))
;;------------------------------------------------------------------------------
(defn map-lookup
  (^clojure.lang.IFn [^java.util.Map m ^String nm default]
    (MapLookup. (ImmutableMap/copyOf m) nm default {}))
  (^clojure.lang.IFn [m nm] (map-lookup m nm nil))
  (^clojure.lang.IFn [m] (map-lookup m "")))
;;------------------------------------------------------------------------------
(defn map-lookup? [f] (instance? MapLookup f))
(defn entries ^java.util.Map [^MapLookup f] (.m f))
;;------------------------------------------------------------------------------
(defn map-index
  (^clojure.lang.IFn$OD [^clojure.lang.IFn k
                         ^clojure.lang.IFn z
                         ^Iterable data
                         ^String nm
                         ^Object default]
    (let [b (java.util.HashMap.)]
      (g/mapc #(.put b (k %) (z %)) data)
      (MapLookup. (java.util.Collections/unmodifiableMap b) nm default {})))
  (^clojure.lang.IFn$OD [k z data] (map-index k z data nil (commons/name z))))
;;------------------------------------------------------------------------------
(defmethod zfg/support MapLookup [^MapLookup f]
  (guava/sort (.keySet ^java.util.Map (.m f))))
;;------------------------------------------------------------------------------
;; object->double map lookup with default NaN
;;------------------------------------------------------------------------------
;; !! mutable if a reference to odm is held elsewhere
(deftype ODMLookup [^com.carrotsearch.hppc.ObjectDoubleHashMap odm
                    ^String nm
                    ^double default
                    ^clojure.lang.IPersistentMap m]
  clojure.lang.Fn
  clojure.lang.IFn 
  (invoke [this k] (.getOrDefault odm k default))
  clojure.lang.IFn$OD 
  (invokePrim [this k] (double (.getOrDefault odm k default)))
  clojure.lang.Named (getName [this] nm)
  clojure.lang.IObj
  (withMeta [this m] (ODMLookup. odm default nm m))
  (meta [this] m)
  Object
  (equals [this that] 
    (and (instance? ODMLookup that)
         (identical? m (.m ^ODMLookup that))))
  (hashCode [this] (System/identityHashCode odm))
  (toString [this] (str "[" (hppc/entries odm) " " default "]")))
;;------------------------------------------------------------------------------
(defmethod zfg/domain ODMLookup [^ODMLookup f] Object)
(defmethod zfg/codomain ODMLookup [^ODMLookup f] Double/TYPE)
(defmethod zfg/support ODMLookup [^ODMLookup f]
  (guava/sort
    (java.util.Arrays/asList
      (.toArray
        (.keys ^com.carrotsearch.hppc.ObjectFloatHashMap (.odm f))))))
(defmethod zfg/range ODMLookup [^ODMLookup f]
  (hppc/range (.values ^com.carrotsearch.hppc.ObjectFloatHashMap (.odm f))))
;;------------------------------------------------------------------------------
(defmethod clojure.core/print-method ODMLookup [^ODMLookup f
                                                ^java.io.Writer w]
  (.write w (str "[" (hppc/entries (.odm f)) "; " (.default f)"]")))
;;------------------------------------------------------------------------------
(defn odm-lookup
  (^clojure.lang.IFn$OD [odm ^String nm default] (ODMLookup. odm nm default {}))
  (^clojure.lang.IFn$OD [odm nm] (odm-lookup odm nm Double/NaN ))
  (^clojure.lang.IFn$OD [odm] (odm-lookup odm "")))
;;------------------------------------------------------------------------------
(defn double-lookup 
  (^clojure.lang.IFn$OD [^clojure.lang.IFn$OD x ^Iterable data]
    (let [m (com.carrotsearch.hppc.ObjectDoubleHashMap. (g/count data))]
      (g/mapc 
        (fn put [datum] (.put m datum (.invokePrim x datum)))
        data)
      (odm-lookup m (str (commons/name x) "-lookup"))))
  (^clojure.lang.IFn$OD [^clojure.lang.IFn$OD x ^Iterable ks ^Iterable data]
    (assert (== (g/count ks) (g/count data)))
    (let [m (com.carrotsearch.hppc.ObjectDoubleHashMap. (g/count data))]
      (g/mapc 
        (fn put [k datum] (.put m k (.invokePrim x datum)))
        ks
        data)
      (odm-lookup m (str (commons/name x) "-lookup")))))
;;------------------------------------------------------------------------------
;; object->long map lookup with default Long/MIN_VALUE
;;------------------------------------------------------------------------------
(deftype OLMLookup [^com.carrotsearch.hppc.ObjectLongHashMap olm
                    ^String nm
                    ^long default
                    ^clojure.lang.IPersistentMap m]
  clojure.lang.Named (getName [this] nm)
  clojure.lang.Fn
  clojure.lang.IFn (invoke [this k] (.getOrDefault olm k default))
  clojure.lang.IFn$OL (invokePrim [this k] (.getOrDefault olm k default))
  zana.functions.inverse.Invertible
  (inverse [this]
    (let [a (object-array (inc (hppc/max-value olm)))]
      (doseq [k (hppc/keys olm)] (aset a (.get olm k) k))
      (array-lookup a)))
  clojure.lang.IObj
  (withMeta [this m] (OLMLookup. olm default nm m))
  (meta [this] m)
  Object
  (equals [this that]
    (and (instance? OLMLookup that)
         (.equals olm (.olm ^OLMLookup that))))
;;         (identical? olm (.olm ^OLMLookup that))))
  (hashCode [this] (System/identityHashCode olm))
  (toString [this] (str "[" (hppc/entries olm) " " default "]")))
;;------------------------------------------------------------------------------
(defmethod zfg/domain OLMLookup [^OLMLookup f] Object)
(defmethod zfg/codomain OLMLookup [^OLMLookup f] Long/TYPE)
(defmethod zfg/support OLMLookup [^OLMLookup f]
  (guava/sort
    (java.util.Arrays/asList
      (.toArray
        (.keys ^com.carrotsearch.hppc.ObjectLongHashMap (.olm f))))))
(defmethod zfg/range OLMLookup [^OLMLookup f]
  (hppc/range (.values ^com.carrotsearch.hppc.ObjectLongHashMap (.olm f))))
;;------------------------------------------------------------------------------
(defmethod clojure.core/print-method OLMLookup [^OLMLookup f
                                                ^java.io.Writer w]
  (.write w (str "[" (hppc/entries (.olm f)) "; " (.default f)"]")))
;;------------------------------------------------------------------------------
(defn olm-lookup
  (^clojure.lang.IFn$OL [olm ^String nm default] (OLMLookup. olm nm default {}))
  (^clojure.lang.IFn$OL [olm nm] (olm-lookup olm nm Long/MIN_VALUE))
  (^clojure.lang.IFn$OL [olm] (olm-lookup olm "")))
;;------------------------------------------------------------------------------
(defn long-lookup 
  (^clojure.lang.IFn$OL [^clojure.lang.IFn$OL x ^Iterable data]
    (let [m (com.carrotsearch.hppc.ObjectLongHashMap. (g/count data))]
      (g/mapc 
        (fn put [datum] (.put m datum (.invokePrim x datum)))
        data)
      (olm-lookup m (commons/name x))))
  (^clojure.lang.IFn$OL [^clojure.lang.IFn$OL x ^Iterable ks ^Iterable data]
    (assert (== (g/count ks) (g/count data)))
    (let [m (com.carrotsearch.hppc.ObjectLongHashMap. (g/count data))]
      (g/mapc 
        (fn put [k datum] (.put m k (.invokePrim x datum)))
        ks
        data)
      (olm-lookup m (commons/name x)))))
;;------------------------------------------------------------------------------
(extend-type ArrayLookup
  zana.functions.inverse/Invertible
  (inverse [this]
    (let [olm (hppc/object-long-map)]
      (doseq [i (range (g/count this))] (.put olm i (this i)))
      (olm-lookup olm (or (commons/name this) "")))))
;;------------------------------------------------------------------------------
#_(defn rank-function [things]
  (let [olm (hppc/object-long-map)]
    (g/mapc (fn [i xi] (.put olm xi (long i))) (range (g/count things)) things)
    (olm-lookup olm "ranks" (long -1))))
;;------------------------------------------------------------------------------
;; object->object identity map lookup with default nil
;; Note: identity map for speed --- may be a premature optimization
;;------------------------------------------------------------------------------
;; !! mutable if a reference to m is held elsewhere
(deftype IdentityMapLookup [^java.util.IdentityHashMap m]
  clojure.lang.IFn (invoke [this k] (.get m k))
  Object
  (equals [this that] (and (instance? IdentityMapLookup that)
                           (identical? m (.m ^IdentityMapLookup that))))
  (hashCode [this] (System/identityHashCode m)))
;;------------------------------------------------------------------------------
(defn- object-lookup 
  (^clojure.lang.IFn$OD [^java.util.IdentityHashMap m]
    (IdentityMapLookup. m))
  (^clojure.lang.IFn$OD [^clojure.lang.IFn$OD x ^Iterable data]
    (let [m (java.util.IdentityHashMap. (g/count data))]
      (g/mapc (fn put [datum] (.put m datum (x datum))) data)
      (object-lookup m)))
  (^clojure.lang.IFn$OD [^clojure.lang.IFn$OD x ^Iterable ks ^Iterable data]
    (let [m (java.util.IdentityHashMap. (g/count data))]
      (g/mapc (fn put [k datum] (.put m k (x datum))) ks data)
      (object-lookup m))))
;;------------------------------------------------------------------------------
(defn lookup-function 
  "<ul>
   <li>Return a function, <code>f</code>, wrapping the map-like object 
   <code>m</code>, such that <code>(f k)</code> == <code>(get m k)</code>.
   </li>
   <li>Create a map where the keys are the elements of <code>data</code>
   and the values are the values of <code>x</code>.
   </li>
   <li>Create a map where the keys are the elements of <code>ks</code>
   and the values are <code>x</code> mapped over <code>data</code>. 
   This strange case is used in permutation importance statistics.
   </li>
   </ul>"
  (^clojure.lang.IFn [m]
    (cond (instance? com.carrotsearch.hppc.ObjectDoubleHashMap m)
          (odm-lookup m)
          (instance? com.carrotsearch.hppc.ObjectLongHashMap m)
          (olm-lookup m)
          (instance? java.util.IdentityHashMap m)
          (object-lookup m)
          :else
          (throw 
            (IllegalArgumentException. 
              (print-str "can't create a lookup function from" (class m))))))
  (^clojure.lang.IFn [^clojure.lang.IFn x ^Iterable data]
    (cond (instance? clojure.lang.IFn$OD x)
          (double-lookup x data)
          (instance? clojure.lang.IFn$OL x)
          (long-lookup x data)
          (instance? clojure.lang.IFn x)
          (object-lookup x data)
          :else ;; redundant now, but we might generalize later
          (throw 
            (IllegalArgumentException. 
              (print-str "can't create a lookup function from" 
                         (class x) (class data))))))
  ;; for taiga.permutation, etc.
  (^clojure.lang.IFn [^clojure.lang.IFn x ^Iterable ks ^Iterable data]
    (cond (instance? clojure.lang.IFn$OD x)
          (double-lookup x ks data)
          (instance? clojure.lang.IFn$OL x)
          (long-lookup x ks data)
          (instance? clojure.lang.IFn x)
          (object-lookup x ks data)
          :else ;; redundant now, but we might generalize later
          (throw 
            (IllegalArgumentException. 
              (print-str "can't create a lookup function from" 
                         (class x) (class ks) (class data)))))))
;;------------------------------------------------------------------------------
(defn dataset-id

  "This is used when taking the union of a collection of datasets, to create an
   attribute function that tells you which dataset each record came from.
   Given a map key->Iterable, where the Iterables are disjoint (no elements in
   common) return a function that maps any element of any of the Iterables to 
   the corresponding key."

  ^clojure.lang.IFn [^java.util.Map k-iterable-map]

  (let [n (reduce-kv (fn [^long n k v] (+ n (g/count v))) 0 k-iterable-map)
        m (java.util.IdentityHashMap. (int n))]
    (doseq [[k ^Iterable v] k-iterable-map]
      (let [it (g/iterator v)]
        (while (g/has-next? it) (.put m (g/next-item it) k))))
    (lookup-function m)))
;;------------------------------------------------------------------------------
;; reduce -> function
;;------------------------------------------------------------------------------
;; TODO: general reduction
(defn sums ^clojure.lang.IFn [^clojure.lang.IFn k
                              ^clojure.lang.IFn v
                              ^Iterable data]
  (let [m (hppc/sums k v data)]
    (cond (instance? com.carrotsearch.hppc.ObjectLongHashMap m)
          (olm-lookup m 0 (str (commons/name v) "-per-" (commons/name k)))
          (instance? com.carrotsearch.hppc.ObjectDoubleHashMap m)
          (odm-lookup m 0.0 (str (commons/name v) "-per-" (commons/name k)))
          :else
          (throw (IllegalArgumentException.
                   (print-str "can't handle: " (class m)))))))
;;------------------------------------------------------------------------------