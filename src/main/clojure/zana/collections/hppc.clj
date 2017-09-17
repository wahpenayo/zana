(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-09-09"
      :doc "Utilities for HPPC:
            High Performance Primitive Collections for Java." }
    
    zana.collections.hppc
  
  (:refer-clojure :exclude [double-array empty? filter frequencies get inc keys 
                            range reduce])
  (:require [zana.collections.generic :as g]
            [zana.collections.maps :as maps]
            [zana.collections.sets :as sets])
  (:import [clojure.lang IFn IFn$OD IFn$OL]
           [com.carrotsearch.hppc DoubleContainer FloatContainer LongContainer
            LongObjectHashMap ObjectContainer ObjectDoubleHashMap
            ObjectFloatHashMap ObjectIntHashMap ObjectLongHashMap ObjectLongMap]
           [com.carrotsearch.hppc.cursors DoubleCursor FloatCursor LongCursor
            ObjectCursor ObjectDoubleCursor ObjectFloatCursor ObjectIntCursor
            ObjectLongCursor]
           [com.google.common.collect ImmutableList ImmutableSet]
           [java.util Arrays Collection Collections Map Map$Entry Set]))
;;------------------------------------------------------------------------------
;; TODO: replace defmulti with defprotocol?
;;------------------------------------------------------------------------------
(defmethod maps/keys ObjectIntHashMap [^ObjectIntHashMap m]
  (into [] (.toArray (.keys m))))
(defmethod maps/keys ObjectLongHashMap [^ObjectLongHashMap m]
  (into [] (.toArray (.keys m))))
(defmethod maps/keys ObjectDoubleHashMap [^ObjectDoubleHashMap m]
  (into [] (.toArray (.keys m))))
(defmethod maps/keys ObjectFloatHashMap [^ObjectFloatHashMap m]
  (into [] (.toArray (.keys m))))
;;------------------------------------------------------------------------------
(defmethod g/empty? ObjectIntHashMap [^ObjectIntHashMap m] (< 0 (.size m)))
(defmethod g/empty? ObjectLongHashMap [^ObjectLongHashMap m] (< 0 (.size m)))
(defmethod g/empty? ObjectDoubleHashMap [^ObjectDoubleHashMap m] (< 0 (.size m)))
(defmethod g/empty? ObjectFloatHashMap [^ObjectFloatHashMap m] (< 0 (.size m)))
;;------------------------------------------------------------------------------
(defmulti entries class)
;; TODO: return a vector? sort? method macro?
(defmethod entries ObjectIntHashMap [^ObjectIntHashMap olm]
  (let [i (.iterator olm)]
    (loop [entries (transient {})]
      (if (.hasNext i)
        (let [^ObjectIntCursor c (.next i)]
          (recur (assoc! entries (.key c) (.value c))))
        (persistent! entries)))))
(defmethod entries ObjectLongHashMap [^ObjectLongHashMap olm]
  (let [i (.iterator olm)]
    (loop [entries (transient {})]
      (if (.hasNext i)
        (let [^ObjectLongCursor c (.next i)]
          (recur (assoc! entries (.key c) (.value c))))
        (persistent! entries)))))
(defmethod entries ObjectDoubleHashMap [^ObjectDoubleHashMap olm]
  (let [i (.iterator olm)]
    (loop [entries (transient {})]
      (if (.hasNext i)
        (let [^ObjectDoubleCursor c (.next i)]
          (recur (assoc! entries (.key c) (.value c))))
        (persistent! entries)))))
(defmethod entries ObjectFloatHashMap [^ObjectFloatHashMap olm]
  (let [i (.iterator olm)]
    (loop [entries (transient {})]
      (if (.hasNext i)
        (let [^ObjectFloatCursor c (.next i)]
          (recur (assoc! entries (.key c) (.value c))))
        (persistent! entries)))))
;;------------------------------------------------------------------------------
(defmulti ^Long/TYPE max-value class)
(defmethod max-value LongContainer ^long [^LongContainer container]
  (assert (> (.size container) 0))
  (let [i (.iterator container)]
    (loop [l1 Long/MIN_VALUE]
      (if (.hasNext i)
        (let [l (.value ^LongCursor (.next i))
              l1 (if (> l l1) l l1)]
          (recur l1))
        l1))))
(defmethod max-value ObjectLongMap ^long [^ObjectLongMap olm]
  (max-value (.values olm)))
