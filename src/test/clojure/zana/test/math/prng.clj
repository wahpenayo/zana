(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2015-11-18"
      :doc "Unit tests for zana.stats.prng." }
     
    zana.test.math.prng
  
  (:require [clojure.test :as test]
            [zana.stats.prng :as prng])
  
  (:import [org.uncommons.maths.binary BinaryUtils]
           [org.uncommons.maths.random MersenneTwisterRNG]))
;;------------------------------------------------------------------------------
(test/deftest mersenne-twister-seed
  (let [seed0 (prng/mersenne-twister-seed)
        seed1 (prng/mersenne-twister-seed)]
    (test/is (not (nil? seed0)))
    (test/is (not (nil? seed1)))
    (test/is (not= seed0 seed1))))

(test/deftest mersenne-twister-generator
  (let [seed0 "21CEE048585DD1821A40D3556E1FD10E"
        b (into [] (BinaryUtils/convertHexStringToBytes seed0))
        ^MersenneTwisterRNG prng0 (prng/mersenne-twister-generator seed0)
        b0 (into [] (.getSeed prng0))
        ^MersenneTwisterRNG prng1 (prng/mersenne-twister-generator)
        b1 (into [] (.getSeed prng1))]
    (test/is (not (nil? prng0)))
    (test/is (not (nil? prng1)))
    (test/is (not= prng0 prng1))
    (test/is (not= (.nextLong prng0)  (.nextLong prng0)))
    (test/is (not= (.nextLong prng0)  (.nextLong prng1)))
    (test/is (= b b0))
    (test/is (not= b0 b1))))
;;------------------------------------------------------------------------------