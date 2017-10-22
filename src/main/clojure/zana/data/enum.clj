(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2017-10-21"
      :doc "Syntatic sugar for categorical 'enum' type definition.
            Note: one per namespace; some names (eg 'singleton') are reserved.
            Best practice is one enum and a small number of related functions
            in the namespace." }
  
    zana.data.enum
  
  (:require [clojure.string :as s]
            [zana.commons.core :as cc]))
;;------------------------------------------------------------------------------
;; A marker interface
;; TODO: defprotocol instead?
;; TODO: values method, etc?
 ;; conflict with java.lang.Enum
(definterface Enumb
  (readResolve []))
(defn ^:no-doc enum? [x]
  (or (instance? zana.data.enum.Enumb x)
      (cc/descendant? Enumb x)))
;;------------------------------------------------------------------------------
;; camelCase to lisp-case
(defn- skewer ^String [s] (s/lower-case (s/join "-" (s/split s #"(?=[A-Z])"))))
(defn- safe ^String [^String s]
  (s/replace (str s) #"[^A-Za-z0-9\_\+\-\*\!\?]{1}" ""))
(defn- safe-keyword ^clojure.lang.Keyword [s]
  (if (instance? clojure.lang.Keyword s) s (keyword (safe s))))
(defn- namespace-symbol [ename]
  (symbol (str (namespace-munge *ns*) "." ename)))
(defn- accessor-name [ename] (symbol (str "." ename)))
(defn- access [ename field arg]
  `(~(accessor-name field) ~(with-meta arg {:tag ename})))
(defn- constructor [ename] (symbol (str ename ".")))
;;------------------------------------------------------------------------------
;; TODO: binary IO
;; TODO: JSON IO
;; TODO: tsv parsing, with and without header
;; TODO: faster if values are Keywords rather than Strings?
(defmacro ^:no-doc ordered [ename [& values]]
  (let [cname (namespace-symbol ename)
        label (with-meta (gensym "label") {:tag String})
        rank (gensym "rank")
        this (gensym "this")
        that (gensym "that")
        hinted-that (with-meta that {:tag cname})
        writer (with-meta (gensym "w") {:tag 'java.io.Writer})
        this-str (with-meta `(str ~this) {:tag 'String})
        singletons (symbol "singletons")
        lookup (symbol "singleton")
        k (gensym "k")
        v (gensym "v")]
    `(let []
       (declare ~lookup)
       (deftype ~ename [~(with-meta rank {:tag 'int}) ~label]
         Enumb
         (readResolve [~this] (~lookup ~label))
         java.io.Serializable
         Object
         (toString [~this] (name ~label))
         (hashCode [~this]
           (unchecked-add-int (hash ~label) (unchecked-multiply-int ~rank 37)))
         (equals [~this ~that]
           (or (identical? ~this ~that)
               (and (instance? ~ename ~that)
                    (== ~rank ~(access ename rank that))
                    (= ~label ~(access ename label that)))))
         clojure.lang.Named
         (getName [~this] (name ~label))
         Comparable
         (compareTo [~this ~that] (- ~rank ~(access ename rank that))))
       (def ~singletons
         (into
           {}
           (map-indexed
             (fn [~rank ~label] [~label (~(constructor cname) ~rank ~label)])
             [~@values])))
       (defn ~lookup [~k]
         (let [~v (~singletons ~k)]
           (assert ~v (str ~(str "no " ename " for ") ~k))
           ~v))
       ;; Don't show the enum name in default printing
       (defmethod clojure.core/print-method ~cname [~this ~writer]
         (.write ~writer ~this-str)))))
;;------------------------------------------------------------------------------
(defmacro ^:no-doc unordered [ename [& values]]
  (let [cname (namespace-symbol ename)
        label (with-meta (gensym "label") {:tag String})
        this (gensym "this")
        that (gensym "that")
        hinted-that (with-meta that {:tag cname})
        singletons (symbol "singletons")
        lookup (symbol "singleton")
        k (gensym "k")
        v (gensym "v")]
    `(let []
       (declare ~lookup)
       (deftype ~ename [~label]
         Enumb
         (readResolve [~this] (~lookup ~label))
         java.io.Serializable
         Object
         (toString [~this] (name ~label))
         (hashCode [~this] (hash ~label))
         (equals [~this ~that]
           (or (identical? ~this ~that)
               (and (instance? ~ename ~that)
                    (= ~label ~(access ename label that)))))
         clojure.lang.Named (getName [~this] (name ~label)))
       (refer-clojure :exclude [~'get])
       (def ~singletons
         (into {} (map (fn [~label] [~label (~(constructor cname) ~label)])
                       [~@values])))
       (defn ~lookup [~k]
         (let [~v (~singletons ~k)]
           (assert ~v (str ~(str "no " ename " for ") ~k))
           ~v)))))
;;------------------------------------------------------------------------------