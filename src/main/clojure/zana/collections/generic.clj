(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2018-02-09"
      :doc 
      "Generic versions of clojure functions.
       Some implemented with defmulti, some with defprotocol, 
       some by hand.
       <br>
       Naming convention: 
       <dl>
       <dt>foo</dt>
       <dd>arg is not modified and value is immutable.</dd>
       <dt>foo!</dt>
       <dd>arg is mutable and (may be) modified, return value 
       may be void/nil.</dd>
       <dl>!foo</dl>
       <dd>arg is not modified, return value is mutable.</dd>
       </dl>" }
    
    zana.collections.generic
  
  (:refer-clojure 
    :exclude [compare concat count doall drop empty? every? filter 
              first get last list map map-indexed mapcat next nth 
              partition pmap remove repeatedly second shuffle some
              sort sort-by split-with take])
  
  (:require [zana.commons.core :as cc])
  
  (:import [java.util ArrayList Arrays Collection Collections 
            HashMap IdentityHashMap Iterator List Map Map$Entry 
            NoSuchElementException RandomAccess Set]
           [java.util.concurrent 
            Executors Future ThreadLocalRandom]
           [clojure.lang Counted IFn IPersistentMap 
            IPersistentCollection Seqable Sequential]
           [com.google.common.base Function Predicate]
           [com.google.common.collect 
            ImmutableList ImmutableTable Iterables Iterators 
            Multimap Multiset Ordering Sets Table Table$Cell]
           [com.google.common.primitives 
            Booleans Bytes Chars Doubles Floats Ints Longs 
            Shorts]))
;;----------------------------------------------------------------
;; Accessing elements
;;----------------------------------------------------------------
;; Iterators
;;----------------------------------------------------------------
;; these need to be fast, so not generic
;; for custom iteration, implement Iterable and Iterator for the 
;; data structure.
;;----------------------------------------------------------------
(defn has-next? 
  "Function wrapper for <code>Iterator.hasNext()</code>."
  [^Iterator i] 
  (and i (.hasNext i)))
;;----------------------------------------------------------------
;; Avoid confusion with clojure.core/next (which is a terrible 
;; choice of name for an alternative to rest...)
(defn next-item 
  "Function wrapper for <code>Iterator.next()</code>."
  [^Iterator i] 
  (if-not (nil? i) 
    (.next i)
    (throw (NoSuchElementException. "nil iterator!"))))
;;----------------------------------------------------------------
;; TODO: defmulti? handle indexed collections?
(defn get
  "Like 
   <a href=\"http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/get\">
   clojure.core/get</a> but for more kinds of <code>things</code>."
  ([things k default]
    (assert (not (nil? things)))
    (cond (instance? Map things) 
          (.getOrDefault ^Map things k default)
          (instance? Multimap things) 
          (get (.asMap ^Multimap things) k default)
          :else 
          (clojure.core/get things k default)))
  ([things k]
    (assert (not (nil? things)))
    (cond (instance? Multiset things) 
          (.count ^Multiset things k)
          
          :else 
          (get things k nil))))
