(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-11-28"
      :doc "Generic functions to apply to functions." }
    
    zana.functions.generic
  
  (:refer-clojure :exclude [name range vector?])
  (:require [clojure.string :as s]
            [zana.commons.core :as commons]
            [zana.collections.generic :as g]
            [zana.collections.guava :as guava]
            [zana.collections.sets :as sets]
            [zana.geometry.r1 :as r1]
            [zana.geometry.z1 :as z1]
            [zana.stats.statistics :as stats])
  (:import [clojure.lang
            Fn IFn IFn$DD IFn$DL IFn$DO IFn$LD IFn$LL IFn$LO IFn$OD IFn$OL
            IMeta Named PersistentVector]))
;;------------------------------------------------------------------------------
#_(defprotocol Function)
;;------------------------------------------------------------------------------
;; domains: nil means 'unspecified' or "unimplemented", not the empty set or
;; anything like that.
;; TODO: explicit Unspecified domain?
;;------------------------------------------------------------------------------
;; A function should accept any element in its domain as an arg and return a
;; value in the codomain.
;; (TODO: what about singular functions?)
;; The support is where the function is 'really' defined, usually with some
;; default value everywhere else.
;; The range is a subset of the codomain --- the image of the domain.
;; Any or all of these may be nil (unspecified) for any function.
;;------------------------------------------------------------------------------
(defmulti domain class)
(defmethod domain :default [f] (:domain (meta f)))
(defmethod domain IFn$DD [^IFn$DD f] (or (:domain (meta f)) Double/TYPE))
(defmethod domain IFn$DL [^IFn$DL f] (or (:domain (meta f)) Double/TYPE))
(defmethod domain IFn$DO [^IFn$DO f] (or (:domain (meta f)) Double/TYPE))
(defmethod domain IFn$LD [^IFn$LD f] (or (:domain (meta f)) Long/TYPE))
(defmethod domain IFn$LL [^IFn$LL f] (or (:domain (meta f)) Long/TYPE))
(defmethod domain IFn$LO [^IFn$LO f] (or (:domain (meta f)) Long/TYPE))
(defmethod domain IFn$OD [^IFn$OD f] (or (:domain (meta f)) Object))
(defmethod domain IFn$OL [^IFn$OL f] (or (:domain (meta f)) Object))
(defmethod domain PersistentVector [PersistentVector f] [0 (g/count f)])
;;------------------------------------------------------------------------------
(def ^{:private true :tag java.util.Map}
     declared-values 
  (atom (java.util.IdentityHashMap.)))

