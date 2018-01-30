(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2018-01-29"
      :doc "Hashmap utilities." }
    
    zana.collections.maps
  
  (:refer-clojure :exclude [assoc get frequencies group-by index 
                            keys map? merge sorted-map vals 
                            zipmap])
  (:require [zana.collections.generic :as g]
             [zana.functions.generic :as fn])
  (:import [com.google.common.collect ArrayListMultimap ImmutableMap  
            ImmutableMultimap ImmutableMultiset Multimap Multimaps Multiset]
           [java.util Collections HashMap Map]))
;;------------------------------------------------------------------------------
;; TODO: Multimaps? Multisets?
(defn map? 
  "True if m is an instance of java.util.Map."
  [m] 
  (instance? java.util.Map m))
;;------------------------------------------------------------------------------
(defmulti ^java.util.Set keys
  "More general version of <code>clojure.core/keys</code>."
  class)

(defmethod keys nil [_] (Collections/emptySet))
(defmethod keys Map [^Map m] (.keySet m))
(defmethod keys Multimap [^Multimap m] (.keySet m))
;;------------------------------------------------------------------------------
(defn vals 
  "More general version of <code>clojure.core/vals</code>."
  ^java.util.Collection [m]
  (cond (instance? Map m) (.values ^java.util.Map m)
        ;; elements of vals are Collections, one for each distinct key
        (instance? Multimap m) (.values (.asMap ^Multimap m))
        :else (throw (IllegalArgumentException. "Can't get values for:" m))))
;;------------------------------------------------------------------------------
#_(defn map-builder ^com.google.common.collect.ImmutableMap$Builder []
    (ImmutableMap/builder))
;;------------------------------------------------------------------------------
(defn index 
  "Like [[group-by]] except the returned map only has one value per key, which
   will be the last element of <code>things</code> encountered that has 
   the particular key = <code>(f thing)</code>."
  ^java.util.Map [f things]
  (assert (ifn? f) (print-str "Not a function:" f))
  (let [b (HashMap.)
        ^java.util.Iterator it (g/iterator things)]
    (while (.hasNext it) (let [x (.next it)] (.put b (f x) x)))
    (Collections/unmodifiableMap b)))
;;------------------------------------------------------------------------------
#_(defn indexf ^clojure.lang.IFn [f things]
    (assert (ifn? f) (print-str "Not a function:" f))
    (let [m (index f things)] (fn [k] (.get m k))))
;;------------------------------------------------------------------------------
(defn assoc 
  
  "Return an immutable map having value <code>v</code> for key <code>k</code>,
   creating a new map only if necessary."
  
  ^java.util.Map [^Map m k v]
  
  (if (= v (.get m k))
    m
    (let [hm (HashMap. (inc (.size m)))]
      (.putAll hm m)
      (.put hm k v)
      (Collections/unmodifiableMap hm))))
;;------------------------------------------------------------------------------
(defn merge 
  "More general, non-persistent, eager version of 
   <code>clojure.core/merge</code>."
  ^java.util.Map [^Map m0 ^Map m1]
  (let [m (HashMap. (max (.size m0) (.size m1)))]
    (.putAll m m0)
    (.putAll m m1)
    (Collections/unmodifiableMap m)))
;;------------------------------------------------------------------------------
;; Because clojure.core/contains? should really be has-key?
(defn ^:no-doc has-key? [m k]
  (if (instance? Map m)
    (.containsKey ^Map m k)
    (clojure.core/contains? m k)))