;;----------------------------------------------------------------
;; asList -> asImmutableList
;;----------------------------------------------------------------
(defmulti ^java.util.Iterator iterator 
  "Construct an Iterator over things."
  {:arglists '([things])}
  class)

(defmethod iterator Iterator  [^Iterator things]
  things)
(defmethod iterator Iterable [^Iterable things] 
  (.iterator things))
(defmethod iterator nil [_] 
  (Collections/emptyIterator))
(defmethod iterator Map [^Map things] 
  (iterator (.entrySet things)))
(prefer-method iterator Map Iterable)
(defmethod iterator Multimap [^Multimap things] 
  (iterator (.asMap things)))
(defmethod iterator Table [^Table things] 
  (iterator (.cellSet things)))

;; TODO: iterators that return unboxed double and long primitives (and String?)
(let [aclass (class (boolean-array 0))]
  (defmethod iterator aclass [^booleans things] 
    (iterator (Booleans/asList things))))
(let [aclass (class (byte-array 0))] 
  (defmethod iterator aclass [^bytes things] 
    (iterator (Bytes/asList things))))
(let [aclass (class (char-array 0))]
  (defmethod iterator aclass [^chars things] 
    (iterator (Chars/asList things))))
(let [aclass (class (double-array 0))]
  (defmethod iterator aclass [^doubles things] 
    (iterator (Doubles/asList things))))
(let [aclass (class (float-array 0))]
  (defmethod iterator aclass [^floats things] 
    (iterator (Floats/asList things))))
(let [aclass (class (long-array 0))]
  (defmethod iterator aclass [^longs things] 
    (iterator (Longs/asList things))))
(let [aclass (class (short-array 0))]
  (defmethod iterator aclass [^shorts things] 
    (iterator (Shorts/asList things))))
(let [aclass (class (object-array 0))]
  (defmethod iterator aclass [^objects things] 
    (iterator (Arrays/asList things))))
;;----------------------------------------------------------------
;; TODO: no iterator implementations' either defmulti of hand dispatch
(defn first 
  "Return the first element of <code>things</code> in the natural ordering
   of its elements."
  [things] 
  (cond (nil? things)
        nil
        
        (or (seq? things) (coll? things) 
            (counted? things) (sequential? things) 
            (list? things) (map? things)) 
        (clojure.core/first things)
        
        (instance? List things) 
        (when (< 0 (.size ^List things)) 
          (.get ^List things 0))
        
        (instance? String things)
        (when (< 0 (.length ^String things))
          (.charAt ^String things 0))
        
        (cc/double-array? things) 
        (when (< 0 (alength ^doubles things))
          (aget ^doubles things 0))
        (cc/object-array? things)
        (when (< 0 (alength ^objects things))
          (aget ^objects things 0))
        
        (cc/long-array? things) 
        (when (< 0 (alength ^longs things))
          (aget ^longs things 0))
        
        (cc/int-array? things) 
        (when (< 0 (alength ^ints things))
          (aget ^ints things 0))
        
        (cc/float-array? things)
        (when (< 0 (alength ^floats things))
          (aget ^floats things 0))
        
        (cc/char-array? things) 
        (when (< 0 (alength ^chars things))
          (aget ^chars things 0))
        
        (cc/byte-array? things) 
        (when (< 0 (alength ^bytes things))
          (aget ^bytes things 0))
        
        (cc/short-array? things) 
        (when (< 0 (alength ^shorts things))
          (aget ^shorts things 0))
        
        (instance? Multimap things) 
        (first (.asMap ^Multimap things))
        
        :else  
        (let [it (iterator things)] 
          (when (.hasNext it) (.next it)))))
;;----------------------------------------------------------------
(defn second
  "Return the second element of <code>things</code> in the natural
   ordering of its elements."
  [things] 
  (cond (nil? things) 
        nil
        
        (or (seq? things) (coll? things) 
            (counted? things) (sequential? things) 
            (list? things) (map? things)) 
        (clojure.core/second things) 
        
        (instance? List things) 
        (when (< 1 (.size ^List things)) 
          (.get ^List things 1))
        
        (instance? String things) 
        (when (< 1 (.length ^String things))
          (.charAt ^String things 1))
        
        (cc/double-array? things) 
        (when (< 1 (alength ^doubles things))
          (aget ^doubles things 1))
        
        (cc/object-array? things) 
        (when (< 1 (alength ^objects things))
          (aget ^objects things 1))
        
        (cc/long-array? things) 
        (when (< 1 (alength ^longs things))
          (aget ^longs things 1))
        
        (cc/int-array? things) 
        (when (< 1 (alength ^ints things))
          (aget ^ints things 1))
        
        (cc/float-array? things) 
        (when (< 1 (alength ^floats things))
          (aget ^floats things 1))
        
        (cc/char-array? things) 
        (when (< 1 (alength ^chars things))
          (aget ^chars things 1))
        
        (cc/byte-array? things) 
        (when (< 1 (alength ^bytes things))
          (aget ^bytes things 1))
        
        (cc/short-array? things) 
        (when (< 1 (alength ^shorts things))
          (aget ^shorts things 1))
        
        (instance? Multimap things) 
        (second (.asMap ^Multimap things))
        
        :else  
        (let [it (iterator things)]
          (when (.hasNext it) 
            (.next it)
            (when (.hasNext it) 
              (.next it))))))
;;----------------------------------------------------------------
(defn last
  "Return the last element of <code>things</code> in the natural 
   ordering of its elements."
  [things] 
  (cond (nil? things) 
        nil
        
        (or (seq? things) (coll? things) 
            (counted? things) (sequential? things) 
            (list? things) (map? things)) 
        (clojure.core/last things) 
        
        (instance? List things) 
        (let [n (- (.size ^List things) 1)] 
          (when (<= 0 n) (.get ^List things n)))
        
        (instance? String things) 
        (let [n (- (.length ^String things) 1)] 
          (when (<= 0 n) (.charAt ^String things n)))
        
        (cc/double-array? things) 
        (let [n (- (alength ^doubles things) 1)] 
          (when (<= 0 n) (aget ^doubles things n)))
        
        (cc/object-array? things) 
        (let [n (- (alength ^objects things) 1)] 
          (when (<= 0 n) (aget ^objects things n)))
        
        (cc/long-array? things) 
        (let [n (- (alength ^longs things) 1)] 
          (when (<= 0 n) (aget ^longs things n)))
        
        (cc/int-array? things) 
        (let [n (- (alength ^ints things) 1)] 
          (when (<= 0 n) (aget ^ints things n)))
        
        (cc/float-array? things) 
        (let [n (- (alength ^floats things) 1)] 
          (when (<= 0 n) (aget ^floats things n)))
        
        (cc/char-array? things) 
        (let [n (- (alength ^chars things) 1)] 
          (when (<= 0 n) (aget ^chars things n)))
        
        (cc/byte-array? things) 
        (let [n (- (alength ^bytes things) 1)] 
          (when (<= 0 n) (aget ^bytes things n)))
        
        (cc/short-array? things) 
        (let [n (- (alength ^shorts things) 1)] 
          (when (<= 0 n) (aget ^shorts things n)))
        
        (instance? Multimap things)
        (last (.asMap ^Multimap things))
        
        :else  
        (let [it (iterator things)] 
          (loop [v nil]
            (if-not (.hasNext it) 
              v
              (recur (.next it)))))))
;;----------------------------------------------------------------
(defmulti ^Long/TYPE generic-count
  "How many things? How many in (filter f things)?"
  {:arglists '([things] [f things])}
  (fn count-dispatch
    ([things] (class things))
    ([f things] [(class f) (class things)])))

(defmethod generic-count Iterator [^Iterator things] 
  (long (Iterators/size things)))
(defmethod generic-count Iterable [^Iterable things] 
  (long (Iterables/size things)))
(defmethod generic-count Collection [^Collection things] 
  (long (.size things)))
(prefer-method generic-count Collection Iterable)
(defmethod generic-count Map [^Map things] 
  (long (.size things)))
(prefer-method generic-count Map Iterable)
(defmethod generic-count Multimap [^Multimap things] 
  (generic-count (.asMap things)))
(defmethod generic-count nil [_] (long 0))
(defmethod generic-count String [^String things] 
  (long (.length things)))

(let [aclass (class (boolean-array 0))]
  (defmethod generic-count aclass [^booleans things] 
    (long (alength things))))
(let [aclass (class (byte-array 0))]
  (defmethod generic-count aclass [^bytes things] 
    (long (alength things))))
(let [aclass (class (char-array 0))]
  (defmethod generic-count aclass [^chars things] 
    (long (alength things))))
(let [aclass (class (double-array 0))]
  (defmethod generic-count aclass [^doubles things] 
    (long (alength things))))
(let [aclass (class (float-array 0))]
  (defmethod generic-count aclass [^floats things] 
    (long (alength things))))
(let [aclass (class (long-array 0))]
  (defmethod generic-count aclass [^longs things] 
    (long (alength things))))
(let [aclass (class (short-array 0))]
  (defmethod generic-count aclass [^shorts things] 
    (long (alength things))))
(let [aclass (class (object-array 0))]
  (defmethod generic-count aclass [^objects things] 
    (long (alength things))))

(defmethod generic-count Counted [^Counted things] 
  (long (clojure.core/count things)))
(prefer-method generic-count Counted Iterable)
(prefer-method generic-count Counted Map)

(defmethod generic-count [IFn Object] [f things]
  (let [i (iterator things)]
    (loop [n (long 0)]
      (if-not (.hasNext i)
        (long n)
        (recur (if (f (.next i)) (inc n) n))))))

;; TODO: defmulti can't declare primitive return values?
(defn count 
  "How many elements in <code>things</code>?<br>
   How many elements in <code>(filter f things)</code>?"
  (^long [things] (long (generic-count things)))
  (^long [f things] (long (generic-count f things))))
;;----------------------------------------------------------------
;; predicates
;;----------------------------------------------------------------
(defmulti empty? 
  "Is <code>thing</code> empty, in whatever makes sense for it?"
  class)

(defmethod empty? nil [_] true)
(defmethod empty? Iterator [^Iterator x] (not (.hasNext x)))
(defmethod empty? Iterable [^Iterable x] (empty? (.iterator x)))

(defmethod empty? String [^String x] (>= 0 (.length x)))

(defmethod empty? Collection [^Collection x] (>= 0 (.size x)))
(prefer-method empty? Collection Iterable)
(defmethod empty? Map [^Map x] (>= 0 (.size x)))
(defmethod empty? Multimap [^Multimap x] (empty? (.asMap x)))

#_(defmethod empty? 
    IPersistentCollection [^IPersistentCollection x] 
    (clojure.core/empty? x))
(defmethod empty? Counted [^Counted x] (clojure.core/empty? x))
(prefer-method empty? Counted Iterable)
(prefer-method empty? Counted Map)
(prefer-method empty? Map Iterable)

#_(defmethod empty? Sequential [^Sequential x] 
    (clojure.core/empty? x))

