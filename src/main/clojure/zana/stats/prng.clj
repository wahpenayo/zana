(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-08-31"
      :doc "Pseudo-random number generators, constructed from a resource
            of truly random seeds." }
     
    zana.stats.prng
  
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [zana.io.gz :as gz])
  
  (:import [java.util ArrayList Arrays Collection Collections List Random]
           [org.uncommons.maths.binary BinaryUtils]
           [org.uncommons.maths.random ContinuousUniformGenerator 
            DiscreteUniformGenerator MersenneTwisterRNG]))
;;------------------------------------------------------------------------------
(defn ^:no-doc java-random-generator ^Random [] (Random.))
;;------------------------------------------------------------------------------
;; Good enough for small collections, but...
;; TODO: replace with Waterman/Knuth algorithm.
(defn- sample-collection [^Random prng
                         ^long sample-size
                         ^java.util.Collection data]
  (let [copy (ArrayList. data)]
    (Collections/shuffle copy prng)
    (Collections/unmodifiableList
      (if (>= sample-size (.size data))
        copy
        (ArrayList. (.subList copy 0 sample-size))))))

(defn- sample-ipm [^Random prng
                  ^long sample-size
                  ^clojure.lang.IPersistentMap data]
  (into (sorted-map) 
        (sample-collection prng sample-size (seq data))))

(defn sample 
  "Collect <code>sample-size</code> pseudo-random elements 
   from <code>data</code> without replacement. Equivalent to shuffling
   <code>data</code> and taking the first <code>sample-size</code> elements.
   <br>See [[sample-with-replacement]]."
  [^Random prng
   ^long sample-size
   data]
  
  (cond (map? data)
        (sample-ipm prng sample-size data)
        
        (instance? java.util.Collection data)
        (sample-collection prng sample-size data)
        
        :else
        (throw (IllegalArgumentException.
                 (print-str "Don't know how to sample from:" (class data))))))
;;------------------------------------------------------------------------------
;; lazy immutable seq version --- too much overhead for large datasets
(defn ^:no-doc slow-sample-with-replacement [prng data]
  (let [n (count data)]
    (repeatedly n (fn [] (nth data (.nextInt ^Random prng n))))))
;;------------------------------------------------------------------------------
;; TODO: faster version
(defn sample-with-replacement 
  "Create a new data set of the same size by sampling from <code>data</code> 
   with replacement. That means every element in the sample is chosen from the
   full original data set, so any given element can occur in the sample multiple
   times, and the sample will contain on average about 2/3 of the original
   elements.
   <br>See [[sample]]."
  ^java.util.List [^Random prng ^List data]
  (let [n (count data)
        a (object-array n)]
    (dotimes [i n] (aset a i (.get data (.nextInt prng n))))
    (Collections/unmodifiableList (Arrays/asList a))))
;;------------------------------------------------------------------------------
(defn- read-mersenne-twister-seeds [_]
  (with-open [r (clojure.lang.LineNumberingPushbackReader. 
                  (gz/reader 
                    (io/resource 
                      "zana/stats/mersenne-twister-seeds.edn")))]
      (edn/read r)))
;;------------------------------------------------------------------------------
;; TODO: add seeds for other generator classes
;; TODO: fetch new seeds when these run out, and persist all of them.

(def ^{:private true} mersenne-twister-seeds (ref nil))
;;------------------------------------------------------------------------------
(defn reset-mersenne-twister-seeds 
  "Zana maintains a thread-safe finite sequence of truly random 
   mersenne twister seeds, which are read from a resource file.
   This resets the sequence to its start. Useful for getting reproducible
   results from algorithms like random forests. See [[mersenne-twister-seed]]."
  []
  (dosync (alter mersenne-twister-seeds read-mersenne-twister-seeds)))
;;------------------------------------------------------------------------------
(defn mersenne-twister-seed 
   "Zana maintains a thread-safe finite sequence of truly random 
   mersenne twister seeds. This consumes the next seed in the sequence, 
   resetting the sequence to its original start when it runs out.
   See [[reset-mersenne-twister-seeds]]."
   
   ^String []

  (when-not (deref mersenne-twister-seeds)
    (reset-mersenne-twister-seeds))
  (dosync
    (let [seed (first (deref mersenne-twister-seeds))]
      (alter mersenne-twister-seeds next)
      seed)))