;;------------------------------------------------------------------------------
;; not quite the same as values --- return intervals for numerical containers.
;; TODO: replace with 'span' of values
(defmulti range class)
(defmethod range LongContainer [^LongContainer container]
  (assert (> (.size container) 0))
  (let [i (.iterator container)]
    (loop [l0 Long/MAX_VALUE
           l1 Long/MIN_VALUE]
      (if (.hasNext i)
        (let [l (.value ^LongCursor (.next i))
              l0 (if (< l l0) l l0)
              l1 (if (> l l1) l l1)]
          (recur l0 l1))
        [l0 (+ 1 l1)]))))
(defmethod range DoubleContainer [^DoubleContainer container]
  (assert (> (.size container) 0))
  (let [i (.iterator container)]
    (loop [d0 Double/POSITIVE_INFINITY
           d1 Double/NEGATIVE_INFINITY]
      (if (.hasNext i)
        (let [d (.value ^DoubleCursor (.next i))
              d0 (if (< d d0) d d0)
              d1 (if (> d d1) d d1)]
          (recur d0 d1))
        [d0 d1]))))
(defmethod range FloatContainer [^FloatContainer container]
  (assert (> (.size container) 0))
  (let [i (.iterator container)]
    (loop [d0 Float/POSITIVE_INFINITY
           d1 Float/NEGATIVE_INFINITY]
      (if (.hasNext i)
        (let [d (float (.value ^FloatCursor (.next i)))
              d0 (float (if (< d d0) d d0))
              d1 (float (if (> d d1) d d1))]
          (recur d0 d1))
        [d0 d1]))))
;;------------------------------------------------------------------------------
;; TODO: more factories
(defn object-double-map
  (^com.carrotsearch.hppc.ObjectDoubleHashMap [] (ObjectDoubleHashMap.))
  (^com.carrotsearch.hppc.ObjectDoubleHashMap [^Map m]
    (let [odohm (ObjectDoubleHashMap. (.size m))
          entries (.iterator (.entrySet m))]
      (while (.hasNext entries)
        (let [^Map$Entry entry (.next entries)]
          (.put odohm (.getKey entry) (double (.getValue entry)))))
      odohm)))
(defn object-double-map? [x] (instance? ObjectDoubleHashMap x))
;;------------------------------------------------------------------------------
(defn object-float-map
  (^com.carrotsearch.hppc.ObjectFloatHashMap [] (ObjectFloatHashMap.))
  (^com.carrotsearch.hppc.ObjectFloatHashMap [x]
    (if-not (instance? java.util.Map x)
      (ObjectFloatHashMap. (int x))
      (let [^java.util.Map m x
            odohm (ObjectFloatHashMap. (.size m))
            entries (.iterator (.entrySet m))]
        (while (.hasNext entries)
          (let [^Map$Entry entry (.next entries)]
            (.put odohm (.getKey entry) (float (.getValue entry)))))
        odohm))))
(defn object-float-map? [x] (instance? ObjectFloatHashMap x))
;;------------------------------------------------------------------------------
(defn object-long-map
  (^com.carrotsearch.hppc.ObjectLongHashMap [] (ObjectLongHashMap.))
  (^com.carrotsearch.hppc.ObjectLongHashMap [^Map m]
    (let [mm (ObjectLongHashMap. (.size m))
          entries (.iterator (.entrySet m))]
      (while (.hasNext entries)
        (let [^Map$Entry entry (.next entries)]
          (.put mm (.getKey entry) (long (.getValue entry)))))
      mm)))
(defn object-long-map? [x] (instance? ObjectLongHashMap x))
;;------------------------------------------------------------------------------
(defn long-object-map
  (^com.carrotsearch.hppc.LongObjectHashMap [] (LongObjectHashMap.))
  (^com.carrotsearch.hppc.LongObjectHashMap [^Map m]
    (let [mm (LongObjectHashMap. (.size m))
          entries (.iterator (.entrySet m))]
      (while (.hasNext entries)
        (let [^Map$Entry entry (.next entries)]
          (.put mm (long (.getKey entry)) (.getValue entry))))
      mm)))
(defn long-object-map? [x] (instance? LongObjectHashMap x))
;;------------------------------------------------------------------------------
;; TODO: multimethods?
(defn get
  (^double [^ObjectDoubleHashMap m k ^double default]
    (.getOrDefault m k default))
  (^double [^ObjectDoubleHashMap m k]
    (get m k Double/NaN)))
(defn put ^double [^ObjectDoubleHashMap m k ^double v] (.put m k v))
(defn add [^ObjectDoubleHashMap m k ^double v] (.addTo m k v))
(defn inc [m k]
  (cond (instance? ObjectDoubleHashMap m)
        (.addTo ^ObjectDoubleHashMap m k (double 1.0))
        (instance? ObjectLongHashMap m)
        (.addTo ^ObjectLongHashMap m k (long 1))
        :else
        (throw (UnsupportedOperationException.))))
