(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-07-12"
      :doc "Field reflection, etc." }
    
    zana.data.reflect
  
  (:refer-clojure :exclude [accessor cast munge namespace type]))
;;------------------------------------------------------------------------------
;; marker interface for Datum classes
;;------------------------------------------------------------------------------
(definterface Datum)
(defn datum-class? [^Class c] (.isAssignableFrom zana.data.reflect.Datum c))
;;------------------------------------------------------------------------------
;; reflection on namespaces --- to get attribute functions
;;------------------------------------------------------------------------------
;; return a java package qualified version of s.
;; Typically for the case where s names a class defined with deftype, etc.

(defn munge ^clojure.lang.Symbol [^clojure.lang.Symbol s]
  (with-meta (symbol (str (namespace-munge *ns*) "." s)) (meta s)))
(defn edn-munge ^clojure.lang.Symbol [^clojure.lang.Symbol s]
  (with-meta (symbol (str (namespace-munge *ns*) "/" s)) (meta s)))
;;------------------------------------------------------------------------------
(defn constructor ^clojure.lang.Symbol [^clojure.lang.Symbol name] 
  (symbol (str (munge name) ".")))
;;------------------------------------------------------------------------------
;; Field spec is currently either:
;;
;; field
;;
;; or
;;
;; [field parser]
;;
;; where <parser> is a function that takes a Keyword -> String hashmap.

(defn extract-field ^clojure.lang.Symbol [field-spec]
  (if (sequential? field-spec)
    (with-meta (first field-spec) (meta field-spec))
    field-spec))
;;------------------------------------------------------------------------------
;; Assuming c is defined via deftype, etc., in a clojure namespace, attempt
;; to extract the namespace from the classname.
;; Notes:
;; (1) It appears that deftype classes do NOT have a package, just a long 
;; name including the namespace. May be different when explicitly AOT compiled.
;; (2) If we get a clojure defined class that does have a package, this may 
;; fail, because namespace-munge, which transforms the namespace name into a 
;; valid package name, doesn't have an inverse --- ie, there's no reliable way
;; to get the original namespace back from the package name, except in cases
;; where they are identical.

(defn namespace ^clojure.lang.Namespace [^Class c] 
  (let [cn (.getName c)
        rem (re-matches #"^([a-zA-Z0-9\.\-\_]+)\.[a-zA-Z0-9\.\-\_]+$" cn)
        pn (last rem)
        s (when pn (symbol pn))
        ns (when s (find-ns s))]
    (assert ns (pr-str "No namespace for" c))
    (assert (instance? clojure.lang.Namespace ns))
    ns))
;;------------------------------------------------------------------------------
(defn qualified-symbol ^clojure.lang.Symbol [^Class c s]
  (symbol (str (namespace c) "/" s)))
;;------------------------------------------------------------------------------
;; get the actual class for a type-hinted object.

(defn type ^Class [^clojure.lang.IMeta imeta]
  (let [^clojure.lang.Symbol tag (:tag (meta imeta))
        ^Class c (case tag
                   boolean Boolean/TYPE
                   byte Byte/TYPE
                   char Character/TYPE
                   double Double/TYPE
                   float Float/TYPE
                   short Short/TYPE
                   int Integer/TYPE
                   long Long/TYPE
                   (or (eval tag) java.lang.Object))]
    (assert c (binding [*print-meta* true] (pr-str "No class for" imeta)))
     c))
;;------------------------------------------------------------------------------
(defn accessor ^clojure.lang.Symbol [^clojure.lang.Symbol field] 
  (symbol (str "." field)))
;;------------------------------------------------------------------------------
;; reflection on java classes --- to get fields for IO
;;------------------------------------------------------------------------------
(defn primitive? [^Class c]
  (#{Boolean/TYPE Byte/TYPE Character/TYPE Double/TYPE Float/TYPE Short/TYPE
     Integer/TYPE Long/TYPE}
    c))
(defn- public? [^java.lang.reflect.Member m]
  (java.lang.reflect.Modifier/isPublic (.getModifiers m)))
(defn- local? [^java.lang.reflect.Member m]
  (not (java.lang.reflect.Modifier/isStatic (.getModifiers m))))
(defn- concrete? [^java.lang.reflect.Member m]
  (not (java.lang.reflect.Modifier/isAbstract (.getModifiers m))))
;;------------------------------------------------------------------------------