;;------------------------------------------------------------------------------
(defn mersenne-twister-generator
  "Return a 
   <a href=\"http://maths.uncommons.org/api/org/uncommons/maths/random/MersenneTwisterRNG.html\">
  MersenneTwisterRNG</a>, using the provided <code>seed</code>, or the next 
  value of [[mersenne-twister-seed]]."
  (^java.util.Random [^String seed]
    (MersenneTwisterRNG. (BinaryUtils/convertHexStringToBytes seed)))
  (^java.util.Random []
    (let [seed (mersenne-twister-seed)]
      (assert
        seed
        "Ran out of mersene twister seeds! Change the cache to grow as needed!")
      (mersenne-twister-generator seed))))
;;------------------------------------------------------------------------------
(defn continuous-uniform-generator 

  "Return a function which, when called, returns uniformly distributed 
   pseudo-random <code>double</code> values, based on <code>prng</code>.
   <dl>
   <dt><code>^clojure.lang.IFn$D [^double x0 ^double x1 prng]</code>
   </dt>
   <dd>If <code>prng</code> is an instance of </code>java.util.Random</code>,
   return a function <code>f</code> such that <code>(f)</code> is uniformly
   distrubuted between <code>x0</code> and <code>x1</code>.<br>
   If <code>prng</code> is a <code>String</code>, assume it's a valid seed for
   [[mersenne-twister-generator]], and use the <code>Random</code> returned as
   to create the generating function.
   </dd>
   <dt><code>^clojure.lang.IFn$D [prng]</code>
   </dt>
   <dd>Same as <code>(continuous-uniform-generator 0.0 1.0 prng)</code>.
   </dd>
   </dl>"
  
  (^clojure.lang.IFn$D [^double x0 ^double x1 prng]
  (cond (instance? java.util.Random prng)
        (let [cug (ContinuousUniformGenerator. x0 x1 prng)]
          (fn generate-continuous-uniform ^double [] (.nextValue cug)))
        
        (string? prng)
        (continuous-uniform-generator x0 x1 (mersenne-twister-generator prng))
        
        :else 
        (throw (IllegalArgumentException. 
                 (pr-str "can't create a generator from" prng)))))
  (^clojure.lang.IFn$D [prng] (continuous-uniform-generator 0.0 1.0 prng)))
;;------------------------------------------------------------------------------
(deftype BernoulliGenerator [^double p ^ContinuousUniformGenerator cug]
  org.uncommons.maths.number.NumberGenerator
  (nextValue [this] (if (> (double (.nextValue cug)) p) 0.0 1.0)))
;;------------------------------------------------------------------------------
(defn bernoulli-generator 
  
  "Return a function which returns 0.0 or 1.0, pseudo-randomly, based on
   <code>prng</code>.
   <dl>
   <dt><code>^clojure.lang.IFn$DD [prng]</code>
   </dt>
   <dd>If <code>prng</code> is an instance of </code>java.util.Random</code>,
   return a function <code>f</code> such that <code>(f p)</code> is 1.0 with
   probability <code>p</code> and 0.0 otherwise.<br>
   If <code>prng</code> is a <code>String</code>, assume it's a valid seed for
   [[mersenne-twister-generator]], and use the <code>Random</code> returned as
   to create the generating function.
   </dd>
   <dt><code>^clojure.lang.IFn$D [^double p prng]</code>
   </dt>
   <dd>If <code>prng</code> is an instance of </code>java.util.Random</code>,
   return a function <code>f</code> such that <code>(f)</code> is 1.0 with
   probability <code>p</code> and 0.0 otherwise.<br>
   If <code>prng</code> is a <code>String</code>, assume it's a valid seed for
   [[mersenne-twister-generator]], and use the <code>Random</code> returned as
   to create the generating function.
   </dd>
   </dl>"
  
  (^clojure.lang.IFn$DD [prng]
    (cond (instance? java.util.Random prng)
          (let [cug (ContinuousUniformGenerator. 0.0 1.0 prng)]
            (fn generate-bernoulli ^double [^double p] 
              (cond (<= p 0.0) 0.0
                    (>= p 1.0) 1.0
                    (> (double (.nextValue cug)) p) 0.0 
                    :else 1.0)))
          
          (string? prng)
          (bernoulli-generator (mersenne-twister-generator prng))
          
          :else 
          (throw (IllegalArgumentException. 
                   (pr-str "can't create a generator from" prng)))))
  
  (^clojure.lang.IFn$D [^double p prng]
    (cond (instance? java.util.Random prng)
          (let [cug (ContinuousUniformGenerator. 0.0 1.0 prng)
                bg (BernoulliGenerator. p cug)]
            (fn generate-bernoulli ^double [] (.nextValue bg)))
          
          (string? prng)
          (bernoulli-generator p (mersenne-twister-generator prng))
          
          :else 
          (throw (IllegalArgumentException. 
                   (pr-str "can't create a generator from" prng))))))