;;------------------------------------------------------------------------------
(defn key-array ^objects [m]
  (cond (instance? ObjectDoubleHashMap m)
        (.toArray (.keys ^ObjectDoubleHashMap m))
        (instance? ObjectFloatHashMap m)
        (.toArray (.keys ^ObjectFloatHashMap m))
        (instance? ObjectIntHashMap m)
        (.toArray (.keys ^ObjectIntHashMap m))
        :else
        (throw
          (UnsupportedOperationException. (str "Can't get keys for " m)))))
(defn keys ^Iterable [m]
  (cond (instance? ObjectDoubleHashMap m)
        (Collections/unmodifiableList
          (Arrays/asList
            (.toArray (.keys ^ObjectDoubleHashMap m))))
        (instance? ObjectFloatHashMap m)
        (Collections/unmodifiableList
          (Arrays/asList
            (.toArray (.keys ^ObjectFloatHashMap m))))
        (instance? ObjectIntHashMap m)
        (Collections/unmodifiableList
          (Arrays/asList
            (.toArray (.keys ^ObjectIntHashMap m))))
        (instance? ObjectLongHashMap m)
        (Collections/unmodifiableList
          (Arrays/asList
            (.toArray (.keys ^ObjectLongHashMap m))))
        :else
        (throw
          (UnsupportedOperationException. (str "Can't get keys for " m)))))
;;------------------------------------------------------------------------------
(defmethod g/filter [IFn ObjectIntHashMap] [^IFn f ^ObjectIntHashMap m]
  (let [i (.iterator m)
        mm (ObjectIntHashMap. (.size m))]
    (while (.hasNext i)
      (let [^ObjectIntCursor c (.next i)
            k (.key c)
            v (.value c)]
        (when (f k v) (.put mm k v))))
    mm))

(defmethod g/filter [IFn ObjectDoubleHashMap] [^IFn f ^ObjectDoubleHashMap m]
  (let [i (.iterator m)
        mm (ObjectDoubleHashMap. (.size m))]
    (while (.hasNext i)
      (let [^ObjectDoubleCursor c (.next i)
            k (.key c)
            v (.value c)]
        (when (f k v) (.put mm k v))))
    mm))
;;------------------------------------------------------------------------------
;; TODO: multimethods?
;; TODO: automated method generation?
(defn reduceObjectIntHashMap [f x0 ^ObjectIntHashMap m]
  (let [i (.iterator m)]
    (loop [x x0]
      (if (.hasNext i)
        (let [^ObjectIntCursor oic (.next i)
              k (.key oic)
              v (.value oic)]
          (recur (f x k v)))
        x))))
(defn reduceObjectLongHashMap [f x0 ^ObjectLongHashMap m]
  (let [i (.iterator m)]
    (loop [x x0]
      (if (.hasNext i)
        (let [^ObjectLongCursor oic (.next i)
              k (.key oic)
              v (.value oic)]
          (recur (f x k v)))
        x))))
(defn reduceObjectDoubleHashMap [f x0 ^ObjectDoubleHashMap m]
  (let [i (.iterator m)]
    (loop [x x0]
      (if (.hasNext i)
        (let [^ObjectDoubleCursor oic (.next i)
              k (.key oic)
              v (.value oic)]
          (recur (f x k v)))
        x))))
(defn reduce [f init m]
  (cond (instance? ObjectIntHashMap m)
        (reduceObjectIntHashMap f init m)
        (instance? ObjectLongHashMap m)
        (reduceObjectLongHashMap f init m)
        (instance? ObjectDoubleHashMap m)
        (reduceObjectDoubleHashMap f init m)
        :else (throw
                (IllegalArgumentException.
                  (str "Don't know how to reduce a " (class m))))))
;;------------------------------------------------------------------------------
#_(defn frequencies ^com.carrotsearch.hppc.ObjectIntHashMap
                  ([^Iterable data]
                    (let [m (ObjectIntHashMap.)
                          it (.iterator data)]
                      (while (.hasNext it) (.addTo m (.next it) (int 1)))
                      m))
  ([^IFn f ^Iterable data]
    (let [m (ObjectIntHashMap.)
          it (.iterator data)]
      (while (.hasNext it) (.addTo m (f (.next it)) (int 1)))
      m))
  ([^IFn x ^IFn y ^Iterable data]
    (let [m (ObjectIntHashMap.)
          it (.iterator data)]
      (while (.hasNext it)
        (let [di (.next it)
              xy [(x di) (y di)]]
          (.addTo m xy (int 1))))
      m)))
