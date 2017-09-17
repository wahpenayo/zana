(set! *warn-on-reflection* false) ;; criterium has warnings
(ns ^{:author "John Alan McDonald" :date "2016-04-21"
      :doc "Time (new ) vs (.newInstance )." }
    
    zana.scripts.data.construction
  
  (:require [clojure.java.io :as io]
            [clojure.test :as test]
            [criterium.core :as criterium]
            [zana.test.defs.data.bin :as bin])
  (:import [zana.test.defs.data.bin Bin]))
(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;------------------------------------------------------------------------------
(let [zero (float 0.0)]
  (defn new-bin [^double x]
    (new 
      Bin 
      zero zero zero zero
      zero zero zero zero zero zero zero zero zero zero zero zero zero zero zero zero
      zero zero zero zero zero zero zero zero zero zero zero zero zero zero zero zero
      zero zero zero zero zero zero zero zero zero zero zero zero zero zero zero zero
      zero zero zero zero zero zero zero zero zero zero zero zero zero zero zero zero
      zero zero zero zero zero zero zero zero zero zero zero zero zero zero zero zero 
      zero (float x))))
;;------------------------------------------------------------------------------
(let [zero (Float/valueOf (float 0.0))
      ^objects values 
      (to-array 
        [zero zero zero zero
         zero zero zero zero zero zero zero zero zero zero zero zero zero zero zero zero
         zero zero zero zero zero zero zero zero zero zero zero zero zero zero zero zero
         zero zero zero zero zero zero zero zero zero zero zero zero zero zero zero zero
         zero zero zero zero zero zero zero zero zero zero zero zero zero zero zero zero
         zero zero zero zero zero zero zero zero zero zero zero zero zero zero zero zero 
         zero zero])
      ^java.lang.reflect.Constructor constructor (aget (.getConstructors Bin) 0)]
  ;;(println (into [] (.getConstructors Bin)))
  (defn new-instance [^double x]
    (aset values (dec (alength values)) (Float/valueOf (float x)))
    (.newInstance constructor values)))
;;------------------------------------------------------------------------------
(let [^java.util.Random random (java.util.Random.)]
  (println "new-instance:")
  (criterium/bench (new-instance (.nextDouble random)))
  (println "new:")
  (criterium/bench (new-bin (.nextDouble random))))
;;------------------------------------------------------------------------------


