(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-05-20"
      :doc "Tests for zana.data.datum." }
    
    zana.test.data.binaryio
  
  (:require [clojure.java.io :as io]
            [clojure.test :as test]
            [zana.api :as z]
            [zana.test.defs.data.empty :as empty]
            [zana.test.defs.data.primitive :as primitive]
            [zana.test.defs.data.typical :as typical]
            [zana.test.defs.data.change :as change]
            [zana.test.data.setup :as setup])
  (:import [java.time LocalDateTime LocalDate]
           [zana.test.defs.data.empty Empty]
           [zana.test.defs.data.primitive Primitive]
           [zana.test.defs.data.typical Typical]
           [zana.test.defs.data.change Change]))
;;------------------------------------------------------------------------------
;; mvn -Dtest=zana.test.data.binaryio clojure:test
#_(test/run-tests 'zana.test.data.binaryio)
;;------------------------------------------------------------------------------
;; edge case

(test/deftest empty-datum
  (let [[e0 e1 e2] (setup/empties)
        f (io/file "tst" "empty.bin.gz")
        _ (empty/write-binary-file [e0 e1 e2] f)
        es (empty/read-binary-file f)]
    (test/is (every? #(instance? Empty %) es))))
;;------------------------------------------------------------------------------
;; no recursion

(test/deftest primitive
  (let [ps0 (setup/primitives)
        f (io/file "tst" "primitive.bin.gz")
        _ (primitive/write-binary-file ps0 f)
        ps1 (primitive/read-binary-file f)]
    (test/is (== 5 (count ps0) (count ps1)))
    (test/is (every? #(instance? Primitive %) ps0))
    (test/is (every? #(instance? Primitive %) ps1))
    (z/mapc (fn [p0 p1] (test/is (setup/equal-primitives? p0 p1))) ps0 ps1)))
;;------------------------------------------------------------------------------
;; one level of recursion

(test/deftest typical
  (let [ts0 (setup/typicals)
        f (io/file "tst" "typical.bin.gz")
        _ (typical/write-binary-file ts0 f)
        ts1 (typical/read-binary-file f)]
    (test/is (== 5 (count ts0) (count ts1)))
    (test/is (every? #(instance? Typical %) ts0))
    (test/is (every? #(instance? Typical %) ts1))
    (test/is (== 5.0 (typical/p-d (first ts0)))) 
    (test/is (== 5.5 (typical/p-d (second ts0))))
    (z/mapc (fn [t0 t1] (test/is (setup/equal-typicals? t0 t1))) ts0 ts1)))
;;------------------------------------------------------------------------------
;; two levels of recursion

(test/deftest change
  (let [cs0 (setup/changes)
        f (io/file "tst" "change.bin.gz")
        _ (change/write-binary-file cs0 f)
        cs1 (change/read-binary-file f)]
    (test/is (== 6 (count cs0) (count cs1)))
    (test/is (every? #(instance? Change %) cs0))
    (test/is (every? #(instance? Change %) cs1))
    (z/mapc (fn [c0 c1] (test/is (setup/equal-changes? c0 c1))) cs0 cs1)))
;;------------------------------------------------------------------------------