(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns zana.prob.seed
  
  {:doc "Independent seed generation and seed resource IO."
   :author "wahpenayo at gmail dot com"
   :since "2017-11-01"
   :date "2017-11-01"}
  
  (:refer-clojure :exclude [read write])
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :as pp])
  (:import [java.io PushbackReader]
           [java.nio ByteBuffer ByteOrder IntBuffer]
           [org.uncommons.maths.random 
            DefaultSeedGenerator RandomDotOrgSeedGenerator]))
;;----------------------------------------------------------------
(defn- bytes-to-ints ^ints [^bytes ba]
  (let [^IntBuffer ib (.asIntBuffer 
                        (.order 
                          (ByteBuffer/wrap ba) 
                          ByteOrder/BIG_ENDIAN))
        ^ints ia (int-array (.capacity ib))]
    (assert (== (* 4 (alength ia)) (alength ba)))
    (.get ib ia)
    ia))
;;----------------------------------------------------------------
(defn generate-default-seed ^ints [^long size]
  (bytes-to-ints
    (.generateSeed (DefaultSeedGenerator/getInstance) 
      (* 4 (int size)))))
;;----------------------------------------------------------------
(defn generate-randomdotorg-seed ^ints [^long size]
  (bytes-to-ints
    (.generateSeed (RandomDotOrgSeedGenerator.) 
      (* 4 (int size)))))
;;----------------------------------------------------------------
(defn write [^ints seed f]
  (io/make-parents (io/file f))
  (with-open [w (io/writer f)]
    (binding [*out* w]
      (pp/pprint (into [] seed)))))
;;----------------------------------------------------------------
(defn read ^ints [f]
  (with-open [r (PushbackReader. (io/reader f))]
    (int-array (edn/read r))))
;;----------------------------------------------------------------
;; TODO: move somewhere more appropriate
(let [c (class (int-array 0))]
  (defn- int-array? [x] (instance? c x)))
;;----------------------------------------------------------------
;; if the seed is a string, assume it's the name of a resource.
;; if it's a resource, read it.
;; TODO: what about ordinary files?
(defn seed ^ints [x]
  (cond (string? x) (recur (io/resource x))
        (instance? java.net.URL x) (recur (read x))
        (int-array? x) x
        :else
        (throw 
          (IllegalArgumentException. 
            (print-str "Invalid seed source:" x)))));
;;----------------------------------------------------------------
