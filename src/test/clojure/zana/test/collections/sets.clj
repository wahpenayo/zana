(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-07-27"
      :doc "Tests for zana.test.collections.maps." }
    
     zana.test.collections.sets
  
  (:require [clojure.test :as t]
            [zana.api :as z]))
;;------------------------------------------------------------------------------
(t/deftest test-distinct-identity
  (t/testing
    "distinct identity"
    (let [a [1 2 3]
          b (vector 1 2 3)
          s (z/distinct-identity [a b])]
      (t/is (not (identical? a b)))
      (t/is (= a b))
      (t/is (= 2 (z/count s)))
      )))
;;------------------------------------------------------------------------------