(let [aclass (class (boolean-array 0))]
  (defmethod empty? aclass [^booleans x]
    (>= 0 (long (alength x)))))
(let [aclass (class (byte-array 0))]
  (defmethod empty? aclass [^bytes x] 
    (>= 0 (long (alength x)))))
(let [aclass (class (char-array 0))]
  (defmethod empty? aclass [^chars x] 
    (>= 0 (long (alength x)))))
(let [aclass (class (double-array 0))]
  (defmethod empty? aclass [^doubles x]
    (>= 0 (long (alength x)))))
(let [aclass (class (float-array 0))]
  (defmethod empty? aclass [^floats x]
    (>= 0 (long (alength x)))))
(let [aclass (class (int-array 0))]
  (defmethod empty? aclass [^ints x] 
    (>= 0 (long (alength x)))))
(let [aclass (class (long-array 0))]
  (defmethod empty? aclass [^longs x] 
    (>= 0 (long (alength x)))))
(let [aclass (class (short-array 0))]
  (defmethod empty? aclass [^shorts x] 
    (>= 0 (long (alength x)))))
(let [aclass (class (object-array 0))]
  (defmethod empty? aclass [^objects x] 
    (>= 0 (long (alength x)))))
;;----------------------------------------------------------------
(defn every? 
  "Does <code>f</code> return a truthy value for every element of 
   things?"
  [f things]
  (assert (ifn? f) (print-str "Not a function:" f))
  (let [it (iterator things)]
    (loop []
      (cond (not (.hasNext it)) true
            (not (f (.next it))) false
            :else (recur)))))
