(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns ^{:author "wahpenayo at gmail dot com"
      :date "2018-02-07"
      :doc 
      "pseudo-random number generators using apache commons math3.
      <p>
      <i>TODO:</i> merge this with 
      <code>zana.stat.prng</code>, favoring commons math3
      (commons rng and math4) over uncommons maths."}
    
    zana.prob.prng
  
  (:require [zana.commons.core :as zcc]
            [zana.prob.seed :as zps])
  (:import [clojure.lang IFn IFn$D IFn$LO] 
           [org.apache.commons.math3.distribution
            RealDistribution]
           [org.apache.commons.math3.random 
            MersenneTwister RandomGenerator Well44497b]))
;;----------------------------------------------------------------
(defn mersenne-tiwster
  "Create an instance of <a href=
  \"http://commons.apache.org/proper/commons-math/javadocs/api-3.6.1/org/apache/commons/math3/random/MersenneTwister.html\">
   org.apache.commons.math3.random.MersenneTwister</a>.
   <p>
   See Makoto Matsumoto and Takuji Nishimura 
   <a href=\"http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/ARTICLES/mt.pdf\">
   <i>Mersenne Twister: A 623-Dimensionally Equidistributed Uniform Pseudo-Random
   Number Generator</i></a>, 
   <b>ACM Trans. Modeling and Computer Simulation 8</b> (1) 1998</a>.
   <p>
   <code>seed</code> is expected to be an <code>int[624]</code>,
   or the name or URL of a resource containing EDN code for 
   a vector of 624 integers."
  
  ;; TODO: generalize to accept int and long
  ;; seeds, like org.apache.commons.math3.random.Well44497b?
  ;; TODO: does it make a difference to supply a complete
  ;; initial state of truly independent numbers?
  
  ^RandomGenerator [seed]
  (let [seed (zps/seed seed)]
    (assert (zcc/int-array? seed))
    (assert (== 1391 (alength ^ints seed)))
    (MersenneTwister. seed)))
;;----------------------------------------------------------------
(defn well44497b 
  "Create an instance of <a href=
  \"http://commons.apache.org/proper/commons-math/javadocs/api-3.6.1/org/apache/commons/math3/random/Well44497b.html\">
   org.apache.commons.math3.random.Well44497b</a>.
   <p>   
   See Fran&ccedil;ois Panneton,Pierre L'Ecuyer and Makoto Matsumoto
   <a href=\"http://www.iro.umontreal.ca/~lecuyer/myftp/papers/wellrng.pdf\">
   <i>Improved Long-Period Generators Based on Linear Recurrences Modulo 2</i></a> 
   <b>ACM Trans. Math. Software 32</b> (1) 2006, and
   <a href=\"http://www.iro.umontreal.ca/~lecuyer/myftp/papers/wellrng-errata.txt\">
   errata</a>.
   <p>
   <code>seed</code> is expected to be an <code>int[1391]</code>,
   or the name or URL of a resource containing EDN code for 
   a vector of 1391 integers."
  
  ;; TODO: generalize to accept int and long
  ;; seeds, like org.apache.commons.math3.random.Well44497b?
  ;; TODO: does it make a difference to supply a complete
  ;; initial state of truly independent numbers?
  
  ^RandomGenerator [seed]
  (let [seed (zps/seed seed)]
    (assert (zcc/int-array? seed))
    (assert (== 1391 (alength ^ints seed)))
    (Well44497b. seed)))
;;----------------------------------------------------------------
;; Return generator functions
(defn double-generator ^IFn$D [^RealDistribution d]
  (fn generate (^double [] (.sample d))))
;;----------------------------------------------------------------
