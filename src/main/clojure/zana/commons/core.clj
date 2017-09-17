(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-10-19"
      :doc "Things that ought to be in clojure.core, and don't have an
            obvious place elsewhere in Zana." }
    
    zana.commons.core
  
  (:refer-clojure :exclude [contains? name time])
  (:require [clojure.pprint :as pp]
            [clojure.string :as s])
  (:import [java.util Collection HashMap Iterator List Map]
           [java.time LocalDateTime]
           [java.time.format DateTimeFormatter]
           [com.google.common.collect Multimap]))
;;------------------------------------------------------------------------------
(defn jvm-args 
  "Return the command line arguments passed to java. Useful for logging."
  []
  (.getInputArguments 
    (java.lang.management.ManagementFactory/getRuntimeMXBean)))
;;------------------------------------------------------------------------------
;; Typical use: (binding-var-root [*out* a-print-writer] multi-threaded-stuff)
(defmacro binding-var-root 
  
  "A macro wrapping <a href=\"http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/alter-var-root\">
   alter-var-root</a>, used to temporarily alter the value of a dynamic global
   var in all threads. Use with caution.<br>
   Contrast with 
   <a href=\"http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/binding\">
   binding</a>, which only affects the current thread."
  
  [[sym new-value] & body]
  
  `(let [old# ~sym
         new# ~new-value]
     (alter-var-root (var ~sym) (fn [v#] new#))
     (let [ret# (do ~@body)]
       (alter-var-root (var ~sym) (fn [v#] old#))
       ret#)))
;;------------------------------------------------------------------------------
(defn watchable? [x] (instance? clojure.lang.IRef x))
(defn watches ^java.util.Map [^clojure.lang.IRef watchable]
  (.getWatches watchable))
(defn validator [^clojure.lang.IRef watchable]
  (.getValidator watchable))
;;------------------------------------------------------------------------------
(defn agent? "" [x] (instance? clojure.lang.Agent x))
(defn atom? "" [x] (instance? clojure.lang.Atom x))
(defn ref? "" [x] (instance? clojure.lang.Ref x))
;;(defn var? [x] (instance? clojure.lang.Var x))
;;------------------------------------------------------------------------------
(defn array? "" [x] (and x (.isArray ^Class (class x))))
(let [char-array-type (type (char-array 0))]
  (defn char-array? "" [x] (instance? char-array-type x)))
(let [byte-array-type (type (byte-array 0))]
  (defn byte-array? "" [x] (instance? byte-array-type x)))
(let [short-array-type (type (short-array 0))]
  (defn short-array? "" [x] (instance? short-array-type x)))
(let [int-array-type (type (int-array 0))]
  (defn int-array? "" [x] (instance? int-array-type x)))
(let [long-array-type (type (long-array 0))]
  (defn long-array? "" [x] (instance? long-array-type x)))
(let [float-array-type (type (float-array 0))]
  (defn float-array? "" [x] (instance? float-array-type x)))
(let [double-array-type (type (double-array 0))]
  (defn double-array? "" [x] (instance? double-array-type x)))
(let [object-array-type (type (object-array 0))]
  (defn object-array? "" [x] (instance? object-array-type x)))
;;------------------------------------------------------------------------------
(defn ^:no-doc descendant? [parent child]
  (and parent child (.isAssignableFrom ^Class parent ^Class child)))
;;------------------------------------------------------------------------------
;; specialized functions for cleaning postcodes
(defn ^:no-doc digits ^String [^String s]
  (let [n (.length s)
        c (char-array n)]
    (loop [i 0
           j 0]
      (if (< j n)
        (let [sj (.charAt s j)]
          (if (Character/isDigit sj)
            (do
              (aset c i sj)
              (recur (inc i) (inc j)))
            (recur i (inc j))))
        (String. c 0 i)))))
;;------------------------------------------------------------------------------
(defn ^:no-doc letters-and-digits ^String [^String s]
  (let [n (.length s)
        c (char-array n)]
    (loop [i 0
           j 0]
      (if (< j n)
        (let [sj (.charAt s j)]
          (if (Character/isLetterOrDigit sj)
            (do
              (aset c i sj)
              (recur (inc i) (inc j)))
            (recur i (inc j))))
        (String. c 0 i)))))
;;------------------------------------------------------------------------------
(defn ^:no-doc letters ^String [^String s]
  (let [n (.length s)
        c (char-array n)]
    (loop [i 0
           j 0]
      (if (< j n)
        (let [sj (.charAt s j)]
          (if (Character/isLetter sj)
            (do
              (aset c i sj)
              (recur (inc i) (inc j)))
            (recur i (inc j))))
        (String. c 0 i)))))
;;------------------------------------------------------------------------------
(defn skewer 
  "camelCase to lisp-case."
  ^String [s] (s/lower-case (s/join "-" (s/split s #"(?=[A-Z])"))))
;;------------------------------------------------------------------------------
(defn safe
  "Convert <code>s</code> to something that's safe for, eg, 
   <code>(symbol s)</code>. Replace the bad characters with <code>c</code>, 
   which defaults to \"-\"."
  (^String [^String s ^String c] 
    (if (nil? s)
      "nil"
      (s/replace s #"[^A-Za-z0-9\_\+\-\*\!\?]+" c)))
  (^String [^String s] (safe s "-")))
;;------------------------------------------------------------------------------
(defn ^:no-doc gen-keyword 
  "Like <code>gensym</code> but for keywords."
  [s]
  (keyword
    (if (empty? s)
      (gensym)
      (gensym (safe (str s "-")) ))))
;;------------------------------------------------------------------------------
;; try to find a reasonable name for any object
;;------------------------------------------------------------------------------
(defn- fn-name [^clojure.lang.Fn f]
  (let [strf (str f)]
    (if (.startsWith ^String strf "clojure.core$constantly$fn")
      (str "(constantly " (print-str (f nil)) ")")
      (let [fname (s/replace strf #"^(.+)\$([^@]+)(|@.+)$" "$2")
            fname (s/replace fname \_ \-)
            fname (s/replace fname #"--\d+$" "")]
        fname))))
;;------------------------------------------------------------------------------
;; Search for a Var whose value is equal to v.
(defn- find-binding [v]
  (ffirst (filter #(= v (var-get (second %))) (mapcat ns-publics (all-ns)))))
(defn- binding-name [x] 
  (if-let [sym (find-binding x)] (str sym) ""))
;;------------------------------------------------------------------------------
(defmulti ^String name 
  "Try to find a reasonable name string for <code>x</code>.<br> 
   Try harder than <code>clojure.core/name</code>."
  (fn dispatch-name [x] (class x)))

(defmethod name :default [x] (binding-name x))

(defmethod name nil [x] nil)
(defmethod name String [^String x] x)
(defmethod name Class [^Class x] (.getSimpleName x))
(defmethod name java.io.File [^java.io.File x] (.getName x))

(defmethod name clojure.lang.Namespace [x] (str x))

(defmethod name java.util.Collection [^java.util.Collection x] 
  (binding-name x))

(defmethod name clojure.lang.Fn [^clojure.lang.Fn x] 
  (let [mn (:name (meta x)) 
        bn (binding-name x)
        fn (fn-name x)]
    (cond (not (empty? mn)) mn
          (not (empty? bn)) bn
          (not (empty? fn)) fn
          :else "")))

(defmethod name clojure.lang.IMeta [^clojure.lang.IMeta x] 
  (or (:name (meta x)) (binding-name x)))
(prefer-method name clojure.lang.IMeta java.util.Collection)
(prefer-method name clojure.lang.Fn clojure.lang.IMeta)

(defmethod name clojure.lang.Named [x] (clojure.core/name x))
(prefer-method name clojure.lang.Named java.util.Collection)
(prefer-method name clojure.lang.Named clojure.lang.Fn)
(prefer-method name clojure.lang.Named clojure.lang.IMeta)
;;------------------------------------------------------------------------------
;; timing
;;------------------------------------------------------------------------------
;; like clojure.core.time, prefixes results with a message
(defmacro time
  "Evaluates expr and prints the time it took.  Returns the value of expr."
  ([msg expr]
    `(let [start# (System/nanoTime)
           ret# ~expr
           end# (System/nanoTime)
           msec# (/ (Math/round (/ (double (- end# start#)) 10000.0)) 100.0)]
       (println ~msg (float msec#) "ms")
       ret#))
  ([expr] `(time (str (quote ~@expr)) ~expr)))
;; like clojure.core.time, but reports results rounded to seconds and minutes
(defmacro seconds
  "Evaluates expr and prints the time it took.  Returns the value of expr."
  ([msg & exprs]
    (let [expr `(do ~@exprs)]
      `(let [
             ^DateTimeFormatter fmt#
             (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss")]
         (println ~msg (.format fmt# (LocalDateTime/now)))
         (let [start# (System/nanoTime)
               ret# ~expr
               end# (System/nanoTime)
               msec# (/ (double (- end# start#)) 1000000.0)
               sec# (/ msec# 1000.0)
               min# (/ sec# 60.0)]
           (println ~msg (.format fmt# (LocalDateTime/now))
                    (str "(" (int (Math/round msec#)) "ms)"
                         " ("(int (Math/round min#))  "m) "
                         (int (Math/round sec#)) "s"))
           ret#))))
  ([exprs] `(seconds "" ~@exprs)))
;;------------------------------------------------------------------------------
(defn print-stack-trace
  ([] (.printStackTrace (Throwable.)))
  ([& args]
    (let [^String msg (s/join " " args)]
      (.printStackTrace (Throwable. msg)))))
;;------------------------------------------------------------------------------
(defmacro echo 
  "Print the expressions followed by their values. Useful for quick logging."
  [& exps]
  (let [qexprs (mapv str exps)]
    `(println ~@qexprs ~@(mapv (fn [expr] `(print-str ~expr)) exps))))