;;------------------------------------------------------------------------------
;; TODO: multimethods? protocol?
(defn mode [^ObjectLongHashMap m]
  (let [[k _] (reduce
                (fn [[k ^long v] ki ^long vi] (if (> vi v) [ki vi] [k v]))
                [nil -1]
                m)]
    k))
;;------------------------------------------------------------------------------
(defn- sums-double ^com.carrotsearch.hppc.ObjectDoubleHashMap [^IFn k
                                                               ^IFn$OD v
                                                               ^Iterable data]
  (let [m (ObjectDoubleHashMap.)
        it (.iterator data)]
    (while (.hasNext it)
      (let [x (.next it)]
        (.addTo m (k x) (v x))))
    m))
(defn- sums-long ^com.carrotsearch.hppc.ObjectLongHashMap [^IFn k
                                                           ^IFn$OL v
                                                           ^Iterable data]
  (let [m (ObjectLongHashMap.)
        it (.iterator data)]
    (while (.hasNext it)
      (let [x (.next it)]
        (.addTo m (k x) (v x))))
    m))
(defn- sums-generic ^com.carrotsearch.hppc.ObjectDoubleHashMap [^IFn k
                                                                ^IFn v
                                                                ^Iterable data]
  (let [m (ObjectDoubleHashMap.)
        it (.iterator data)]
    (while (.hasNext it)
      (let [x (.next it)]
        (.addTo m (k x) (v x))))
    m))
(defn sums [^IFn k ^IFn v ^Iterable data]
  (cond (instance? IFn$OD v) (sums-double k v data)
        (instance? IFn$OL v) (sums-long k v data)
        :else (throw
                (IllegalArgumentException.
                  (print-str "can't handle:" v)))))
;;------------------------------------------------------------------------------
;; Sparse vectors
;;------------------------------------------------------------------------------
;; could use a DoubleArrayList
(defn double-array ^doubles [^ObjectDoubleHashMap odohm keys]
  (cond (instance? Collection keys)
        (let [^Collection keys keys
              n (.size keys)
              it (.iterator keys)
              a (clojure.core/double-array n)]
          (dotimes [i (int n)]
            (let [index (.indexOf odohm (.next it))]
              (when-not (neg? index) (aset a i (.indexGet odohm index)))))
          a)
        (.isArray (class keys))
        (let [^objects keys keys
              n (alength keys)
              a (clojure.core/double-array n)]
          (dotimes [i (int n)]
            (let [index (.indexOf odohm (aget keys i))]
              (when-not (neg? index) (aset a i (.indexGet odohm index)))))
          a)
        :else
        (throw
          (UnsupportedOperationException.
            (str "Can't evaluate map at keys in " keys)))))
;; Treating the maps as sparse representations of vectors where the keys
;; correspond to the canonical basis vectors of a vector space whose dimensions
;; correspond to any java Object (equivalence classes of objects under .equal).
(defn inner-product ^double [^ObjectDoubleHashMap m0
                             ^ObjectDoubleHashMap m1]
  ;; iterate over the sparsest vector
  (if (> (.size m0) (.size m1))
    (inner-product m1 m0)
    (let [i (.iterator m0)]
      (loop [sum (double 0.0)]
        (if (.hasNext i)
          (let [^ObjectDoubleCursor oic (.next i)
                index (.indexGet m1 (.key oic))]
            (if-not (neg? index)
              (recur (+ sum (* (.value oic) (.indexGet m1 index))))
              (recur sum)))
          sum)))))
;;------------------------------------------------------------------------------
;; Treating the maps as sparse representations of vectors where the keys
;; correspond to the canonical basis vectors of a vector space whose dimensions
;; correspond to any java Object (equivalence classes of objects under .equal).
(defn add-values
  ^com.carrotsearch.hppc.ObjectDoubleHashMap [^ObjectDoubleHashMap m0
                                              ^ObjectDoubleHashMap m1]
  (let [sum (ObjectDoubleHashMap.)
        i (.iterator (sets/union (keys m0) (keys m1)))]
    (while (.hasNext i)
      (let [k (.next i)
            i0 (.indexOf m0 k)
            i1 (.indexOf m1 k)]
        (if-not (neg? i0)
          (if-not (neg? i1)
            (.put sum k (+ (.indexGet m0 i0) (.indexGet m1 i1)))
            (.put sum k (.indexGet m0 i0)))
          (.put sum k (.indexGet m1 i1)))))
    sum))
;;------------------------------------------------------------------------------
(defmethod clojure.core/print-method ObjectLongHashMap [^ObjectLongHashMap m
                                                        ^java.io.Writer w]
  (.write w (pr-str (entries m))))
;;------------------------------------------------------------------------------