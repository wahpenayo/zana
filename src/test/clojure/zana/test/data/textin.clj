(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-05-19"
      :doc "Tests for zana.data.datum." }
    
    zana.test.data.textin
  
  (:require [clojure.java.io :as io]
            [clojure.test :as test]
            [zana.data.textin :as textin]))
;;------------------------------------------------------------------------------
;;  mvn -Dtest=zana.test.data.textin clojure:test
#_(test/run-tests 'zana.test.data.textin)
;;------------------------------------------------------------------------------
(test/deftest tuple-tree
  (let [header [:a [:b :c] :d]
        tokens [1 2 3]]
    (test/is (= (textin/tuple-tree header tokens)
                {:a 1 :b {:c 2} :d 3}))))
;;------------------------------------------------------------------------------