;;------------------------------------------------------------------------------
(defn zipmap
  "More general, non-persistent, eager version of 
   <code>clojure.core/zipmap</code>, accelerating:
   <ul>
   <li><code>(zipmap (map kf ks) (map vf vs))</code></li>
   <li><code>(zipmap ks (map vf vs))</code></li>
   <li><code>(zipmap ks vs)</code></li>
   </ul>"
  (^java.util.Map [kf ks vf vs]
    "Like (zipmap (map kf ks) (map vf vs))"
    (assert (ifn? kf) (print-str "Not a function:" kf))
    (assert (ifn? vf) (print-str "Not a function:" vf))
    (let [b (HashMap.)
          ik (g/iterator ks)
          iv (g/iterator vs)]
      (while (.hasNext ik)
        (.put b (kf (.next ik)) (vf (.next iv))))
      (assert (not (.hasNext iv)) "More values than keys")
      (Collections/unmodifiableMap b)))
  (^java.util.Map [ks vf vs]
    "Like (zipmap ks (map vf vs))"
    (assert (ifn? vf) (print-str "Not a function:" vf))
    (let [b (HashMap.)
          ik (g/iterator ks)
          iv (g/iterator vs)]
      (while (.hasNext ik)
        (.put b (.next ik) (vf (.next iv))))
      (assert (not (.hasNext iv)) "More values than keys")
      (Collections/unmodifiableMap b)))
  (^java.util.Map [ks vs]
    "Like (zipmap ks vs)"
    (let [b (HashMap.)
          ik (g/iterator ks)
          iv (g/iterator vs)]
      (while (.hasNext ik)
        (.put b (.next ik) (.next iv)))
      (assert (not (.hasNext iv)) "More values than keys")
      (Collections/unmodifiableMap b))))
;;------------------------------------------------------------------------------
;; Grouping
;;------------------------------------------------------------------------------
#_(defn invert
    (^java.util.Map [^Map m ^Multimap mm]
      (.asMap (Multimaps/invertFrom (Multimaps/forMap m) mm)))
    (^java.util.Map [^Map m]
      (invert m (ArrayListMultimap/create (.size m) 2))))
;;------------------------------------------------------------------------------
(defn- finalize-groups ^java.util.Map [^java.util.Map m]
  (let [i (g/iterator (.entrySet m))]
    (while (.hasNext i)
      (let [^java.util.Map$Entry e (.next i)
            ^java.util.List v (.getValue e)]
        (.setValue e (Collections/unmodifiableList v)))))
  (Collections/unmodifiableMap m))