;;------------------------------------------------------------------------------
(defn pprint-str
  "Pretty print <code>x</code> without getting carried away..."
  ([x level depth]
    (binding [*print-length* level
              *print-level* depth
              pp/*print-right-margin* 160
              pp/*print-miser-width* 128
              pp/*print-suppress-namespaces* true]
      (with-out-str (pp/pprint x))))
  ([x] (pprint-str x 10 8)))
;;------------------------------------------------------------------------------ 
(defn- abbreviate [v]
  (cond (instance? List v) (concat 
                                [(class v) (.size ^List v)] 
                               (when-not (empty? v)[(.get ^List v 0) "..."]))
        (instance? Collection v) [(class v) (.size ^Collection v)]
        (instance? Iterable v) (class v)
        (keyword? v) v
        (ifn? v) (class v)
        (nil? v) nil
        :else (.toString ^Object v)))
;;------------------------------------------------------------------------------ 
(defn- abbreviate-keyvalue [[k v]] [k (abbreviate v)])
;;------------------------------------------------------------------------------
(defn pprint-map-str
  "Convert a map to a readable string, abbreviating large values."
  ^String [m]
  (cond (nil? m) "nil"
        (instance? java.util.List m) 
        (pprint-str (map abbreviate m) false false)
        
        (instance? Map m) 
        (pprint-str (into (sorted-map) (map abbreviate-keyvalue m)) false false)
        
        (instance? Multimap m) 
        (pprint-map-str (.asMap ^Multimap m))
        
        :else
        (throw (IllegalArgumentException. (print-str "can't handle" (class m)))))) 
;;------------------------------------------------------------------------------
(defn ordered?
  "Test whether Comparable items are ordered non-decreasing."
  ([a & items]
    (if (empty? items)
      true
      (let [b (first items)]
        (and (<= (compare a b) 0)
             (recur b (next items))))))
  ([] true))
;;------------------------------------------------------------------------------
(defn make-archetyper
  "Return a closure containing a hashmap used to de-dupe its argument."
  ([]
    (let [canon (HashMap.)]
      (fn [item]
        (when item
          (or (.get ^HashMap canon item)
              (.put ^HashMap canon item item)
              item)))))
  ([n]
    (let [canon (HashMap. (int n))]
      (fn [item]
        (when item
          (or (.get ^HashMap canon item)
              (.put ^HashMap canon item item)
              item))))))
;;------------------------------------------------------------------------------
(defn ^:no-doc make-labeler []
  (let [hm (HashMap.)]
    (fn labeler
      ([k]   (.get hm k))
      ([k v] (.put hm k v)))))
;;------------------------------------------------------------------------------
