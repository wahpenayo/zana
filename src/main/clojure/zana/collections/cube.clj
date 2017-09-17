(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-07-12"
      :doc "Something like a data cube" }
    
    zana.collections.cube
  
  (:refer-clojure :exclude [range])
  (:require [clojure.pprint :as pp]
            [zana.commons.core :as cc]
            [zana.collections.generic :as generic]
            [zana.collections.maps :as maps]
            [zana.collections.sets :as sets]))
;;------------------------------------------------------------------------------
(deftype Cube [;; functions corresponding to axes of the data cube.
               ;; TODO: ImmutableSet?
               ^java.util.Collection attributes 
               ;; TODO: ImmutableMap?
               ^java.util.Map ranges 
               ;; key is {attribute0 value0 attribute1 value1 ...}
               ;; with entry for every attribute
               ;; value is a collection or the records that match the key
               ;; TODO: ImmutableMap?
               ^java.util.Map data]
  java.util.Map ;; TODO: ImmutableMap?
  (containsKey [this k] (.containsKey data k))
  (entrySet [this] (.entrySet data))
  (get [this k] 
    (assert (= (.keySet ^java.util.Map k) (.keySet ^java.util.Map ranges))
            (print-str "k:\n" (cc/pprint-str k) 
                       "ranges:\n" (cc/pprint-str ranges))) 
    (.get data k))
  (isEmpty [this] (.isEmpty data))
  (keySet [this] (.keySet data))
  (values [this] (.values data))
  (size [this] (.size data))
  Object ;; identity semantics
  (hashCode [this] (System/identityHashCode this))
  (equals [this that] (identical? this that))
  (toString [this] (cc/pprint-str {:attributes attributes
                                   :ranges ranges
                                   :data (into {} data)})))
;;------------------------------------------------------------------------------
(defmethod print-method Cube [^Cube this ^java.io.Writer w]
  (.write w (.toString this)))
;;------------------------------------------------------------------------------
(defn cube? 
  "Is this a data cube?"
  [x] (instance? Cube x))
(defn- data ^java.util.Map [^Cube cube] (.data cube))
(defn attributes 
  "Return a collection of the attribute functions used to define the axes of 
   this cube."
  ^java.util.Collection [^Cube cube] (.attributes cube))
(defn- ranges ^java.util.Map [^Cube cube] (.ranges cube))
(defn has-attribute? 
  "Is <code>attribute</code> one of the functions used to define the axes of the
   <code>cube</code>?" 
  [^Cube cube attribute]
  (.containsKey (ranges cube) attribute))
(defn range 
  "Return a set of the distinct values of <code>attribute</code> over the 
   original data used to construct the cube. Throw an exception if 
   <code>attribute</code> isn't one of the cube attributes."
  [^Cube cube attribute]
  (assert (has-attribute? cube attribute)
          (print-str "Not an attribute of the Cube:\n" cube))
  (.get (ranges cube) attribute))
;;------------------------------------------------------------------------------
(defn- key-function [attributes] 
  (fn [datum] (into {} (map (fn [z] [z (z datum)]) attributes))))
;;------------------------------------------------------------------------------
(defn cube 
  "Construct a data cube, indexing the elements of <code>data</code> on the 
   values of the <code>attributes</code>.<br> 
   Each attribute function should have a small number of distinct values over
   <code>data</code>.<br>
   Basically a multi-key multi-map with special keys: 
   <code>{attribute0 value0 attribute1 value1 ...}</code>.
   Data cubes are maps, <code>get</code> returns a list of the data elements
   that match a full key (a map of attribute-value pairs for all the attributes). 
   <br>In addition, data cubes offer a [[slice]] operation,
   which takes a partial key (a map of attribute-value pairs for a subset of the
   attributes) and returns a sub-cube."
  ^zana.collections.cube.Cube [attributes data]
  (assert (every? ifn? attributes))
  (let [ranges (into {} (map (fn [f] [f (sets/distinct f data)]) attributes))
        indexed (maps/group-by (key-function attributes) data)]
    (Cube. attributes ranges indexed)))
;;------------------------------------------------------------------------------
;; Note: attributes are no longer functions of the elements of the cube
(defmethod generic/map 
  [clojure.lang.IFn Cube] 
  [^clojure.lang.IFn f ^Cube cube]
  (let [d (generic/map f (data cube))]
    (Cube. (attributes cube) (ranges cube) d)))
;;------------------------------------------------------------------------------
;; TODO: reset the ranges? remove single value attributes?
(defmethod generic/filter
  [clojure.lang.IFn Cube] 
  [^clojure.lang.IFn f ^Cube cube]
  (let [d (generic/filter f (data cube))]
    (Cube. (attributes cube) (ranges cube) d)))
;;------------------------------------------------------------------------------
(defn- match? [partial-key whole-key]
  (let [it (generic/iterator partial-key)]
    (loop []
      (if-not (generic/has-next? it)
        true
        (let [[k v] (generic/next-item it)]
          (if-not (= v (generic/get whole-key k))
            false
            (recur)))))))

(defn slice 
  "Return a data cube with the same attributes and attributes ranges (the same
   axes) but with only the data corresponding to the <code>partial-key</code>. 
   The resulting cube will have the same cells as the original, but the one that 
   don't match the <code>partial-key</code> will be 
  empty."
  ^zana.collections.cube.Cube [partial-key ^Cube cube]
  (generic/filter (fn [k v] (match? partial-key k)) cube))
;;------------------------------------------------------------------------------
;; TODO: is this better than taking the union of all the data and re-cubing?
;; TODO: as a generic partial reduction
#_(defn project ^zana.collections.cube.Cube [^Cube cube onto]
  )
;;------------------------------------------------------------------------------
