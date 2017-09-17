(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-10-28"
      :doc "Eager versions of filter, map, reduce, etc." }

    zana.collections.guava
  
  (:refer-clojure :exclude [compare concat count doall drop empty? every? filter 
                            first list map map-indexed mapcat next nth pmap 
                            remove repeatedly second shuffle some sort sort-by 
                            split-at take])
  (:require [clojure.pprint :as pp]
            [zana.commons.core :as cc]
            [zana.collections.generic :as g])
  (:import [com.google.common.base Function Predicate]
           [com.google.common.collect ImmutableList Iterables Iterators Multimap 
            Ordering Sets Table]
           [com.google.common.primitives Doubles]
           [clojure.lang IFn]
           [java.util ArrayList Collection Collections HashMap IdentityHashMap 
            Iterator List Map Set]
           [java.util.concurrent Executors Future]))
;;------------------------------------------------------------------------------
;; TODO: rename to zana.collections.eager
;; TODO: break up into list, map, set, table, ..., namespaces
;; TODO: ImmutableList vs Collections/unmodifiableList performance issues.
;;------------------------------------------------------------------------------
(defn function ^com.google.common.base.Function [f]
  (cond (instance? Function f) f
        (ifn? f) (reify Function (apply [this x] (f x)))
        :else (throw (IllegalArgumentException. 
                       (print-str "Not a function:" f)))))
;;------------------------------------------------------------------------------
(defn predicate ^com.google.common.base.Predicate [f]
  (cond (instance? Predicate f) f
        (ifn? f)(reify Predicate (apply [this x] (boolean (f x))))
        :else (throw (IllegalArgumentException. 
                       (print-str "Not a function:" f)))))
;;------------------------------------------------------------------------------
;; ImmutableList
;;------------------------------------------------------------------------------
(defn nth [things ^long i]
  (assert things (str "No things to fetch " i "th element from."))
  (cond (instance? List things) (.get ^List things (int i))
        (instance? Iterator things) (let [^Iterator things things
                                          i (int i)]
                                      (loop [ii (int 0)]
                                        (when (g/has-next? things)
                                          (let [x (g/next-item things)]
                                            (if (== ii i)
                                              x
                                              (recur (inc ii)))))))
        :else (nth (g/iterator things) i)))
;;------------------------------------------------------------------------------
;; A misleading name here!
(defn ^:no-doc doall 
  "Add all the </code>things</code> to a new immutable list."
  [things] 
  (let [b (ArrayList.)]
    (.addAll b things)
    (Collections/unmodifiableList b)))
;;------------------------------------------------------------------------------
(defn #^:no-doc list 
  "Coerce <code>x</code> to an immutable list, if possible.
   Throw an exception if not."
  ^java.util.List [x]
  (cond (nil? x) (ImmutableList/of)
        (instance? List x) x ;; should we copy to immutable list here?
        (instance? Collection x) (ImmutableList/copyOf ^Collection x)
        (instance? Iterable x) (ImmutableList/copyOf ^Iterable x)
        (instance? Iterator x) (ImmutableList/copyOf ^Iterator x)
        (cc/object-array? x) (ImmutableList/copyOf ^objects x)
        :else (throw
                (UnsupportedOperationException. (str "can't List-ify: " x)))))
;;------------------------------------------------------------------------------
(defn any? 

  "Does <code>f</code> return a 
  <a href=\"http://blog.jayfields.com/2011/02/clojure-truthy-and-falsey.html\">truthy</a>
  value for any element of <code>things</code>?<br>
  Returns <code>true</code> or <code>false</code>."
  
  [f things]
  
  (assert (ifn? f) (print-str "Not a function:" f))
  (let [it (g/iterator things)]
    (loop []
      (cond (not (g/has-next? it)) false
            (f (g/next-item it)) true
            :else (recur)))))
;;------------------------------------------------------------------------------
(defn some 

  "Does <code>f</code> return a 
  <a href=\"http://blog.jayfields.com/2011/02/clojure-truthy-and-falsey.html\">truthy</a>
  value for any element of <code>things</code><br> 
  Returns the truthy value or </code>nil</code>."
  
  [f things]
  
  (assert (ifn? f) (print-str "Not a function:" f))
  (let [it (g/iterator things)]
    (loop []
      (if-not (g/has-next? it)
      nil
      (let [fi (f (g/next-item it))]
        (if fi
          fi
          (recur)))))))