;;----------------------------------------------------------------
;; creation and modification
;;----------------------------------------------------------------
(defmulti builder   
  "Return a mutable object that has add, addAll, etc. methods for 
   adding content, and a build method that returns a corresponding 
   immutable object."
  {:arglists '([class-or-prototype])}
  (fn builder-dispatch [class-or-prototype] 
    (if (class? class-or-prototype)
      class-or-prototype
      (class class-or-prototype))))
;;----------------------------------------------------------------
(defmulti build   
  "Tale a mutable builder or prototype object and return a corresponding 
   immutable object."
  {:arglists '([builder-or-prototype])}
  class)
;;----------------------------------------------------------------
;;; destructive (constructive) operations
;;----------------------------------------------------------------
(defmulti add!   
  {:arglists '([builder-or-prototype thing])
   :doc "Take a mutable container and add some <code>thing</code> 
         to it."}
  (fn add!-dispatch [builder-or-prototype thing]
    (class builder-or-prototype)))
;;----------------------------------------------------------------
;; Mutation of Java data structures. Fails on immutable collections!
;; TODO: non-destructive copy version
(defmethod add! Collection [^Collection things thing]
  (.add things thing)
  things)
;;----------------------------------------------------------------
(defmulti add-all!   
  "Tale a mutable object and add the contents of things to it."
  {:arglists '([builder-or-prototype things])}
  (fn add-all!-dispatch [builder-or-prototype things]
    [(class builder-or-prototype) (class things)]))
;;----------------------------------------------------------------
(defmethod add-all! 
  
  [java.util.Collection Iterable]
  [^java.util.Collection dst ^Iterable src]
  
  (assert (not (nil? dst)) (print-str "Can't add to:" dst))
  (cond (instance? java.util.Collection src)
        (.addAll dst ^java.util.Collection src)
        :else
        (let [it (iterator src)]
          (while (.hasNext it) (add! dst (.next it)))))
  dst)
;;----------------------------------------------------------------
;; TODO: take-map together? Transducer?

(defmulti ^Iterable take 
  "Return an 
   <a href=\"https://docs.oracle.com/javase/8/docs/api/index.html?java/lang/Iterable.html\">
   Iterable</a> over the first <code>n</code> elements of
   <code>things</code>."
  {:arglists '([n things])}
  (fn take-dispatch [n things] [(class things)]))

(defmethod take [Iterable] [^long n ^Iterable things]
  (let [b (ArrayList. n)
        it (iterator things)]
    (loop [i 0]
      (when (and (< i n) (.hasNext it))
        (add! b (.next it))
        (recur (inc i))))
    (Collections/unmodifiableList b)))

;; TODO: check mutability of things?
(defmethod take [List] [^long n ^List things]
  (Collections/unmodifiableList (.subList things 0 n)))
(prefer-method take [List] [Iterable])

(defmethod take [Seqable] [^long n ^Seqable things]
  (clojure.core/take n things))
(prefer-method take [Seqable] [List])
;;----------------------------------------------------------------
;; filter: data structure -> similar data structure of smaller 
;; size
;; TODO: (filter f) -> transducer?
;; TODO: (filter-into class-builder-or-prototype f things ...)?
;;----------------------------------------------------------------
(defmulti ^Iterable filter 
  "Select the elements of some data structure where (f thing) is 
   truthy, usually returning a similar structure."
  {:arglists '([f things])}
  (fn filter-dispatch ([f things] [(class f) (class things)])))

(defmethod filter [IFn nil] [^IFn f _]
  (Collections/emptyList))

(defmethod filter [IFn Iterator] [^IFn f ^Iterator things]
  (let [a (ArrayList.)]
    (while (.hasNext things)
      (let [nxt (.next things)]
        (when (f nxt) (.add a nxt))))
    (Collections/unmodifiableList a)))

(defmethod filter [IFn Iterable] [^IFn f ^Iterable things]
  (filter f (iterator things)))

(defmethod filter [IFn Collection] [^IFn f ^Collection things]
  (let [a (ArrayList. (count things))
        it (iterator things)]
    (while (.hasNext it)
      (let [nxt (.next it)]
        (when (f nxt) (.add a nxt))))
    (Collections/unmodifiableList a)))

;; Note: requires f to be a function that takes 2 args.
;; Could use destructuring of Map$Entry 
;; --- need to compare performance.
(defmethod filter [IFn Map] [^IFn f ^Map things]
  (let [m (HashMap. (count things))
        it (iterator things)]
    (while (.hasNext it)
      (let [^Map$Entry nxt (.next it)
            k (.getKey nxt)
            v (.getValue nxt)]
        (when (f k v) (.put m k v))))
    (Collections/unmodifiableMap m)))

(defmethod filter [IFn Table] [^IFn f ^Table table]
  (assert (ifn? f))
  (assert table "no table to filter over")
  (let [builder (ImmutableTable/builder)]
    (doseq [^Table$Cell cell (.cellSet table)]
      (assert (not (nil? cell)) (print-str "Nil cell:" table))
      (let [r (.getRowKey cell)
            c (.getColumnKey cell)
            v (.getValue cell)]
        (assert (not (nil? r)) (print-str "Nil cell-row:" cell))
        (assert (not (nil? c)) (print-str "Nil cell-col:" cell))
        (assert (not (nil? v)) (print-str "Nil value:" cell f))
        ;; TODO: (f r c v) like Maps?
        (when (f cell) (.put builder r c v))))
    (.build builder)))

;; preserve Clojure behavior on Clojure data structures.
;; TODO: is this what we want?
#_(defmethod filter [IFn Seqable] [^IFn f ^Seqable things]
    (clojure.core/filter f things))

#_(prefer-method filter [IFn Seqable] [IFn Collection])

#_(prefer-method filter [IFn Seqable] [IFn Iterable])

#_(defmethod filter 
    [IFn IPersistentMap] [^IFn f ^IPersistentMap things]
    (clojure.core/filter f things))

#_(prefer-method filter [IFn IPersistentMap] [IFn Collection])

#_(prefer-method filter [IFn IPersistentMap] [IFn Iterable])
;;----------------------------------------------------------------
;; split-with: data structure -> 2 similar data structures of 
;; smaller size
;; TODO: (split-with f) -> transducer?
;; TODO: (split-with-into class-builder-or-prototype f things...)?
;;----------------------------------------------------------------
(defmulti ^java.util.List !split-with
  "Return list containing 2 new mutable 'collections':
   one containing the elements where (f thing) is truthy, 
   and one containing the other elements. 
   Like running [[filter]] twice, once with <code>f</code> and 
   once with <code>(fn [x] (not (f x)))</code>."
  {:arglists '([f things])}
  (fn !split-with-dispatch 
    ([f things] [(class f) (class things)])))

(defmethod !split-with [IFn nil] [^IFn f _] 
  [(ArrayList.) (ArrayList.)])

(defmethod !split-with [IFn Iterator] [^IFn f ^Iterator things]
  (let [a0 (ArrayList.)
        a1 (ArrayList.)]
    (while (.hasNext things)
      (let [nxt (.next things)]
        (if (f nxt) 
          (.add a0 nxt)
          (.add a1 nxt))))
    [a0 a1]))

(defmethod !split-with [IFn Iterable] [^IFn f ^Iterable things]
  (!split-with f (iterator things)))

(defmethod !split-with 
  [IFn RandomAccess] 
  [^IFn f ^RandomAccess things]
  ;; assuming things is a List and won't change!
  (assert (instance? List things))
  (let [^List things things
        a0 (ArrayList.)
        a1 (ArrayList.)
        n (int (.size things))]
    (dotimes [i n]
      (let [nxt (.get things i)
            fi (f nxt)]
        (if fi
          (.add a0 nxt)
          (.add a1 nxt))))
    [a0 a1]))

(prefer-method !split-with [IFn RandomAccess] [IFn Iterable])

;; Note: requires f to be a function that takes 2 args.
;; Could use destructuring of Map$Entry 
;; --- need to compare performance.
(defmethod !split-with [IFn Map] [^IFn f ^Map things]
  (let [m0 (HashMap. (quot (count things) 4))
        m1 (HashMap. (quot (count things) 4))
        it (iterator things)]
    (while (.hasNext it)
      (let [^Map$Entry nxt (.next it)
            k (.getKey nxt)
            v (.getValue nxt)]
        (if (f k v) 
          (.put m0 k v)
          (.put m1 k v))))
    [m0 m1]))
;;----------------------------------------------------------------
(defn split-with
  "Return list containing 2 new 'collections':
   one containing the elements where (f thing) is truthy, 
   and one containing the other elements. 
   Like running [[filter]] twice, once with <code>f</code> and 
   once with <code>(fn [x] (not (f x)))</code>."
  ^java.util.List [f things]
  (let [[x0 x1] (!split-with f things)]
    (if (instance? java.util.Map things)
      [(Collections/unmodifiableMap x0) 
       (Collections/unmodifiableMap x1)]
      [(Collections/unmodifiableList x0) 
       (Collections/unmodifiableList x1)])))
;;----------------------------------------------------------------
(defn partition
  "Return a list of lists of the elements of <code>things</code>.
   Each inner sublist is length <code>n</code>, except the last,
   which has however many remaining elements there are after as
   many as possible <code>n</code> sized lists are created.<br>
   <b>Note:</b> no support for <code>step</code> or 
   <code>pad</code> (yet)."
  ([^long n ^Iterable things]
    (let [outer (ArrayList. 2)
          it (iterator things)]
      (while (.hasNext it)
        (let [inner (ArrayList. (int n))]
          (loop [i (int 0)]
            (if (or (not (.hasNext it)) (<= n i))
              (.add outer (Collections/unmodifiableList inner))
              (do
                (.add inner (.next it))
                (recur (inc i)))))))
      (Collections/unmodifiableList outer))))
;;----------------------------------------------------------------
;; map-into: data structure into possibly different type of data 
;; structure
;; TODO: defmulti 
;;----------------------------------------------------------------
(defn map-to-objects 
  "Like [[map]] but with output in an array with element type 
   <code>type</code>."
  ^objects [type f things]
  (assert (class? type) (print-str "Not a class:" type))
  (assert (ifn? f) (print-str "Not a function:" f))
  (let [n (count things)
        ^objects a (make-array type n)
        it (iterator things)]
    (assert (.isArray ^Class (class a)))
    (dotimes [i (int n)]
      (assert (.hasNext it))
      (let [ai (f (.next it))] 
        (try
          (aset a i ai)
          (catch Throwable t
            (println "array class:" (class a))
            (println "element type:" (.getComponentType (class a)))
            (println "new value type:" (class ai))
            (throw t)))))
    a))
;;----------------------------------------------------------------
(defn map-to-doubles 
  "Like [[map]] but with output in a <code>double[]</code>."
  ^doubles [f things]
  (assert (ifn? f) (print-str "Not a function:" f))
  (let [n (count things)
        ^doubles a (double-array n)
        it (iterator things)]
    (if (instance? clojure.lang.IFn$OD f)
      (let [^clojure.lang.IFn$OD fd f]
        (dotimes [i n]
          (assert (.hasNext it))
          (aset a i (.invokePrim fd (.next it)))))
      (dotimes [i n]
        (assert (.hasNext it))
        (aset a i (double (f (.next it))))))
    a))
;;----------------------------------------------------------------
;; map: data structure -> similar data structure of same size
;; TODO: (map f) -> transducer?
;; TODO: (map-into class-builder-or-prototype f things ...)?
;;       and (map f things0 ...) -? 
;;       (map-into things0 f things0 ...)
;; TODO: use arglist of f in map-dispatch, to determine if mapping 
;; over key, value or entry, ...
;;----------------------------------------------------------------
(defmulti map 
  "Apply a function to each 'element' of some data structure, 
   returning a structure similar to the first argument."
  {:arglists '([f things0] 
                [f things0 things1] 
                [f things0 things1 things2])}
  (fn map-dispatch ([& args] (mapv class args))))

(defmethod map [IFn Iterator] [^IFn f ^Iterator things]
  (let [a (ArrayList.)]
    (while (.hasNext things)
      (let [nxt (.next things)]
        (.add a (f nxt))))
    (Collections/unmodifiableList a)))

(defmethod map [IFn Iterable] [^IFn f ^Iterable things]
  (map f (.iterator things)))

(defmethod map [IFn Collection] [^IFn f ^Collection things]
  (let [a (ArrayList. (.size things))
        it (.iterator things)]
    (while (.hasNext it)
      (let [nxt (.next it)]
        (.add a (f nxt))))
    (Collections/unmodifiableList a)))

;; Note: requires f to be a function that takes 2 args.
;; Could use destructuring of Map$Entry 
;; --- need to compare performance.
(defmethod map [IFn Map] [^IFn f ^Map things]
  (let [m (HashMap. (.size things))
        it (iterator things)]
    (while (.hasNext it)
      (let [^Map$Entry nxt (.next it)
            k (.getKey nxt)
            v0 (.getValue nxt)
            v1 (f k v0)]
        (.put m k v1)))
    (Collections/unmodifiableMap m)))

(prefer-method map [IFn Map] [IFn Object])
(prefer-method map [IFn Map] [IFn Iterable])

(defmethod map [IFn Table] [^IFn f ^Table table]
  (assert (ifn? f))
  (assert table "no table to map over")
  (let [builder (ImmutableTable/builder)]
    (doseq [^Table$Cell cell (.cellSet table)]
      (assert (not (nil? cell)) (print-str "Nil cell:" table))
      (let [r (.getRowKey cell)
            c (.getColumnKey cell)
            v (f cell)]
        (assert (not (nil? r)) (print-str "Nil cell-row:" cell))
        (assert (not (nil? c)) (print-str "Nil cell-col:" cell))
        (assert (not (nil? v)) (print-str "Nil value:" cell f))
        (.put builder r c v)))
    (.build builder)))

(defmethod map [IFn Seqable] [^IFn f ^Seqable things]
  (let [a (if (counted? things) 
            (ArrayList. (count things))
            (ArrayList.))
        it (iterator things)]
    (while (.hasNext it)
      (let [nxt (.next it)]
        (.add a (f nxt))))
    (Collections/unmodifiableList a)))

(prefer-method map [IFn Seqable] [IFn Collection])
(prefer-method map [IFn Seqable] [IFn Iterable])
(prefer-method map [IFn Map] [IFn Seqable])

(defmethod map 
  [IFn IPersistentMap] 
  [^IFn f ^IPersistentMap things]
  (let [m (if (counted? things)
            (HashMap. (count things))
            (HashMap.))
        it (iterator things)]
    (while (.hasNext it)
      (let [^Map$Entry nxt (.next it)
            k (.getKey nxt)
            v0 (.getValue nxt)
            v1 (f k v0)]
        (.put m k v1)))
    (Collections/unmodifiableMap m)))

(prefer-method map [IFn IPersistentMap] [IFn Collection])
(prefer-method map [IFn IPersistentMap] [IFn Iterable])
(prefer-method map [IFn IPersistentMap] [IFn Map])
(prefer-method map [IFn IPersistentMap] [IFn Seqable])

;;----------------------------------------------------------------
;; 3 args

(defmethod map 
  [IFn Iterator Iterator] 
  [^IFn f ^Iterator things0 ^Iterator things1]
  (let [a (ArrayList.)]
    (while (and (.hasNext things0) (.hasNext things1))
      (.add a (f (.next things0) (.next things1))))
    (Collections/unmodifiableList a)))

(defmethod map 
  [IFn Object Object] 
  [^IFn f ^Object things0 ^Object things1]
  (map f (iterator things0) (iterator things1)))

(defmethod map 
  [IFn Collection Collection] 
  [^IFn f ^Collection things0 ^Collection things1]
  (let [a (ArrayList.)
        i0 (iterator things0)
        i1 (iterator things1)]
    (while (and (.hasNext i0) (.hasNext i1))
      (.add a (f (.next i0) (.next i1))))
    (Collections/unmodifiableList a)))

;;----------------------------------------------------------------
;; 4 args

(defmethod map 
  [IFn Object Object Object] 
  [^IFn f ^Object things0 ^Object things1 ^Object things2]
  (let [a (ArrayList.)
        i0 (iterator things0)
        i1 (iterator things1)
        i2 (iterator things2)]
    (while (and (.hasNext i0) (.hasNext i1) (.hasNext i2))
      (.add a (f (.next i0) (.next i1) (.next i2))))
    (Collections/unmodifiableList a)))

(defmethod map 
  [IFn Collection Collection Collection] 
  [^IFn f 
   ^Collection things0 
   ^Collection things1 
   ^Collection things2]
  (let [a (ArrayList. (min (count things0) 
                           (count things1) 
                           (count things2)))
        i0 (iterator things0)
        i1 (iterator things1)
        i2 (iterator things2)]
    (while (and (.hasNext i0) (.hasNext i1) (.hasNext i2))
      (.add a (f (.next i0) (.next i1) (.next i2))))
    (Collections/unmodifiableList a)))

;;----------------------------------------------------------------
;; mapc: iterate over data structure for side effects
;; TODO: (mapc f) -> transducer?
;; TODO: (map-into nil f things0 ...)?
;;----------------------------------------------------------------
(defmulti mapc 
  "Apply a function to each 'element' of some data structure for 
   side effects, returning <code>nil</code>."
  {:arglists '([f things0] 
                [f things0 things1] 
                [f things0 things1 things2])}
  (fn mapc-dispatch ([& args] (mapv class args))))

(defmethod mapc [IFn Iterator] [^IFn f ^Iterator things]
  (while (.hasNext things) (f (.next things)))
  nil)

(defmethod mapc [IFn Object] [^IFn f ^Object things]
  (mapc f (iterator things)))

;; Note: requires f to be a function that takes 2 args.
;; Could use destructuring of Map$Entry 
;; --- need to compare performance.
(defmethod mapc [IFn Map] [^IFn f ^Map things]
  (let [it (iterator things)]
    (while (.hasNext it)
      (let [^Map$Entry nxt (.next it)
            k (.getKey nxt)
            v (.getValue nxt)]
        (f k v)))
    nil))

(prefer-method mapc [IFn Map] [IFn Object])

(defmethod mapc [IFn Table] [^IFn f ^Table table]
  (assert (ifn? f))
  (assert table "no table to mapc over")
  (doseq [^Table$Cell cell (.cellSet table)]
    (assert (not (nil? cell)) (print-str "Nil cell:" table))
    (f cell))
  nil)
;;----------------------------------------------------------------
;; 3 args

(defmethod mapc 
  [IFn Iterator Iterator] 
  [^IFn f ^Iterator things0 ^Iterator things1]
  (while (and (.hasNext things0) (.hasNext things1))
    (f (.next things0) (.next things1))))

(defmethod mapc 
  [IFn Object Object] 
  [^IFn f ^Object things0 ^Object things1]
  (mapc f (iterator things0) (iterator things1)))

;;----------------------------------------------------------------
;; 4 args

(defmethod mapc 
  [IFn Iterator Iterator Iterator] 
  [^IFn f ^Iterator i0 ^Iterator i1 ^Iterator i2]
  (while (and (.hasNext i0) (.hasNext i1) (.hasNext i2))
    (f (.next i0) (.next i1) (.next i2)))
  nil)

(defmethod mapc 
  [IFn Object Object Object] 
  [^IFn f ^Object things0 ^Object things1 ^Object things2]
  (mapc f 
        (iterator things0) (iterator things1) (iterator things2)))
;;----------------------------------------------------------------
;; concurrent mapping
;;----------------------------------------------------------------
;; Looks like clojure functions are both Callable and Runnable,
;; and ExecutorService treats them as Runnable (no return value).
(defn- callable 
  (^java.util.concurrent.Callable [f x] 
    (assert (ifn? f) (print-str "Not a function:" f))
    (reify java.util.concurrent.Callable 
      (call [this] (f x))))
  (^java.util.concurrent.Callable [f x0 x1] 
    (assert (ifn? f) (print-str "Not a function:" f))
    (reify java.util.concurrent.Callable 
      (call [this] (f x0 x1))))
  (^java.util.concurrent.Callable [f x0 x1 x2] 
    (assert (ifn? f) (print-str "Not a function:" f))
    (reify java.util.concurrent.Callable 
      (call [this] (f x0 x1 x2))))
  (^java.util.concurrent.Callable [f x0 x1 x2 & args] 
    (assert (ifn? f) (print-str "Not a function:" f))
    (reify java.util.concurrent.Callable 
      (call [this] (apply f x0 x1 x2 args)))))
;;----------------------------------------------------------------
;; TODO: how expensive does f have to be for this to be faster 
;; than a serial loop
(defn nmap-doubles
  "Like [[nmap]] with output in a <code>double[]</code>."
  (^doubles [^long n ^clojure.lang.IFn$OD f things]
    (let [pool (Executors/newFixedThreadPool (int n))
          tasks (map #(callable f %) things)
          b (double-array (count things))
          futures (iterator (.invokeAll pool tasks))]
      (try
        (loop [i 0]
          (when (.hasNext futures)
            (let [^Future future (.next futures)]
              (aset b i (double (.get future))))
            (recur (inc i))))
        (finally (.shutdown pool)))
      b))
  (^doubles [^long n ^clojure.lang.IFn$OD f things0 things1]
    (let [pool (Executors/newFixedThreadPool (int n))
          tasks (map (fn [x0 x1] (callable f x0 x1)) 
                     things0 things1)
          b (double-array (min (count things0) (count things1)))
          futures (iterator (.invokeAll pool tasks))]
      (try
        (loop [i 0]
          (when (.hasNext futures)
            (let [^Future future (.next futures)]
              (aset b i (double (.get future))))
            (recur (inc i))))
        (finally (.shutdown pool)))
      b))
  (^doubles [n ^clojure.lang.IFn$OD f things0 things1 things2]
    (let [pool (Executors/newFixedThreadPool (int n))
          tasks (map (fn [x0 x1 x2] (callable f x0 x1 x2))
                     things0 things1 things2)
          b (double-array (min (count things0) 
                               (count things1) 
                               (count things2)))
          futures (iterator (.invokeAll pool tasks))]
      (try
        (loop [i 0]
          (when (.hasNext futures)
            (let [^Future future (.next futures)]
              (aset b i (double (.get future))))
            (recur (inc i))))
        (finally (.shutdown pool)))
      b)))
;;----------------------------------------------------------------
(defn pmap-doubles
  "Like [[pmap]] with output in a <code>double[]</code>."
  (^doubles [^clojure.lang.IFn$OD f things]
    (assert (ifn? f) (print-str "Not a function:" f))
    (nmap-doubles (.availableProcessors (Runtime/getRuntime)) 
                  f things))
  (^doubles [^clojure.lang.IFn$OD f things0 things1]
    (assert (ifn? f) (print-str "Not a function:" f))
    (nmap-doubles (.availableProcessors (Runtime/getRuntime))
                  f things0 things1))
  (^doubles [^clojure.lang.IFn$OD f things0 things1 things2]
    (assert (ifn? f) (print-str "Not a function:" f))
    (nmap-doubles (.availableProcessors (Runtime/getRuntime)) 
                  f things0 things1 things2)))
;;----------------------------------------------------------------
;; TODO: nmap, pmap to one generic function
(defmulti nmap 
  "Apply a function to each 'element' of some data structure, 
   returning a structure similar to the first argument. Use <
   code>n</code> threads to evaluate the function calls in 
   parallel."
  {:arglists '( [n f things0] 
                [n f things0 things1] 
                [n f things0 things1 things2])}
  (fn nmap-dispatch 
    ([n f things0] 
      [(class f) (class things0)])
    ([n f things0 things1]
      [(class f) (class things0) (class things1)]) 
    ([n f things0 things1 things2] 
      [(class f) (class things0) (class things1) (class things2)])))

(defn- nmap-collect-results 
  ^Iterable [^long n ^java.util.Collection tasks]
  (let [pool (Executors/newFixedThreadPool (int n))
        a (ArrayList. (count tasks))]
    (try
      (mapc #(.add a (.get ^Future %)) (.invokeAll pool tasks))
      (finally (.shutdown pool)))
    (Collections/unmodifiableList a)))

(defmethod nmap [IFn Iterator] [n ^IFn f ^Iterator things]
  (nmap-collect-results n (map #(callable f %) things)))

(defmethod nmap [IFn Object] [n ^IFn f ^Object things]
  (nmap n f (iterator things)))

(defmethod nmap 
  [IFn Iterator Iterator] 
  [^long n ^IFn f ^Iterator things0 ^Iterator things1]
  (nmap-collect-results 
    n (map (fn [x0 x1] (callable f x0 x1)) things0 things1)))

(defmethod nmap 
  [IFn Object Object] 
  [^long n ^IFn f ^Object things0 ^Object things1]
  (nmap n f (iterator things0) (iterator things1)))

(defmethod nmap 
  [IFn Iterator Iterator Iterator] 
  [n ^IFn f ^Iterator i0 ^Iterator i1 ^Iterator i2]
  (nmap-collect-results 
    (long n) (map (fn [x0 x1 x2] (callable f x0 x1 x2)) 
                  i0 i1 i2)))

(defmethod nmap 
  [IFn Object Object Object] 
  [n ^IFn f ^Object things0 ^Object things1 ^Object things2]
  (nmap n f 
        (iterator things0) (iterator things1) (iterator things2)))
;;----------------------------------------------------------------
(defn pmap
  
  "Eager version of <code>clojure.core/pmap</code> that uses as 
   any threads as there are 
   <a href=\"https://docs.oracle.com/javase/8/docs/api/java/lang/Runtime.html#availableProcessors--\">
   available processors."
  
  (^Iterable [f things]
    (assert (ifn? f) (print-str "Not a function:" f))
    (nmap (.availableProcessors (Runtime/getRuntime)) f 
          things))
  
  (^Iterable [f things0 things1]
    (assert (ifn? f) (print-str "Not a function:" f))
    (nmap (.availableProcessors (Runtime/getRuntime)) f 
          things0 things1))
  
  (^Iterable [f things0 things1 things2]
    (assert (ifn? f) (print-str "Not a function:" f))
    (nmap (.availableProcessors (Runtime/getRuntime)) f 
          things0 things1 things2)))
;;----------------------------------------------------------------
;; concurrent side effects; danger, danger!
;;----------------------------------------------------------------
(defn nmapc
  "[[nmap]] but purely for side effects; always returns 
   <code>nil</code>."
  ([^long n f things]
    (assert (ifn? f) (print-str "Not a function:" f))
    (let [pool (Executors/newFixedThreadPool (int n))
          tasks (mapv #(callable f %) things)]
      (assert (every? #(instance? Callable %) tasks))
      (assert (every? #(not (instance? Runnable %)) tasks))
      (try
        (doseq [^Future future (.invokeAll pool tasks)] 
          (.get future))
        (finally (.shutdown pool))))
    nil)
  ([^long n f things0 things1]
    (assert (ifn? f) (print-str "Not a function:" f))
    (let [pool (Executors/newFixedThreadPool (int n))
          tasks (mapv (fn [x0 x1] (callable f x0 x1)) 
                      things0 things1)]
      (try
        (doseq [^Future future (.invokeAll pool tasks)] 
          (.get future))
        (finally (.shutdown pool))))
    nil))
;;----------------------------------------------------------------
;; TODO: move somewhere more appropriate
;; This works for any Iterable, not just immutable lists.
;; Faster and less garbage than seq operation on iterator-seq on 
;; Iterable.
(defn pmapc
  "[[pmap]] but purely for side effects; always returns 
   <code>nil</code>."
  ([f things0 things1]
    (assert (ifn? f) (print-str "Not a function:" f))
    (nmapc (.availableProcessors (Runtime/getRuntime)) f 
           things0 things1))
  ([f things]
    (assert (ifn? f) (print-str "Not a function:" f))
    (nmapc (.availableProcessors (Runtime/getRuntime)) f 
           things)))
;;----------------------------------------------------------------
;; mapcat and friends
;;----------------------------------------------------------------
#_(defn concat 
    
    "Return an 
   <a href=\"https://docs.oracle.com/javase/8/docs/api/index.html?java/lang/Iterable.html\">
   Iterable</a>
   which is the concatentation of the input <code>Iterable</code>s."
    
    ^Iterable [& iterables]
    
    (if (empty? iterables)
      (Collections/emptyList)
      (let [b (ArrayList.
                (int (clojure.core/reduce 
                       + 
                       (clojure.core/map count iterables))))]
        (doseq [^Iterable i iterables] (when i (add-all! b i)))
        (Collections/unmodifiableList b))))

(defmulti concat
  "Return a 'collection' which is the natural concatenation of the 
   <a href=\"https://docs.oracle.com/javase/8/docs/api/index.html?java/lang/Iterable.html\">
   Iterable</a>
   which is the concatentation of the input <code>Iterable</code>s."
  
  {:arglists '([] [stuff] [stuff & more-stuff])}
  (fn concat-dispatch 
    ([] nil)
    ([stuff] :one)
    ;; TODO: assuming everything 'collection' is the same
    ;; could do it pairwise --- more garbage but fully general
    ([stuff & more-stuff] (class stuff))))

(defmethod concat nil [] (Collections/emptyList)) 

(defmethod concat :one [stuff] stuff) 

(defmethod concat Iterable [& iterables] 
  (let [b (ArrayList. 
            (int (reduce 
                   (fn [^long n iterable] (+ n (count iterable)))
                   0
                   iterables)))]
    (doseq [it iterables] (add-all! b it))
    (Collections/unmodifiableList b)))

(defmethod concat java.util.Set [^java.util.Set s0 & collections] 
  (if (clojure.core/empty? collections)
    s0
    (let [s (java.util.HashSet. s0)]
      (doseq [^java.util.Collection c collections] (.addAll s c))
      (Collections/unmodifiableSet s))))

(prefer-method concat java.util.Set Iterable)

(let [aclass (class (double-array 0))]
  (defmethod concat aclass [& arrays] 
    (let [n (int (reduce + alength arrays))
          ^doubles a (double-array n)]
      (loop [i (int 0)
             arrays arrays]
        (when-not (empty? arrays)
          (let [^doubles ai (first arrays)
                ni (int (alength ai))]
            (System/arraycopy ai (int 0) a i ni)
            (recur (+ i ni) (rest arrays)))))
      a)))
;;----------------------------------------------------------------
(defn mapcat 
  "More general eager version of 
   <code>clojure.core/mapcat</code>."
  ^java.util.List [f things]
  (assert (ifn? f) (print-str "Not a function:" f))
  (when things
    (let [l (ArrayList.)
          it (iterator things)]
      (while (.hasNext it) 
        (let [^java.util.Collection fi (f (.next it))]
          (when fi (.addAll l fi))))
      (Collections/unmodifiableList l))))
;;----------------------------------------------------------------
(defn nmapcat
  "A combination of [[nmap]] and [[mapcat]]."
  ([^long n f things]
    (assert (ifn? f) (print-str "Not a function:" f))
    (let [pool (Executors/newFixedThreadPool (int n))
          tasks (mapv #(callable f %) things)
          b (ArrayList.)]
      (try
        (doseq [^Future future (.invokeAll pool tasks)]
          (when-let [^Iterable x (.get future)] x (.addAll b x)))
        (finally (.shutdown pool)))
      (Collections/unmodifiableList b))))
;;----------------------------------------------------------------
(defn pmapcat
  "A combination of [[pmap]] and [[mapcat]]."
  ([f things]
    (assert (ifn? f) (print-str "Not a function:" f))
    (nmapcat 
      (.availableProcessors (Runtime/getRuntime)) f things)))
;;----------------------------------------------------------------
;; collection IO
;;----------------------------------------------------------------
(defmethod clojure.core/print-method 
  java.util.Set [^java.util.Set things ^java.io.Writer w]
  (.write w "#{")
  (let [it (iterator things)]
    (when (.hasNext it) (clojure.core/print-method (.next it) w))
    (mapc #(do (.write w " ") (clojure.core/print-method % w)) 
          it))
  (.write w "}"))

(defmethod clojure.core/print-method 
  java.util.List [^java.util.List things ^java.io.Writer w]
  (.write w "[")
  (let [it (iterator things)]
    (when (.hasNext it) (clojure.core/print-method (.next it) w))
    (mapc #(do (.write w " ") (clojure.core/print-method % w)) 
          it))
  (.write w "]"))

(defmethod clojure.core/print-method 
  com.google.common.collect.RegularImmutableList
  [^com.google.common.collect.RegularImmutableList things 
   ^java.io.Writer w]
  (.write w "[")
  (let [it (iterator things)]
    (when (.hasNext it) (clojure.core/print-method (.next it) w))
    (mapc #(do (.write w " ") (clojure.core/print-method % w)) it))
  (.write w "]"))

;;----------------------------------------------------------------
;; sorting
;;----------------------------------------------------------------

#_(defmulti sort-by 
    "Sort <code>data</code> by the value of <code>z</code>,
   using the <code>comparator</code> if supplied.<br>
   If <code>comparator</code> is not supplied, then the range of 
   <code>z</code> over <code>data</code> must contain only 
   mutually comparable objects."
    {:arglists '([z data] [z comparatorS data])}
    (fn sort-by-dispatch ([& args] (mapv class args))))

;; TODO: methods that extract z values to array of primitives, and 
;; do a parallel sort of the array and the list.

;;----------------------------------------------------------------
;; un-sorting
;;----------------------------------------------------------------
(defmulti shuffle!
  
  "Destructively and randomly permute an ordered 'collection' 
   <code>things</code> using the pseudo-random number generator 
   <code>prng</code>."
  
  {:arglists '([things] [things ^java.util.Random prng])}
  
  (fn shuffle!-dispatch 
    ([things] 
      [(class things)])
    ([things ^java.util.Random prng] 
      [(class things) (class prng)])))

(defmethod shuffle! 
  [nil java.util.Random] 
  [things ^java.util.Random prng] 
  nil) 

(defmethod shuffle! 
  [List java.util.Random] 
  [^List things ^java.util.Random prng] 
  (Collections/shuffle things prng)
  things)

(let [aclass (class (double-array 0))]
  (defmethod shuffle! 
    [aclass  java.util.Random]
    [^doubles a ^java.util.Random prng] 
    (loop [i (int (dec(alength a)))]
      (when (< 0 i)
        (let [ai (aget a i)
              j (.nextInt prng (inc i))]
          (aset-double a i (aget a j))
          (aset-double a j ai))))
    a))

(defmethod shuffle! :default [things] 
  (shuffle! things (ThreadLocalRandom/current))) 
;;----------------------------------------------------------------
;; TODO: with a generic copy function, could replace this by a 
;; simple function

(defmulti shuffle
  
  "Return a similar ordered 'collection' which contains a random 
   permutation of the elements of <code>things</code> using the 
   pseudo-random number generator 
   <code>prng</code>."
  
  {:arglists '([things] [things ^java.util.Random prng])}
  
  (fn shuffle-dispatch 
    ([things] 
      [(class things)])
    ([things ^java.util.Random prng]
      [(class things) (class prng)])))

(defmethod shuffle 
  [nil java.util.Random]
  [things ^java.util.Random prng]
  (Collections/emptyList)) 

;; can't shuffle an Iterable in place
;; TODO: generic copy to same Iterable implementation
(defmethod shuffle 
  [Iterable java.util.Random]
  [^Iterable things ^java.util.Random prng] 
  (let [n (int (count things))
        a (ArrayList. (count things))]
    (add-all! a things)
    (loop [i (int (dec n))]
      (when (< 0 i)
        (let [ai (.get a i)
              j (.nextInt prng (inc i))]
          (.set a i (.get a j))
          (.set a j ai))))
    (Collections/unmodifiableList a)))

(defmethod shuffle 
  [List  java.util.Random]
  [^List things ^java.util.Random prng] 
  ;; TODO: generic copy to same List implementation
  (shuffle! (ArrayList. things) prng))

(let [aclass (class (double-array 0))]
  (defmethod shuffle 
    [aclass  java.util.Random]
    [^doubles a ^java.util.Random prng] 
    (shuffle! (Arrays/copyOf a (int (alength a))) prng)))

(defmethod shuffle :default [things] 
  (shuffle things (ThreadLocalRandom/current))) 
;;----------------------------------------------------------------