;;------------------------------------------------------------------------------
(defn random-element-generator 
  "Return a function of no args that returns a equally likely pseudo-random 
   element of <code>lst</code> every time it's called, based on
   <code>prng</code>.
   If <code>prng</code> is a <code>String</code>, assume it's a valid seed for
   [[mersenne-twister-generator]], and use the <code>Random</code> returned as
   to create the generating function. Otherwise, <code>prng</code> must be an 
   instance of <code>java.util.Random</code>."
  ^clojure.lang.IFn [^java.util.List lst prng]
  (cond (instance? java.util.Random prng)
        (let [dug (DiscreteUniformGenerator. (int 0) (dec (.size lst)) prng)]
          (fn random-element [] (.get lst (int (.nextValue dug)))))
        
        (string? prng)
        (random-element-generator lst (mersenne-twister-generator prng))
        
        :else 
        (throw (IllegalArgumentException. 
                 (pr-str "can't create a generator from" prng)))))
;;------------------------------------------------------------------------------
;; Run this to generate the desired number of 'truly' random seeds.
(comment
  ;; one seed at a time
  ;;(def g (org.uncommons.maths.random.DefaultSeedGenerator/getInstance))
  (def g (org.uncommons.maths.random.RandomDotOrgSeedGenerator.))
  (def m (alength (.getSeed (org.uncommons.maths.random.MersenneTwisterRNG.))))
  (mapv (fn [_] (org.uncommons.maths.binary.BinaryUtils/convertBytesToHexString
                  (.generateSeed g m)))
        (range 8))
  (let [generator
        (org.uncommons.maths.random.DefaultSeedGenerator/getInstance)
        m (alength (.getSeed (org.uncommons.maths.random.MersenneTwisterRNG.)))
        generate (fn generate [_]
          (org.uncommons.maths.binary.BinaryUtils/convertBytesToHexString
            (.generateSeed generator m)))
        seeds (mapv generate-seed (range (* 8 2048)))]
    (with-open [w (java.io.FileWriter. "seeds.clj")]
      (binding [*out* w] (pprint seeds))))
  (let [generator (org.uncommons.maths.random.DevRandomSeedGenerator.)
        m (alength (.getSeed (org.uncommons.maths.random.MersenneTwisterRNG.)))
        seed (fn seed [_]
          (org.uncommons.maths.binary.BinaryUtils/convertBytesToHexString
            (.generateSeed generator m)))
        seeds (mapv seed (range (* 8 2048)))]
    (with-open [w (java.io.FileWriter. 
                    (clojure.java.io/file "main" "resources" "zana" "stats"
                                          "mersenne-twister-seeds.edn"))]
      (binding [*out* w] (pprint seeds))))
  (let [generator (org.uncommons.maths.random.DevRandomSeedGenerator.)
        m (alength (.getSeed (org.uncommons.maths.random.MersenneTwisterRNG.)))
        f (clojure.java.io/file "src" "main" "resources" "zana" "stats"
                                "mersenne-twister-seeds.edn")]
    (clojure.java.io/make-parents f)
    (with-open [w (java.io.FileWriter. f)]
      (binding [*out* w]
        (print "[")
        (dotimes [_ (* 8 2048)]
          (prn
            (org.uncommons.maths.binary.BinaryUtils/convertBytesToHexString
              (.generateSeed generator m))))
        (println "]"))))
  )
;;------------------------------------------------------------------------------