;;------------------------------------------------------------------------------
(defn repeatedly 
  
  "Call <code>f</code>, with no arguments, <code>n</code> times, accumulating 
   the results in an <a href=\"https://docs.oracle.com/javase/8/docs/api/index.html?java/lang/Iterable.html\">
   Iterable</a>."
  
  ^Iterable [^long n f]
  
  (assert (ifn? f) (print-str "Not a function:" f))
  (let [b (ArrayList. n)]
    (dotimes [i n] (.add b (f)))
    (Collections/unmodifiableList b)))
;;------------------------------------------------------------------------------
(defn take 
  
  "Return an 
   <a href=\"https://docs.oracle.com/javase/8/docs/api/index.html?java/lang/Iterable.html\">
   Iterable</a> over the first <code>n</code> elements of
   <code>things</code>."
  
  ^Iterable [^long n things]
  
  (let [b (ArrayList. n)
        it (g/iterator things)]
    (loop [i 0]
      (when (and (< i n) (g/has-next? it))
        (g/add! b (g/next-item it))
        (recur (inc i))))
    (Collections/unmodifiableList b)))
;;------------------------------------------------------------------------------
(defn drop 

  "Return an 
   <a href=\"https://docs.oracle.com/javase/8/docs/api/index.html?java/lang/Iterable.html\">
   Iterable</a> that skips the first <code>n</code> elements of
   <code>things</code>."
  
  ^Iterable [^long n things]
  (let [b (ArrayList. n)
        it (g/iterator things)]
    (loop [i 0]
      (when (and (< i n) (g/has-next? it)) 
        (g/next-item it)
        (recur (inc i))))
    (while (g/has-next? it) (g/add! b (g/next-item it)))
    (Collections/unmodifiableList b)))
;;------------------------------------------------------------------------------
(defn split-at 
  "Returns equivalent of <code>[(take n things) (drop n things)]</code>,
   see [[take]] and [[drop]]."
  ^Iterable [^long n things]
  (let [b0 (ArrayList. n)
        b1 (ArrayList. n)
        it (g/iterator things)]
    (loop [i 0]
      (when (and (< i n) (g/has-next? it)) 
        (g/add! b0 (g/next-item it))
        (recur (inc i))))
    (while (g/has-next? it) (g/add! b1 (g/next-item it)))
    [(Collections/unmodifiableList b0) (Collections/unmodifiableList b1)]))
;;------------------------------------------------------------------------------
#_(defn lazy-filter ^Iterable [f ^Iterable things]
  (assert (ifn? f) (print-str "Not a function:" f))
  (Iterables/filter things (predicate f)))
