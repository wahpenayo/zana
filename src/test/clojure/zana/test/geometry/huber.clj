(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2018-02-24"
      :doc 
      "Tests for zana.java.geometry.Huber and HuberQR." }
     
    zana.test.geometry.huber

  (:require [clojure.test :as test])
  
  (:import [zana.java.geometry Huber HuberQR]))
;;------------------------------------------------------------------------------
#_(test/deftest Huber
     (let [i (z1/interval 0 1)
           [i0 i1] i]
       (test/is (= i0 (z1/lower i)))
       (test/is (= i1 (z1/upper i)))))
;;------------------------------------------------------------------------------