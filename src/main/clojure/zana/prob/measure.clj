(set! *warn-on-reflection* true)
(set! *unchecked-math* false) ;; warnings in cheshire.generate
(ns ^{:author "wahpenayo at gmail dot com" 
      :since "2017-10-24"
      :date "2017-11-15"
      :doc "Probability measures over <b>R</b>." }
    
    zana.prob.measure
  
  (:refer-clojure :exclude [every?])
  (:require [cheshire.generate]
            [zana.commons.core :as zcc]
            [zana.collections.clojurize :as zccl]
            [zana.io.edn :as zedn]
            [zana.stats.statistics :as zss])
  (:import [java.util Arrays Map]
           [com.carrotsearch.hppc DoubleArrayList]
           [clojure.lang IFn$DO IFn$DDO]
           [org.apache.commons.math3.distribution
            NormalDistribution RealDistribution 
            UniformRealDistribution]
           [org.apache.commons.math3.random RandomGenerator]
           [zana.java.arrays Sorter]
           [zana.java.math Statistics]
           [zana.java.prob ApproximatelyEqual 
            TranslatedRealDistribution WECDF WEPDF]))
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
;; TODO: use float arrays but calculate in double to eliminate 
;; Math/ulp in comparisons?
;; Probably want to move interface and classes to Java in that 
;; case...
;;----------------------------------------------------------------
(defn- to-double ^double [x]
  (cond (instance? Number x) (.doubleValue ^Number x)
        (instance? String x) (Double/parseDouble ^String x)
        :else (throw 
                (IllegalArgumentException. 
                  (str "can't convert" (class x) "to double.")))))