;;------------------------------------------------------------------------------
(defn remove 
  "Like <code>(filter #(not (f %)) things)</code>."
  ^Iterable [f things] 
  (assert (ifn? f) (print-str "Not a function:" f))
  (g/filter #(not (f %)) things))
;;------------------------------------------------------------------------------
(defn not-nil 
  "Like <code>(filter #(not (nil? %)) things)</code>."
  ^Iterable [things]
  (let [b (ArrayList. (g/count things))
        it (g/iterator things)]
    (while (g/has-next? it) (when-let [thing (g/next-item it)] (g/add! b thing)))
    (Collections/unmodifiableList b)))
;;------------------------------------------------------------------------------
#_(defn split-by 
  "Like [[group-by[[, but truthy vs non-truthy, and returns a pair of Iterables."
  [f things]
  (assert (ifn? f) (print-str "Not a function:" f))
  (let [n (g/count things)
        bt (ArrayList. n)
        bf (ArrayList. n)
        it (g/iterator things)]
    (while (g/has-next? it)
      (let [thing (g/next-item it)]
        (if (f thing)
          (g/add! bt thing)
          (g/add! bf thing))))
    [(Collections/unmodifiableList bt) (Collections/unmodifiableList bf)]))
;;------------------------------------------------------------------------------
(defn- map-indexed-collection
  (^Iterable [f ^java.util.Collection things]
    (assert (ifn? f) (print-str "Not a function:" f))
    (let [b (ArrayList. (g/count things))
          it (g/iterator things)]
      (loop [i (int 0)]
        (when (g/has-next? it)
          (let [nxt (g/next-item it)]
            (try (g/add! b (f nxt i))
              (catch Throwable t
                (println "map failed:" f nxt (f i nxt))
                (.printStackTrace t)
                (throw t))))
          (recur (inc i))))
      (Collections/unmodifiableList b)))
  (^Iterable [f ^java.util.Collection things0 ^java.util.Collection things1]
    (assert (ifn? f) (print-str "Not a function:" f))
    (let [b (ArrayList. (max (g/count things0) (g/count things1)))
          it0 (g/iterator things0)
          it1 (g/iterator things1)]
      (loop [i (int 0)]
        (when (and (g/has-next? it0) (g/has-next? it1))
          (g/add! b (f i (g/next-item it0) (g/next-item it1)))
          (recur (inc i))))
      (Collections/unmodifiableList b))))
;;------------------------------------------------------------------------------
(defn- map-indexed-iterable
  (^Iterable [f things]
    (assert (ifn? f) (print-str "Not a function:" f))
    (let [b (ArrayList.)
          it (g/iterator things)]
      (loop [i (int 0)]
        (when (g/has-next? it)
          (let [nxt (g/next-item it)]
            (try (.add b (f nxt i))
              (catch Throwable t
                (println "map failed:" f nxt (f i nxt))
                (.printStackTrace t)
                (throw t))))
          (recur (inc i))))
      (Collections/unmodifiableList b)))
  (^Iterable [f things0 things1]
    (assert (ifn? f) (print-str "Not a function:" f))
    (let [b (ArrayList.)
          it0 (g/iterator things0)
          it1 (g/iterator things1)]
      (loop [i (int 0)]
        (when (and (g/has-next? it0) (g/has-next? it1))
          (.add b (f i (g/next-item it0) (g/next-item it1)))
          (recur (inc i))))
      (Collections/unmodifiableList b))))
;;------------------------------------------------------------------------------
(defn map-indexed
  "Faster, eager, more general version of <code>clojure.core/map-indexed</code>."
  (^Iterable [f things]
    (assert (ifn? f) (print-str "Not a function:" f))
    (cond (instance? java.util.Collection things)
          (map-indexed-collection f things)
          :else
          (map-indexed-iterable f things)))
  (^Iterable [f things0 things1]
    (assert (ifn? f) (print-str "Not a function:" f))
    (cond (and (instance? java.util.Collection things0)
               (instance? java.util.Collection things1))
          (map-indexed-collection f things0 things1)
          :else
          (map-indexed-iterable f things0 things1))))
;;------------------------------------------------------------------------------
(defn filter-map 
  "Like <code>(filter p (map f things))</code>."
  ^java.util.List [p f things]
  (assert (ifn? p) (print-str "Not a function:" p))
  (assert (ifn? f) (print-str "Not a function:" f))
  (let [b (ArrayList. (g/count things))
        it (g/iterator things)]
    (while (g/has-next? it)
      (let [thing (f (g/next-item it))] (when (p thing) (g/add! b thing))))
    (Collections/unmodifiableList b)))
;;------------------------------------------------------------------------------
(defn keep-map 
  "Like <code>(keep identity (map f things))</code>."
  ^java.util.List [f things]
  (assert (ifn? f) (print-str "Not a function:" f))
  (let [b (ArrayList. (g/count things))
        it (g/iterator things)]
    (while (g/has-next? it) (when-let [fi (f (g/next-item it))] (g/add! b fi)))
    (Collections/unmodifiableList b)))
;;------------------------------------------------------------------------------
;; Looks like clojure functions are both Callable and Runnable,
;; and ExecutorService treats them as Runnable (no return value).
#_(defn- callable 
  (^java.util.concurrent.Callable [f x] 
    (assert (ifn? f) (print-str "Not a function:" f))
    (reify java.util.concurrent.Callable (call [this] (f x))))
  (^java.util.concurrent.Callable [f x0 x1] 
    (assert (ifn? f) (print-str "Not a function:" f))
    (reify java.util.concurrent.Callable (call [this] (f x0 x1))))
  (^java.util.concurrent.Callable [f x0 x1 x2] 
    (assert (ifn? f) (print-str "Not a function:" f))
    (reify java.util.concurrent.Callable (call [this] (f x0 x1 x2))))
  (^java.util.concurrent.Callable [f x0 x1 x2 & args] 
    (assert (ifn? f) (print-str "Not a function:" f))
    (reify java.util.concurrent.Callable (call [this] (apply f x0 x1 x2 args)))))
