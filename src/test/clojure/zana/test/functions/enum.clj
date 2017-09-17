(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-11-10"
      :doc "Tests for Enums." }
    
    zana.test.functions.enum
  
  (:require [clojure.test :as test]
            [zana.api :as z]
            [zana.test.functions.kolor :as kolor]
            [zana.test.functions.record :as record])
  (:import [zana.test.java Kolor]))
;; mvn -Dtest=zana.test.functions.enum clojure:test
;;------------------------------------------------------------------------------
(test/deftest kolor
  (test/is (= Double/TYPE (z/declared-value record/x0)))
  (test/is (= clojure.lang.Keyword (z/declared-value record/primate)))
  (test/is (= Kolor (z/declared-value record/kolor)))
  (test/is (z/enum-valued? record/kolor)))
;;------------------------------------------------------------------------------