;;------------------------------------------------------------------------------
(defn group-by
  
  "<dl> 
   <dt><code>[z things]</code></dt>
   <dd>Return a map from values of <code>z</code> to a collection of the 
   <code>things</code> that have that value.
   </dd>
   <dt><code>[x y things]</code></dt>
   <dd>Return a map from values of <code>[(x thing) [(y thing)]</code> to a 
   collection of the <code>things</code> that have that value.
   </dd>
   </dl>"
  
  (^java.util.Map [f things]
    (assert (ifn? f) (print-str "Not a function:" f))
    (let [m (java.util.HashMap.)
          i (g/iterator things)]
      (while (.hasNext i)
        (let [v (.next i)
              k (f v)
              ^java.util.List g (.getOrDefault m k (java.util.ArrayList.))]
          (.add g v)
          (.put m k g)))
      (finalize-groups m)))
  
  (^java.util.Map [x y things]
    (assert (ifn? x) (print-str "Not a function:" x))
    (assert (ifn? y) (print-str "Not a function:" y))
    (group-by (fn [thing] [(x thing) (y thing)]) things)))

;;------------------------------------------------------------------------------
;; TODO: dispatch to this from group-by-not-nil, don't expose
(defn group-enums-by-not-nil-random-access
  
  ^java.util.Map [^clojure.lang.IFn z 
                  ^java.util.List things]
  
  #_(assert (ifn? z) (print-str "Not a function:" z))
  #_(assert (fn/enum-valued? z))
  #_(assert (instance? java.util.RandomAccess things))
  
  (let [^clojure.lang.IFn z z
        m (java.util.EnumMap. ^Class (fn/declared-value z))
        n (int (.size things))]
    (dotimes [i n]
      (let [v (.get things i)
            k (z v)]
        (when-not (nil? k)
          (let [^java.util.List g (.get m k)]
            (if g
              (.add g v)
              (let [g (java.util.ArrayList.)]
                (.add g v)
                (.put m k g)))))))
    
    (finalize-groups m)))

(defn- group-by-not-nil-random-access
  
  ^java.util.Map [z ^java.util.List things]
  
  (assert (ifn? z) (print-str "Not a function:" z))
  (assert (instance? java.util.RandomAccess things))
  
  (let [^clojure.lang.IFn z z
        m (java.util.HashMap.)
        n (int (.size things))]
    (dotimes [i n]
      (let [v (.get things i)
            k (z v)]
        (when-not (nil? k)
          (let [^java.util.List g (.get m k)]
            (if g
              (.add g v)
              (let [g (java.util.ArrayList.)]
                (.add g v)
                (.put m k g)))))))
    (finalize-groups m)))

(defn group-by-not-nil
  
  "<dl> 
   <dt><code>[z things]</code></dt>
   <dd>Return a map from values of <code>z</code> to a collection of the 
   <code>things</code> that have that value, droping any <code>thing</code>
   where <code>(z thing)</code> is <code>nil</code>.
   </dd>
   <dt><code>[x y things]</code></dt>
   <dd>Return a map from values of <code>[(x thing) [(y thing)]</code> to a 
   collection of the <code>things</code> that have that value, 
   droping any <code>thing</code> where <code>(x thing)</code> or
   <code>(y thing)</code> is <code>nil</code>.
   </dd>
   </dl>"
  
  (^java.util.Map [z things]
    (assert (ifn? z) (print-str "Not a function:" z))
    (if (instance? java.util.RandomAccess things)
      (group-by-not-nil-random-access z things)
      (let [^clojure.lang.IFn z z
            m (java.util.HashMap.)
            ^java.util.Iterator i (g/iterator things)]
        (while (.hasNext i)
          (let [v (.next i)
                k (.invoke z v)]
            (when-not (nil? k)
              (let [^java.util.List g (.get m k)]
                (if g
                  (.add g v)
                  (let [g (java.util.ArrayList.)]
                    (.add g v)
                    (.put m k g)))))))
        (finalize-groups m))))
  
  (^java.util.Map [x y things]
    (assert (ifn? x) (print-str "Not a function:" x))
    (assert (ifn? y) (print-str "Not a function:" y))
    (group-by-not-nil (fn pair [thing] 
                        (let [xi (x thing)
                              yi (y thing)]
                          (when (and x y) [xi yi]))) 
                      things)))
;;------------------------------------------------------------------------------
;; Maps
;;------------------------------------------------------------------------------
(defn ^:no-doc entry-pairs
  "Return a list of key-value pairs (as opposed to instances of Map.Entry."
  [m]
  (let [^Map m (if (instance? Multimap m) (.asMap ^Multimap m) m)]
    (mapv #(vector % (.get m %)) (.keySet m))))
;;------------------------------------------------------------------------------
(defn ^:no-doc entry-triples
  "Return a list of [key0 key1 value] triples (as opposed to instances of
   Map.Entry. Every key must be a pair, or an exception is thrown."
  [m]
  (let [^Map m (if (instance? Multimap m) (.asMap ^Multimap m) m)]
    (mapv (fn [k]
            (assert (and (vector? k) (== 2 (count k))))
            (conj k (.get m k)))
          (.keySet m))))
;;------------------------------------------------------------------------------
;; Multisets (frequencies)
;; TODO: replace with ObjectIntMap, and ObjectIntMap -> java.util.Map at end.
;;------------------------------------------------------------------------------
#_(defn frequencies
    (^com.google.common.collect.Multiset [^Iterable things]
      (ImmutableMultiset/copyOf things))
    (^com.google.common.collect.Multiset [f things]
      (assert (ifn? f) (print-str "Not a function:" f))
      (let [b (ImmutableMultiset/builder)
            it (g/iterator things)]
        (while (.hasNext it) (.add b (f (.next it))))
        (.build b))))
(defn frequencies
  "Return a map from the distinct elements of <code>things</code>, or the 
   distinct values of <code>(f thing)</code> to the count of such elements."
  (^java.util.Map [things]
    (let [m (java.util.HashMap.)
          it (g/iterator things)]
      (while (.hasNext it)
        (let [k (.next it)
              v (if (.containsKey m k) (inc (int (.get m k))) 1)]
          (.put m k v)))
      (Collections/unmodifiableMap m)))
  (^java.util.Map [f things]
    (assert (ifn? f) (print-str "Not a function:" f))
    (let [m (java.util.HashMap.)
          it (g/iterator things)]
      (while (.hasNext it)
        (let [k (f (.next it))
              v (if (.containsKey m k) (inc (int (.get m k))) 1)]
          (.put m k v)))
      (Collections/unmodifiableMap m)))
  (^java.util.Map [x y things]
    (assert (ifn? x) (print-str "Not a function:" x))
    (assert (ifn? y) (print-str "Not a function:" y))
    (let [m (java.util.HashMap.)
          it (g/iterator things)]
      (while (.hasNext it)
        (let [nxt (.next it)
              k [(x nxt) (y nxt)]
              v (if (.containsKey m k) (inc (int (.get m k))) 1)]
          (.put m k v)))
      (Collections/unmodifiableMap m))))
;;------------------------------------------------------------------------------
(defn- sorted-map [^java.util.Map m]
  (if-not (empty? m)
    (Collections/unmodifiableMap (java.util.TreeMap. m))
    (Collections/emptySortedMap)))
;;------------------------------------------------------------------------------
(defn frequencies-sorted-by-key
  "Return a map from the distinct elements of <code>things</code>, or the 
   distinct values of <code>(f thing)</code> to the count of such elements."
  (^java.util.Map [things] (sorted-map (frequencies things)))
  (^java.util.Map [f things] (sorted-map (frequencies f things)))
  (^java.util.Map [x y things] (sorted-map (frequencies x y things))))
;;------------------------------------------------------------------------------
(defmethod clojure.core/print-method 
  java.util.Map
  [^java.util.Map counts ^java.io.Writer w]
  (.write w "{")
  (let [it (g/iterator counts)
        print-kv (fn print-kv [[k v]]
                   (clojure.core/print-method k w)
                   (.write w " ")
                   (clojure.core/print-method v w))]
    (when (.hasNext it) (print-kv (.next it)))
    (g/mapc #(do (.write w ", ") (print-kv %)) it))
  (.write w "}"))
;;------------------------------------------------------------------------------
(defmethod clojure.core/print-method 
  com.google.common.collect.Multiset
  [^com.google.common.collect.Multiset counts ^java.io.Writer w]
  (.write w "{")
  (let [it (g/iterator (.elementSet counts))
        print-item-count (fn print-item-count [item ]
                           (clojure.core/print-method item w)
                           (.write w " ")
                           (.write w (str (.count counts item))))]
    (when (.hasNext it) (print-item-count (.next it)))
    (g/mapc #(do (.write w ", ") (print-item-count %)) it))
  (.write w "}"))
;;----------------------------------------------------------------
;; TODO: Returns double[] instances. Not safe.

(defn simplex-coordinates-map 
  "AKA one-hot encoding.
   
   Return a function that maps each of <code>n</code> distinct 
   values of a categorical <code>attribute</code> to the linear 
   (vector) coordinates of a corresponding corner of the unit 
   <code>n</code> simplex in <b>R</b><sup>n-1</sup>. 
   The coordinates are returned as <code>double[n-1]</code>.
   Only the values present in <code>data</code> are explicitly
   mapped. The most frequent value is mapped to the origin
   (a <code>double[n-1]</code> whose elements are all 
   <code>0.0</code>). The remaining values from <code>data</code>
   are mapped in order of decreasing frequency. 
   
   Any value not occuring in <code>data</code> will be mapped to 
   the origin, so that predictive models using this encoding will 
   treat previously unseen values as though they were the most 
   most common value, a simple form of imputation."
  
  (^clojure.lang.IFn [^clojure.lang.IFn attribute 
                      ^java.util.Collection data]
  
  ;; should not be used with numerical attributes
  ;; TODO: check for too many distinct values?
  (assert 
    (not 
      (or 
        (instance? clojure.lang.IFn$OD attribute)
        (instance? clojure.lang.IFn$OL attribute)
        (instance? java.time.temporal.TemporalAccessor attribute))))
  (let [f (frequencies attribute data)
        f (clojure.core/rest (clojure.core/sort-by #(- (val %)) f))
        n (clojure.core/count f)
        origin (double-array n)
        corner (fn corner ^doubles [^long i]
                 (let [c (double-array n)]
                   (aset-double c i 1.0)
                   c))
        f (clojure.core/into 
            {} 
            (clojure.core/map-indexed 
              (fn [[k v] i] [k (corner i)]) 
              f))]
    (fn simplex-coordinates ^doubles [k] 
      (clojure.core/get f k origin)))))
;;----------------------------------------------------------------