;;------------------------------------------------------------------------------
#_(defn nmap
  "Eager version of <code>clojure.core/pmap</code> that uses <code>n</code>
   threads."
  (^Iterable [n f things]
    (assert (ifn? f) (print-str "Not a function:" f))
    (let [pool (Executors/newFixedThreadPool (int n))
          tasks (mapv #(callable f %) things)
          b (ArrayList. (g/count things))]
      (try
        (doseq [^Future future (.invokeAll pool tasks)]
          (let [v (.get future)]
            (g/add! b v)))
        (finally (.shutdown pool)))
      ;;(.build b)))
      (Collections/unmodifiableList b)))
  (^Iterable [n f things0 things1]
    (assert (ifn? f) (print-str "Not a function:" f))
    (let [pool (Executors/newFixedThreadPool (int n))
          tasks (mapv (fn [x0 x1] (callable f x0 x1)) things0 things1)
          b (ArrayList.)]
      (try
        (doseq [^Future future (.invokeAll pool tasks)] (.add b (.get future)))
        (finally (.shutdown pool)))
      (Collections/unmodifiableList b)))
  (^Iterable [n f things0 things1 things2]
    (assert (ifn? f) (print-str "Not a function:" f))
    (let [pool (Executors/newFixedThreadPool (int n))
          tasks (mapv (fn [x0 x1 x2] (callable f x0 x1 x2))
                      things0 things1 things2)
          b (ArrayList.)]
      (try
        (doseq [^Future future (.invokeAll pool tasks)] (.add b (.get future)))
        (finally (.shutdown pool)))
      (Collections/unmodifiableList b))))
;;------------------------------------------------------------------------------
#_(defn pmap
  "Eager version of <code>clojure.core/pmap</code> that uses as many threads as
   there are 
   <a href=\"https://docs.oracle.com/javase/8/docs/api/java/lang/Runtime.html#availableProcessors--\">
   available processors."
  (^Iterable [f things]
    (assert (ifn? f) (print-str "Not a function:" f))
    (nmap (.availableProcessors (Runtime/getRuntime)) f things))
  (^Iterable [f things0 things1]
    (assert (ifn? f) (print-str "Not a function:" f))
    (nmap (.availableProcessors (Runtime/getRuntime)) f things0 things1))
  (^Iterable [f things0 things1 things2]
    (assert (ifn? f) (print-str "Not a function:" f))
    (nmap (.availableProcessors (Runtime/getRuntime)) f
          things0 things1 things2)))
;;------------------------------------------------------------------------------
#_(defn nmap-indexed
  "A combination of [[nmap]] and [[map-indexed]]."
  (^Iterable [^long n f things]
    (assert (ifn? f) (print-str "Not a function:" f))
    (let [pool (Executors/newFixedThreadPool (int n))
          nthings (g/count things)
          tasks (map-indexed (fn [i x] (callable f i x)) things)
          b (ArrayList. nthings)]
      (try
        (doseq [^Future future (.invokeAll pool tasks)]
          (let [v (.get future)]
            (.add b v)))
        (finally (.shutdown pool)))
      (Collections/unmodifiableList b)))
  (^Iterable [^long n f things0 things1]
    (assert (ifn? f) (print-str "Not a function:" f))
    (let [pool (Executors/newFixedThreadPool (int n))
          tasks (map-indexed (fn [i x0 x1] (callable f i x0 x1)) 
                             things0 things1)
          b (ArrayList.)]
      (try
        (doseq [^Future future (.invokeAll pool tasks)] (.add b (.get future)))
        (finally (.shutdown pool)))
      (Collections/unmodifiableList b)))
  (^Iterable [n f things0 things1 things2]
    (assert (ifn? f) (print-str "Not a function:" f))
    (let [pool (Executors/newFixedThreadPool (int n))
          tasks (map-indexed (fn [i x0 x1 x2] (callable f i x0 x1 x2))
                             things0 things1 things2)
          b (ArrayList.)]
      (try
        (doseq [^Future future (.invokeAll pool tasks)] (.add b (.get future)))
        (finally (.shutdown pool)))
      (Collections/unmodifiableList b))))