;;----------------------------------------------------------------
;; TODO: move elsewhere?
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
  {#_:rng #_(.rng d) :z (.getZ d) :w (.getW d)})
(defmethod zccl/clojurize WEPDF [this] (map<-WEPDF this))
(defmethod print-method WEPDF [^WEPDF this ^java.io.Writer w]
  (if *print-readably*
    (do
      (.write w " #zana.java.prob.WEPDF{")
      #_(.write w ":rng ")
      #_(.write w (print-str (.rng this)))
      (.write w " :z ")
      (.write w (print-str (.getZ this)))
      (.write w " :w ")
      (.write w (print-str (.getW this)))
      (.write w "} "))
    (.write w (print-str (map<-WEPDF this)))))
;;----------------------------------------------------------------
(defn map->WECDF [m] 
  (WECDF/sortedAndNormalized 
    (:rng m) (double-array (:z m)) (double-array (:w m))))
(defn map<-WECDF [^WECDF d] 
  {#_:rng #_(.rng d) :z (.getZ d) :w (.getW d)})
(defmethod zccl/clojurize WECDF [this] (map<-WECDF this))
(defmethod print-method WECDF [^WECDF this ^java.io.Writer w]
  (if *print-readably*
    (do
      (.write w " #zana.java.prob.WECDF{")
      #_(.write w ":rng ")
      #_(.write w (print-str (.rng this)))
      (.write w " :z ")
      (.write w (print-str (.getZ this)))
      (.write w " :w ")
      (.write w (print-str (.getW this)))
      (.write w "} "))
    (.write w (print-str (map<-WECDF this)))))
;;----------------------------------------------------------------
;; serializing other RealDistributions to Strings that can be used 
;; in EDN or TSV files.
;; Not attempting to serialize RandomGenerators.
;;----------------------------------------------------------------
(defn- map<-NormalDistribution 
  ^Map [^NormalDistribution d] 
  {:mean (.getMean d) 
   :standardDeviation (.getStandardDeviation d)})
(defn- map->NormalDistribution 
  ^NormalDistribution [^Map m] 
  (NormalDistribution. 
    (to-double (:mean m)) 
    (to-double (:standardDeviation m))))
(defmethod zccl/clojurize 
  NormalDistribution 
  [^NormalDistribution this]
  (map<-NormalDistribution this))
(defmethod print-method 
  NormalDistribution 
  [^NormalDistribution this ^java.io.Writer w]
  (.write w "#org.apache.commons.math3.distribution.")
  (.write w "NormalDistribution{")
  (.write w ",:mean,")
  (.write w (Double/toString (.getMean this)))
  (.write w ",:standardDeviation,")
  (.write w (Double/toString (.getStandardDeviation this)))
  (.write w "}"))
;;----------------------------------------------------------------
(defn- map<-UniformRealDistribution 
  ^Map [^UniformRealDistribution d] 
  {:supportLowerBound (.getSupportLowerBound d)
   :supportUpperBound (.getSupportUpperBound d)})
(defn- map->UniformRealDistribution 
  ^UniformRealDistribution [^Map m] 
  (UniformRealDistribution. 
    (to-double (:supportLowerBound m))
    (to-double (:supportUpperBound m))))
(defmethod zccl/clojurize 
  UniformRealDistribution 
  [^UniformRealDistribution this]
  (map<-UniformRealDistribution this))
(defmethod print-method 
  UniformRealDistribution 
  [^UniformRealDistribution this ^java.io.Writer w]
  (.write w "#org.apache.commons.math3.distribution.")
  (.write w "UniformRealDistribution{")
  (.write w ",:supportLowerBound,")
  (.write w (Double/toString (.getSupportLowerBound this)))
  (.write w ",:supportUpperBound,")
  (.write w (Double/toString (.getSupportUpperBound this)))
  (.write w "}"))
;;----------------------------------------------------------------
(defn- map<-TranslatedRealDistribution 
  ^Map [^TranslatedRealDistribution d] 
  {:dz (.getDz d) :rd (.getRd d)})
(defn- map->TranslatedRealDistribution 
  ^TranslatedRealDistribution [^Map m] 
  ;; handle case where inner distribution is represented by an EDN 
  ;; string
  (let [dz (to-double (:dz m))
        rd (:rd m)
        rd (if (string? rd) (zedn/read-edn rd) rd)]
  (TranslatedRealDistribution/shift rd dz)))
(defmethod zccl/clojurize 
  TranslatedRealDistribution 
  [^TranslatedRealDistribution this]
  (map<-TranslatedRealDistribution this))
(defmethod print-method 
  TranslatedRealDistribution 
  [^TranslatedRealDistribution this ^java.io.Writer w]
  (.write w " #zana.java.prob.")
  (.write w "TranslatedRealDistribution{")
  (.write w ",:dz,")
  (.write w (Double/toString (.getDz this)))
  (.write w ",:rd,")
  (.write w (print-str (.getRd this)))
  (.write w "}"))
;;----------------------------------------------------------------
;; EDN 
;;----------------------------------------------------------------
(zedn/add-edn-readers! 
  {'zana.java.prob.WEPDF 
   map->WEPDF
   
   'zana.java.prob.WECDF 
   map->WECDF
   
   'org.apache.commons.math3.distribution.NormalDistribution
   map->NormalDistribution
   
   'org.apache.commons.math3.distribution.UniformRealDistribution
   map->UniformRealDistribution
   
   
   'zana.java.prob.TranslatedRealDistribution
   map->TranslatedRealDistribution})
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
(defn- NormalDistribution-encoder 
  [^NormalDistribution d json-generator]
  (cheshire.generate/encode-map 
    (map<-NormalDistribution d) json-generator))
(cheshire.generate/add-encoder 
  org.apache.commons.math3.distribution.NormalDistribution
  NormalDistribution-encoder)
(defn- UniformRealDistribution-encoder 
  [^UniformRealDistribution d json-generator]
  (cheshire.generate/encode-map 
    (map<-UniformRealDistribution d) json-generator))
(cheshire.generate/add-encoder 
  org.apache.commons.math3.distribution.UniformRealDistribution
  UniformRealDistribution-encoder)
;;----------------------------------------------------------------
