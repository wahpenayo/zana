(set! *warn-on-reflection* true)
(set! *unchecked-math* false) ;; warnings in cheshire.generate
(ns ^{:author "wahpenayo at gmail dot com" 
      :since "2017-10-24"
      :date "2017-11-02"
      :doc "Probability measures over <b>R</b>." }
    
    zana.prob.measure
  
  (:refer-clojure :exclude [every?])
  (:require [cheshire.generate]
            [zana.commons.core :as zcc]
            [zana.collections.clojurize :as zccl]
            [zana.io.edn :as zedn]
            [zana.stats.statistics :as zss])
  (:import [java.util Arrays]
           [com.carrotsearch.hppc DoubleArrayList]
           [clojure.lang IFn$DO IFn$DDO]
           [org.apache.commons.math3.distribution
            RealDistribution]
           [org.apache.commons.math3.random RandomGenerator]
           [zana.java.arrays Sorter]
           [zana.java.math Statistics]
           [zana.java.prob ApproximatelyEqual WECDF WEPDF]))
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
;; TODO: use float arrays but calculate in double to eliminate 
;; Math/ulp in comparisons?
;; Probably want to move interface and classes to Java in that 
;; case...
;;----------------------------------------------------------------
(defn- dmapcat 
  "Assume f returns <code>double[]</code>. Concatenate the arrays
   into one big array."
  ^doubles [f ^Iterable objects]
  (let [arrays (mapv f objects)
        m (count arrays)
        n (int (reduce + 0 (map #(alength ^doubles %) arrays)))
        out (double-array n)]
    (loop [i 0
           arrays arrays] 
      (if (empty? arrays)
        out
        (let [^doubles zi (first arrays)
              ni (alength zi)]
          (System/arraycopy zi 0 out i ni)
          (recur (+ i ni) (rest arrays)))))))
;;----------------------------------------------------------------
(defn- to-doubles ^doubles [z]
  (assert (not (nil? z)))
  (cond 
    (zcc/double-array? z) z
    (instance? DoubleArrayList z) (.toArray ^DoubleArrayList z)
    (vector? z) (double-array z)
    :else (throw 
            (IllegalArgumentException.
              (str "can't convert " (class z) " to double[]")))))
;;----------------------------------------------------------------
(defn make-wepdf 
  "Create an instance of <code>WEPDF</code>." 
  (^WEPDF [^RandomGenerator prng z w]
    (WEPDF/make prng (to-doubles z) (to-doubles w)))
  (^WEPDF [z w]
    (WEPDF/make (to-doubles z) (to-doubles w)))
  (^WEPDF [z]
    (WEPDF/make (to-doubles z))))
;;----------------------------------------------------------------
(defn average-wepdfs
  "Return the mean probability measure."
  (^WEPDF [& wepdfs]
    (let [zs (dmapcat #(.getZ ^WEPDF %) wepdfs)
          ws (dmapcat #(.getW ^WEPDF %) wepdfs)]
      (make-wepdf zs ws))))
;;----------------------------------------------------------------
(defn make-wecdf 
  "Create an instance of <code>WECDF</code>." 
  (^WECDF [^RandomGenerator prng z w]
    (WECDF/make prng (to-doubles z) (to-doubles w)))
  (^WECDF [z w]
    (WECDF/make (to-doubles z) (to-doubles w)))
  (^WECDF [z]
    (WECDF/make (to-doubles z))))
;;----------------------------------------------------------------
(defn wepdf-to-wecdf
  "Convert a point mass density representation to a cumulative one."
  ^WECDF [^WEPDF pdf] (WECDF/make pdf))
(defn wecdf-to-wepdf
  "Convert a cumulative representation to a point mass density one."
  ^WEPDF [^WECDF cdf] (WEPDF/make cdf))
;;----------------------------------------------------------------
;; TODO: generic function api for general probability measures
(defn pointmass ^double [^RealDistribution rpm ^double z]
  (.probability rpm z))
(defn cdf ^double [^RealDistribution rpm ^double z]
  (.cumulativeProbability rpm z))
(defn quantile ^double [^RealDistribution rpm ^double p]
  (.inverseCumulativeProbability rpm p))
;;----------------------------------------------------------------
;; text serialization
;;----------------------------------------------------------------
;; TODO: JSON/END serialization for RandomGenerator classes
(defn map->WEPDF [m] 
  (WEPDF/sortedAndNormalized  
    (:rng m) (double-array (:z m)) (double-array (:w m))))
(defn map<-WEPDF [^WEPDF d] 
  {#_:rng #_(.rng d) :z (vec (.getZ d)) :w (vec (.getW d))})
(defmethod zccl/clojurize WEPDF [this] (map<-WEPDF this))
(defmethod print-method WEPDF [^WEPDF this ^java.io.Writer w]
  (if *print-readably*
    (do
      (.write w " #zana.java.prob.WEPDF{")
      #_(.write w ":rng ")
      #_(.write w (print-str (.rng this)))
      (.write w " :z ")
      (.write w (print-str (vec (.getZ this))))
      (.write w " :w ")
      (.write w (print-str (vec (.getW this))))
      (.write w "} "))
    (.write w (print-str (map<-WEPDF this)))))
;;----------------------------------------------------------------
(defn map->WECDF [m] 
  (WECDF/sortedAndNormalized 
    (:rng m) (double-array (:z m)) (double-array (:w m))))
(defn map<-WECDF [^WECDF d] 
  {#_:rng #_(.rng d) :z (vec (.getZ d)) :w (vec (.getW d))})
(defmethod zccl/clojurize WECDF [this] (map<-WECDF this))
(defmethod print-method WECDF [^WECDF this ^java.io.Writer w]
  (if *print-readably*
    (do
      (.write w " #zana.java.prob.WECDF{")
      #_(.write w ":rng ")
      #_(.write w (print-str (.rng this)))
      (.write w " :z ")
      (.write w (print-str (vec (.getZ this))))
      (.write w " :w ")
      (.write w (print-str (vec (.getW this))))
      (.write w "} "))
    (.write w (print-str (map<-WECDF this)))))
;;----------------------------------------------------------------
;; EDN 
;;----------------------------------------------------------------
(zedn/add-edn-readers! 
  {'zana.java.prob.WEPDF map->WEPDF
   'zana.java.prob.WECDF map->WECDF})
;;----------------------------------------------------------------
;; JSON output (input not supported)
;;----------------------------------------------------------------
(defn- WEPDF-encoder [^WEPDF d json-generator]
  (cheshire.generate/encode-map (map<-WEPDF d) json-generator))
(cheshire.generate/add-encoder 
  zana.java.prob.WEPDF WEPDF-encoder)
(defn- WECDF-encoder [^WECDF d json-generator]
  (cheshire.generate/encode-map (map<-WECDF d) json-generator))
(cheshire.generate/add-encoder 
  zana.java.prob.WECDF WECDF-encoder)
;;----------------------------------------------------------------