;;------------------------------------------------------------------------------
#_(defn pmap-indexed
  "A combination of [[pmap]] and [[map-indexed]]."
  (^Iterable [f things]
    (assert (ifn? f) (print-str "Not a function:" f))
    (nmap-indexed (.availableProcessors (Runtime/getRuntime)) f things))
  (^Iterable [f things0 things1]
    (assert (ifn? f) (print-str "Not a function:" f))
    (nmap-indexed (.availableProcessors (Runtime/getRuntime)) f things0 things1))
  (^Iterable [f things0 things1 things2]
    (assert (ifn? f) (print-str "Not a function:" f))
    (nmap-indexed (.availableProcessors (Runtime/getRuntime)) f
                  things0 things1 things2)))
;;------------------------------------------------------------------------------
;; TODO: move somewhere more appropriate
;; This works for any Iterable, not just immutable lists.
;; Faster and less garbage than seq operation on iterator-seq on Iterable.
#_(defn mapc
  "Like [[map]] but purely for side effects; always returns <code>nil</code>."
  ([f things]
    (assert (ifn? f) (print-str "Not a function:" f))
    (let [i (g/iterator things)] (while (g/has-next? i) (f (g/next-item i))))
    nil)
  ([f things0 things1]
    (assert (ifn? f) (print-str "Not a function:" f))
    (let [i0 (g/iterator things0)
          i1 (g/iterator things1)]
      (while (and (g/has-next? i0) (g/has-next? i1))
        (f (g/next-item i0) (g/next-item i1))))
    nil))
;;------------------------------------------------------------------------------
#_(defn mapc-indexed 
  "Like [[map-indexed]] but purely for side effects; 
   always returns <code>nil</code>."
  ([f things]
    (assert (ifn? f) (print-str "Not a function:" f))
    (let [it (g/iterator things)]
      (loop [i (int 0)]
        (when (g/has-next? it)
          (f i (g/next-item it))
          (recur (inc i)))))
    nil)
  ([f things0 things1]
    (assert (ifn? f) (print-str "Not a function:" f))
    (let [i0 (g/iterator things0)
          i1 (g/iterator things1)]
      (loop [i (int 0)]
        (when (and (g/has-next? i0) (g/has-next? i1))
          (f i (g/next-item i0) (g/next-item i1))
          (recur (inc i)))))
    nil))
;;------------------------------------------------------------------------------
;; sorting
;;------------------------------------------------------------------------------
;; TODO: functions rather than singletons?
(def ^com.google.common.collect.Ordering natural-ordering
  (Ordering/natural))
(def ^com.google.common.collect.Ordering lexicographic-ordering
  (.lexicographical (Ordering/natural)))
(def ^com.google.common.collect.Ordering string-ordering
  (Ordering/usingToString))

#_(defn- compare
  (^long [^Ordering ordering this that] (.compare ordering this that))
  (^long [this that] (compare natural-ordering this that)))

(defn ^:no-doc lexicographical-compare ^long [this that]
  (.compare lexicographic-ordering this that))

(defn sort
  "Sort <code>things</code> according to an instance of 
   <code>com.google.common.collect.Ordering</code>,
   defaulting to natural ordering for <code>Comparable</code>s
   and lexicographc ordering of <code>toString</code> output otherwise."
  (^Iterable [^Ordering ordering ^Iterable things]
    (if (<= (g/count things) 1)
      things
      (.immutableSortedCopy ordering things)))
  (^Iterable [^Iterable things]
    (if (<= (g/count things) 1)
      things
      (if (g/every? #(instance? Comparable %) things)
        (sort natural-ordering things)
        (sort string-ordering things)))))

(defn sort-by
  "Sort <code>things</code> by the value of <code>f</code>,
   according to an instance of <code>com.google.common.collect.Ordering</code>,
   defaulting to natural ordering for <code>Comparable</code>s
   and lexicographc ordering of <code>toString</code> output otherwise."
  (^Iterable [^Ordering ordering f ^Iterable things]
    (assert (ifn? f) (print-str "Not a function:" f))
    (sort (.onResultOf ordering (function f)) things))
  (^Iterable [f ^Iterable things]
    (assert (ifn? f) (print-str "Not a function:" f))
    (sort-by natural-ordering f things)))
;;------------------------------------------------------------------------------
