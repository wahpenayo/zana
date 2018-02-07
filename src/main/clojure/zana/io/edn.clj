(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2018-02-06"
      :doc "Global reader map for EDN input." }
    
    zana.io.edn
  
  (:require [clojure.edn :as edn]
            [zana.io.gz :as gz]))
;;----------------------------------------------------------------
(def ^{:private true :no-doc true} -edn-readers- (atom {}))

(defn edn-readers
  "Return a shared thread-safe global map from symbols to edn 
   de-serializer functions."
  [] 
  @-edn-readers-)

(defn add-edn-readers! 
  "<code>edn-readers</code> is a map from Symbol to EDN reader 
   function. These bindings are merged into the existing 
   [[edn-readers]].
   Currently no way to remove a reader."
  
  [edn-readers]
  
  (assert (instance? java.util.Map edn-readers))
  (assert (every? symbol? (keys edn-readers)))
  (assert (every? ifn? (vals edn-readers)))
  
  (swap! -edn-readers- merge edn-readers))
;;----------------------------------------------------------------
(let [aclass (class (boolean-array 0))
      reader (fn to-boolean-array ^booleans [v] 
               (into-array Boolean/TYPE v))]
  (defmethod print-method 
    aclass [^booleans this ^java.io.Writer w]
    (if *print-readably*
      (do
        (.write w " #zana/booleans [")
        (dotimes [i (alength this)]
          (.write w " ")
          (.write w (Boolean/toString (aget this i))))
        (.write w "] "))
      (.write w (print-str (into [] this)))))
  (add-edn-readers! {'zana/booleans reader}))
;;----------------------------------------------------------------
(let [aclass (class (char-array 0))
      reader (fn to-char-array ^chars [v]
               (into-array Character/TYPE v))]
  (defmethod print-method 
    aclass [^chars this ^java.io.Writer w]
    (if *print-readably*
      (do
        (.write w " #zana/chars [")
        (dotimes [i (alength this)]
          (.write w " ")
          (.write w (Character/toString (aget this i))))
        (.write w "] "))
      (.write w (print-str (into [] this)))))
  (add-edn-readers! {'zana/chars reader}))
;;----------------------------------------------------------------
(let [aclass (class (double-array 0))
      reader (fn to-double-array ^doubles [v] 
               (into-array Double/TYPE v))]
  (defmethod print-method 
    aclass [^doubles this ^java.io.Writer w]
    (if *print-readably*
      (do
        (.write w " #zana/doubles [")
        (dotimes [i (alength this)]
          (.write w " ")
          (.write w (Double/toString (aget this i))))
        (.write w "] "))
      (.write w (print-str (into [] this)))))
  (add-edn-readers! {'zana/doubles reader}))
;;----------------------------------------------------------------
(let [aclass (class (float-array 0))
      reader (fn to-float-array ^floats [v] 
               (into-array Float/TYPE v))]
  (defmethod print-method 
    aclass [^floats this ^java.io.Writer w]
    (if *print-readably*
      (do
        (.write w " #zana/floats [")
        (dotimes [i (alength this)]
          (.write w " ")
          (.write w (Float/toString (aget this i))))
        (.write w "] "))
      (.write w (print-str (into [] this)))))
  (add-edn-readers! {'zana/floats reader}))
;;----------------------------------------------------------------
(let [aclass (class (int-array 0))
      reader (fn to-int-array 
               ^ints [v] (into-array Integer/TYPE v))]
  (defmethod print-method 
    aclass [^ints this ^java.io.Writer w]
    (if *print-readably*
      (do
        (.write w " #zana/ints [")
        (dotimes [i (alength this)]
          (.write w " ")
          (.write w (Integer/toString (aget this i))))
        (.write w "] "))
      (.write w (print-str (into [] this)))))
  (add-edn-readers! {'zana/ints reader}))
;;----------------------------------------------------------------
(let [aclass (class (long-array 0))
      reader (fn to-long-array 
               ^longs [v] (into-array Long/TYPE v))]
  (defmethod print-method 
    aclass [^longs this ^java.io.Writer w]
    (if *print-readably*
      (do
        (.write w " #zana/longs [")
        (dotimes [i (alength this)]
          (.write w " ")
          (.write w (Long/toString (aget this i))))
        (.write w "] "))
      (.write w (print-str (into [] this)))))
  (add-edn-readers! {'zana/longs reader}))
;;----------------------------------------------------------------
(let [aclass (class (short-array 0))
      reader (fn to-short-array ^shorts [v] 
               (into-array Short/TYPE v))]
  (defmethod print-method 
    aclass [^shorts this ^java.io.Writer w]
    (if *print-readably*
      (do
        (.write w " #zana/shorts [")
        (dotimes [i (alength this)]
          (.write w " ")
          (.write w (Short/toString (aget this i))))
        (.write w "] "))
      (.write w (print-str (into [] this)))))
  (add-edn-readers! {'zana/shorts reader}))
;;----------------------------------------------------------------
;; TODO: handle specialized Object arrays
(let [aclass (class (object-array 0))
      reader (fn to-object-array ^objects [v] 
               (into-array Object v))]
  (defmethod print-method 
    aclass [^objects this ^java.io.Writer w]
    (if *print-readably*
      (do
        (.write w " #zana/objects [")
        (dotimes [i (alength this)]
          (.write w " ")
          (.write w (.toString (aget this i))))
        (.write w "] "))
      (.write w (print-str (into [] this)))))
  (add-edn-readers! {'zana/objects reader}))
;;----------------------------------------------------------------
(defn write-edn 
  
  "Serialize the <code>forest</code> to <code>file</code> in 
   [EDN](https://github.com/edn-format/edn) (clojure) syntax." 
  
  [thing file]

  (with-open [w (gz/print-writer file)]
    (binding [*print-readably* true
              *out* w] 
      (pr thing))))
;;----------------------------------------------------------------
(defn read-edn
  
  "Read a thing serialized to <code>x</code> 
   (a String or something that be coerced to a reader)
   in [EDN](https://github.com/edn-format/edn) (clojure) syntax." 
  
  [x]
  
  (if (instance? String x)
    (edn/read-string {:readers (edn-readers)} x)
    (with-open [r (clojure.lang.LineNumberingPushbackReader. 
                    (gz/reader x))]
      (edn/read {:readers (edn-readers)} r))))
;;----------------------------------------------------------------
