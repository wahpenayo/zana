(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-09-08"
      :doc "Tests for zana.geometry.z1." }
     zana.test.geometry.z1
  (:require
    [clojure.test :as test]
    [zana.geometry.z1 :as z1]))
;;------------------------------------------------------------------------------
(comment
  (test/run-tests 'zana.test.geometry.z1)
  )
;;------------------------------------------------------------------------------
;(test/deftest destructuring
;  (test/testing
;    "destructuring"
;    (let [i (z1/interval 0 1)
;          [i0 i1] i]
;      (test/is (= i0 (z1/lower i)))
;      (test/is (= i1 (z1/upper i))))))
;;------------------------------------------------------------------------------