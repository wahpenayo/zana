(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-11-09"
      :doc "Return nested maps and sequences isomorphic to nested objects." }
    
    zana.collections.clojurize
  
  (:import [java.util Arrays]
           [com.google.common.primitives 
            Booleans Bytes Chars Doubles Floats Ints Longs Shorts]))
;;------------------------------------------------------------------------------
(defmulti clojurize
  "Return nested maps and sequences isomorphic to the input object."
  class)
;;------------------------------------------------------------------------------
(defmethod clojurize nil [_] nil)
(defmethod clojurize Number [x] x)
(defmethod clojurize Enum [^Enum x] (.name x))
(defmethod clojurize String [x] x)
(defmethod clojurize clojure.lang.IObj [x] x)
(defmethod clojurize clojure.lang.Keyword [x] x)
;;(defmethod clojurize clojure.lang.IFn [x] x)
;;------------------------------------------------------------------------------
(defmethod clojurize Iterable [^Iterable i] 
  (mapv clojurize (iterator-seq (.iterator i)))) 
(prefer-method clojurize Iterable clojure.lang.IObj)

(defmethod clojurize java.util.Map [^java.util.Map m] 
  (into (sorted-map) (map (fn [[k v]] [(clojurize k) (clojurize v)]) m)))

(defmethod clojurize java.util.Set [^java.util.Set s] 
  (into (sorted-set) (map clojurize s)))

(prefer-method clojurize java.util.Map clojure.lang.IObj)
(prefer-method clojurize java.util.Map clojure.lang.IFn)
(prefer-method clojurize java.util.Map Iterable)
;;------------------------------------------------------------------------------
(let [aclass (class (boolean-array 0))]
  (defmethod clojurize aclass [^booleans things] (into [] (Booleans/asList things))))
(let [aclass (class (byte-array 0))] 
  (defmethod clojurize aclass [^bytes things] (into [] (Bytes/asList things))))
(let [aclass (class (char-array 0))]
  (defmethod clojurize aclass [^chars things] (into [] (Chars/asList things))))
(let [aclass (class (double-array 0))]
  (defmethod clojurize aclass [^doubles things] (into [] (Doubles/asList things))))
(let [aclass (class (float-array 0))]
  (defmethod clojurize aclass [^floats things] (into [] (Floats/asList things))))
(let [aclass (class (long-array 0))]
  (defmethod clojurize aclass [^longs things] (into [] (Longs/asList things))))
(let [aclass (class (short-array 0))]
  (defmethod clojurize aclass [^shorts things] (into [] (Shorts/asList things))))
(let [aclass (class (object-array 0))]
  (defmethod clojurize aclass [^objects things] (into [] (Arrays/asList things))))
;------------------------------------------------------------------------------