(defn- -declared-value- 
  
  "If <code>f</code> is a function declared with a return type hint, return the 
   Class of the return type (default <code>Object</code> with no type hint)."
  
  ^Class [f]
  
  #_(println f)
  (assert (ifn? f))
  (let [[ns n] (s/split (pr-str (class f)) #"\$")
        s (symbol ns n)
        r (resolve s)
        m (meta r)
        a (first (:arglists m))
        tag (:tag (meta a))
        v (when tag (resolve tag))]
    (cond (instance? Class v) v
          (= #'clojure.core/double v) Double/TYPE
          (= #'clojure.core/long v) Long/TYPE
          :else Object)))

;; TODO: is this better than memoize?

(defn- update-declared-values [^java.util.Map m0 
                               ^clojure.lang.IFn f 
                               ^Class v]
  (let [m1 (java.util.IdentityHashMap. m0)]
    (.put m1 f v)
    m1))

(defn declared-value ^Class [f]
  (or (.get ^java.util.Map @declared-values f)
      (let [v (-declared-value- f)]
        (swap! declared-values update-declared-values f v)
        v)))
;;------------------------------------------------------------------------------
(defn enum-valued? 
  
  "Is <code>f</code> a function declared with a type hint, and is that type an
   Enum class?"
  
  ^Boolean [f] 
  
  (and (ifn? f) (.isEnum ^Class (declared-value f))))
;;------------------------------------------------------------------------------
(defmulti codomain class)
(defmethod codomain :default [f] (:codomain (meta f)))
(defmethod codomain IFn$DD [^IFn$DD f] (or (:codomain (meta f)) Double/TYPE))
(defmethod codomain IFn$LD [^IFn$LD f] (or (:codomain (meta f)) Double/TYPE))
(defmethod codomain IFn$OD [^IFn$OD f] (or (:codomain (meta f)) Double/TYPE))
(defmethod codomain IFn$DL [^IFn$DL f] (or (:codomain (meta f)) Long/TYPE))
(defmethod codomain IFn$LL [^IFn$LL f] (or (:codomain (meta f)) Long/TYPE))
(defmethod codomain IFn$OL [^IFn$OL f] (or (:codomain (meta f)) Long/TYPE))
(defmethod codomain IFn$DO [^IFn$DO f] (or (:codomain (meta f)) Object))
(defmethod codomain IFn$LO [^IFn$LO f] (or (:codomain (meta f)) Object))
(defmethod codomain PersistentVector [PersistentVector f] (guava/sort f))

(defmulti support class)
(defmethod support :default [f] (:support (meta f)))
(defmethod support PersistentVector [PersistentVector f]
  (z1/interval 0 (g/count f)))
(defmulti range
  (fn range-dispatch
    ([f] (class f))
    ([f things] [(class f) (class things)])))
(defmethod range :default [f] (:range (meta f)))
(defmethod range IFn [^IFn f] (:range (meta f)))
(defmethod range PersistentVector [PersistentVector f] f)
;;(defmethod range :default [f things] (mapv f things))
;; TODO: memoize?
;; TODO: <data-range> un-memoized version?
;; TODO: empty intervals?
(defmethod range [IFn Iterable] [^IFn f ^Iterable things]
  (when-not (g/empty? things)
    (let [values (sets/distinct f things)]
      (cond (g/every? float? values)
            (stats/bounds values)
            ;; (g/every? integer? values)
            ;; (let [rinterval (stats/bounds values)]
            ;;   (z1/interval (long (.z0 rinterval)) 
            ;;                (inc (long (.z1 rinterval)))))
            :else
            (guava/sort values)))))

(defmethod range [IFn$OD Iterable] [^IFn$OD f ^Iterable things]
  (when-not (g/empty? things)
    (let [i (g/iterator things)]
      (loop [ymin Double/POSITIVE_INFINITY
             ymax Double/NEGATIVE_INFINITY]
        (if (g/has-next? i)
          (let [y (.invokePrim f (g/next-item i))]
            (if (Double/isNaN y)
              (recur ymin ymax)
              (if (>= y ymin)
                (if (<= y ymax)
                  (recur ymin ymax)
                  (recur ymin y))
                (if (<= y ymax)
                  (recur y ymax)
                  (recur y y)))))
          ;; check for all NaN
          (when-not (and (== ymin Double/POSITIVE_INFINITY)
                         (== ymax Double/NEGATIVE_INFINITY))
            (r1/interval ymin (Math/nextUp ymax))))))))

(prefer-method range [IFn$OD Iterable] [IFn Iterable])

(defmethod range [IFn$OL Iterable] [^IFn$OL f ^Iterable things]
  (when-not (g/empty? things)
    (let [i (g/iterator things)]
      (loop [ymin Long/MAX_VALUE
             ymax Long/MIN_VALUE]
        (if (g/has-next? i)
          (let [y (.invokePrim f (g/next-item i))]
            (if (>= y ymin)
              (if (<= y ymax)
                (recur ymin ymax)
                (recur ymin y))
              (if (<= y ymax)
                (recur y ymax)
                (recur y y))))
          ;; half-open integer interval!
          (z1/interval ymin ymax))))))

(prefer-method range [IFn$OL Iterable] [IFn Iterable])
;;------------------------------------------------------------------------------
#_(defmulti contains? 
  (fn contains?-dispatch [space thing] [(class space) (class thing)]))
;;------------------------------------------------------------------------------
;; add explicit domain, codomain, support, and/or range to exsisting function
;; TODO: test for valid restriction of existing domain, etc.
;;------------------------------------------------------------------------------
(defrecord Restriction [^IFn f
                        domain
                        codomain
                        support
                        range]
  Fn
  IFn (invoke [this arg] (f arg)))
(defmethod commons/name Restriction [^Restriction rf] (commons/name (.f rf)))
(defmethod domain Restriction [^Restriction rf]
  (or (.domain rf) (domain (.f rf))))
(defmethod codomain Restriction [^Restriction rf]
  (or (.codomain rf) (codomain (.f rf))))
(defmethod support Restriction [^Restriction rf]
  (or (.support rf) (support (.f rf))))
(defmethod range Restriction [^Restriction rf]
  (or (.range rf) (range (.f rf))))
;;------------------------------------------------------------------------------
(defrecord RestrictionOD [^IFn$OD f
                          domain
                          codomain
                          support
                          range]
  Fn
  IFn (invoke [this arg] (f arg))
  IFn$OD (invokePrim [this x] (.invokePrim f x)))
(defmethod commons/name RestrictionOD [^RestrictionOD rf] 
  (commons/name (.f rf)))
(defmethod domain RestrictionOD [^RestrictionOD rf]
  (or (.domain rf) (domain (.f rf))))
(defmethod codomain RestrictionOD [^RestrictionOD rf]
  (or (.codomain rf) (codomain (.f rf))))
(defmethod support RestrictionOD [^RestrictionOD rf]
  (or (.support rf) (support (.f rf))))
(defmethod range RestrictionOD [^RestrictionOD rf]
  (or (.range rf) (range (.f rf))))
;;------------------------------------------------------------------------------
(defrecord RestrictionOL [^IFn$OL f
                          domain
                          codomain
                          support
                          range]
  Fn
  IFn (invoke [this arg] (f arg))
  IFn$OL (invokePrim [this x] (.invokePrim f x)))
(defmethod commons/name RestrictionOL [^RestrictionOL rf] 
  (commons/name (.f rf)))
(defmethod domain RestrictionOL [^RestrictionOL rf]
  (or (.domain rf) (domain (.f rf))))
(defmethod codomain RestrictionOL [^RestrictionOL rf]
  (or (.codomain rf) (codomain (.f rf))))
(defmethod support RestrictionOL [^RestrictionOL rf]
  (or (.support rf) (support (.f rf))))
(defmethod range RestrictionOL [^RestrictionOL rf]
  (or (.range rf) (range (.f rf))))
;;------------------------------------------------------------------------------
(defmulti restrict
  (fn dispatch-restrict [f & {:keys [domain codomain support range]}]
    (class f)))
(defmethod restrict IFn [f & {:keys [domain codomain support range]}]
  (Restriction. f domain codomain support range))
(defmethod restrict IFn$OD [f & {:keys [domain codomain support range]}]
  (RestrictionOD. f domain codomain support range))
(prefer-method restrict IFn$OD IFn)
(defmethod restrict IFn$OL [f & {:keys [domain codomain support range]}]
  (RestrictionOL. f domain codomain support range))
(prefer-method restrict IFn$OL IFn)
;;------------------------------------------------------------------------------
;; domain classfication
;;------------------------------------------------------------------------------
(defn- continuous-value? [x]
  (or (instance? Double x)
      (instance? Float x)))
(defn- integral-value? [x]
  (or (instance? Long x)
      (instance? Integer x)
      (instance? Short x)
      (instance? Byte x)))
(defn- ordinal-value? [x] (instance? Comparable x))
;;------------------------------------------------------------------------------
(defn continuous-domain? [d]
  (or (= Double/TYPE d)
      (= Float/TYPE d)
      (r1/interval? d)))
;;------------------------------------------------------------------------------
;; TODO: boolean?
(defn integral-domain? [d]
  (or (= Long/TYPE d)
      (= Integer/TYPE d)
      (= Short/TYPE d)
      (= Byte/TYPE d)
      (z1/interval? d)))
;;------------------------------------------------------------------------------
;; TODO: boolean?
(defn ordinal-domain? [d]
  (or (and (class? d) (commons/descendant? Comparable d))
      (and (instance? Iterable d) (every? #(instance? Comparable %) d))
      (and (commons/object-array? d) (ordinal-domain? (seq d)))))
;;------------------------------------------------------------------------------
;; The cuts partition the support (and maybe define it).
(defprotocol CutFunction (cuts [this]))
;;------------------------------------------------------------------------------
;; attribute functions
;;------------------------------------------------------------------------------
;; TODO: separate categorical, ordinal, integral, and continuous
;; --- and interval
(defn numerical?
  ([^IFn f]
    (or (instance? IFn$OD f)
        (instance? IFn$OL f)
        (continuous-domain? (codomain f))
        (integral-domain? (codomain f))))
  ([^IFn f ^Iterable data]
    (try
      (or (numerical? f)
          (g/every? #(number? (.invoke f %)) data))
      (catch Throwable t
        (throw
          (RuntimeException.
            (print-str "numerical?" f (class data) (g/count data))
            t))))))

(let [dclass (class (double-array 0))
      fclass (class (float-array 0))]
  (defn vector?
    ([^IFn f]
      (let [decl (declared-value f)]
        (or (= decl dclass) (= decl fclass))))
    ([^IFn f ^Iterable data]
      (g/every? (fn [datum]
                  (let [z (f datum)]
                    (or (instance? dclass z) (instance? fclass z))))
                data))))

(defn interval?
  ([^IFn f ^Iterable data]
    (g/every? (fn [datum]
                (let [z (f datum)]
                  (or (instance? zana.java.geometry.r1.Interval z)
                      (instance? zana.java.geometry.z1.Interval z))))
              data)))

;; TODO: not right --- they need to be comparable to each other
(defn ordinal?
  ([^IFn f ^Iterable data] (g/every? #(instance? Comparable (f %)) data)))
;;------------------------------------------------------------------------------