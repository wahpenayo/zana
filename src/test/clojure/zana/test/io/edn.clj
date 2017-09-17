(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2017-01-18"
      :doc "Tests for zana.io.edn." }
    
    zana.test.io.edn
  
  (:require [clojure.java.io :as io]
            [clojure.test :as test]
            [zana.api :as z]))
;;mvn -Dtest=zana.test.io.edn clojure:test
;;------------------------------------------------------------------------------
(test/deftest doubles-io
  (let [f (io/file "tst" "doubles.edn")
        a (double-array 2)
        b (float-array 2)
        v [a b]]
    (aset-double a 0 0.0)
    (aset-double a 1 1.0)
    (aset-float b 0 (float 2.0))
    (aset-float b 1 (float 3.0))
    (z/write-edn v f)
    (let [v1 (z/read-edn f)
          _(test/is (vector? v1))
          a1 (nth v1 0)
          _(test/is (instance? (class a) a1))
          a1 (doubles a1)
          b1 (nth v1 1)
          _(test/is (instance? (class b) b1))
          b1 (floats b1)]
      (test/is (== (aget a 0) (aget a1 0)))
      (test/is (== (aget b 0) (aget b1 0))))))
;;------------------------------------------------------------------------------
