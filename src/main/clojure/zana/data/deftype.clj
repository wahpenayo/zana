(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-09-06"
      :doc "Data definition macro utilities." }

    zana.data.deftype
  
  (:require [clojure.string :as s]
            [zana.commons.core :as cc]
            [zana.io.gz :as gz]
            [zana.collections.generic :as g]
            [zana.data.reflect :as r]))
;;------------------------------------------------------------------------------
(defn- constructor-expression [name k v field-names]
  `(new ~name ~@(mapv (fn [f] (if (= k (keyword f)) v f)) field-names)))
;;------------------------------------------------------------------------------
(defn- updater-name [x] (symbol (str "update-" (name x))))

(defn- updater-value-cast [^clojure.lang.IMeta value-symbol tag]
  (case (symbol tag)
    (boolean byte char double float int long short) `(~tag ~value-symbol)
    value-symbol))

(defn- updater-defn [classname fields field]
  (let [class-meta {:tag (r/munge classname)}
        this (with-meta (gensym "this") class-meta)
        field-tag (:tag (meta field))
        field-type (r/type field)
        field-type-symbol (symbol (.getName field-type))
        new-value-type (case field-type-symbol
                         boolean 'Boolean
                         char 'Character
                         (byte short int long) 'long
                         (float double) 'double
                         field-type-symbol)
        new-value (gensym "new-value")
        args (with-meta [this (with-meta new-value {:tag new-value-type})]
               class-meta)
        constructor-args (map #(if (= field %) 
                                 (updater-value-cast new-value field-tag)
                                 `(~(symbol (str "." %)) ~this))
                              fields)]
    `(defn ~(with-meta (updater-name field) {:no-doc true}) 
       ~args
       (~(r/constructor classname) ~@constructor-args))))

;;(defn- updater-call [classname field]
;;  `(~'toString [~this] (str ~(str "#" (r/munge name)) (into {} ~this))))

(defn- emit-assoc [keys this key val]
  (let [updater (gensym "update")]
  `(~'assoc 
     [~this ~key ~val]
     (assert (~(set keys) ~key) (str "Invalid key: " ~key))
     (let [~updater (case 
                      ~key
                    ~@(interleave keys (mapv updater-name keys)))]
       (~updater ~this ~val)))))
;;------------------------------------------------------------------------------
;; edn compatible
(defn- emit-toString [name this]
  (let [m (gensym "m")]
    `(~'toString 
       [~this] 
       ;; do we really want (remove #(nil? (second %)) ...) instead of second?
       (let [~m (into {} (filter second ~this))] 
         (str ~(str "#" (r/edn-munge name)) " " 
              (s/replace (str ~m) "," ""))))))
;;------------------------------------------------------------------------------
;; Datums use identity (boson) semantics: two datum objects are different, even
;; if they have the same values for all fields.
;; TODO: JSON IO
(defn emit [name field-specs]
  (let [fields (mapv r/extract-field field-specs)
        field-names (mapv #(with-meta % nil) fields)
        keys (mapv keyword fields)
        this (gensym "this")
        that (gensym "that")
        key (gensym "key")
        val (gensym "val")
        default (gensym "default")]
    `((require '[zana.api])
       (declare ~@(mapv updater-name field-names))
       (deftype
         ~name ~fields
         zana.data.reflect.Datum ;; marker interface
         java.io.Serializable
         Iterable
         (~'iterator
           [~this]
           (zana.api/iterator
             ~(mapv (fn [field] 
                      `(clojure.lang.MapEntry. 
                        ~(keyword field) ~(with-meta field nil))) 
                    fields)))
         clojure.lang.IPersistentMap
         ;; TODO: convert to a different representaton?
         (~'assocEx [~this ~key ~val]
                    (throw (UnsupportedOperationException.
                             (str ~this " is <really> immutable."))))
         (~'without [~this ~key]
                    (throw (UnsupportedOperationException.
                             (str ~this " is <really> immutable."))))
         clojure.lang.Counted
         clojure.lang.IPersistentCollection
         (~'count [~this] ~(count fields))
         ;; TODO: convert to a different representaton?
         (~'cons [~this ~val]
                 (throw (UnsupportedOperationException.
                          (str ~this " is <really> immutable."))))
         (~'equiv [~this ~that] (.equals ~this ~that))
         clojure.lang.Seqable
         (~'seq [~this] (iterator-seq (.iterator ~this)))
         clojure.lang.ILookup
         (~'valAt [~this ~key ~default]
                  (case ~key ~@(interleave keys field-names) ~default))
         (~'valAt [~this ~key] (.valAt ~this ~key nil))
         clojure.lang.Associative
         (~'containsKey [~this ~key] (~(set keys) ~key))
         (~'entryAt [~this ~key]
                    (case ~key
                      ~@(interleave
                          keys
                          (mapv (fn [k f] `(clojure.lang.MapEntry. ~k ~f))
                                keys field-names))))
;         ;; TODO: figure out how to apply a constructor to an arglist
;         (~'assoc [~this ~key ~val]
;                  (assert (~(set keys) ~key) (str "Invalid key: " ~key))
;                  (case 
;                    ~key
;                    ~@(interleave
;                        keys
;                        (mapv #(constructor-expression name % val field-names)
;                              keys))))
         ~(emit-assoc keys this key val)
         Object
         ~(emit-toString name this)
         (~'hashCode [~this] (System/identityHashCode ~this))
         (~'equals [~this ~that] (identical? ~this ~that)))
       ~@(map #(updater-defn name fields %) fields))))
;;------------------------------------------------------------------------------
(defn map-factory [classname fields]
  (let [m (with-meta (gensym "m") {:tag java.util.Map})
        field-lookup (fn [field]
                       (let [type-symbol (:tag (meta field))
                             lookup `(~(keyword field) ~m)]
                         (case type-symbol
                           (boolean byte short int long float double char)
                           `(~type-symbol ~lookup)
                           lookup)))]
    `(defn
       ~(symbol (str "map->" classname))
       ~(str "Like <code>defrecord</code>, map-based factory for " classname)
       ~(with-meta [m] {:tag (r/munge classname)})
       (~(r/constructor classname) ~@(mapv field-lookup fields)))))
;;------------------------------------------------------------------------------
