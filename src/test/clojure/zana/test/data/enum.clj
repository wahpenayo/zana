(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-03-16"
      :doc "Tests for zana.data.enum." }
    
    zana.test.data.enum
  
  (:require [clojure.test :as test]
            [zana.api :as z]
            [zana.test.defs.data.oempty :as oempty]
            [zana.test.defs.data.primary-color :as primary-color]
            [zana.test.defs.data.saturated-color :as saturated-color]
            [zana.test.defs.data.uempty :as uempty])
  (:import [zana.data.enum Enumb]
           [zana.test.defs.data.oempty OEmpty]
           [zana.test.defs.data.primary_color PrimaryColor]
           [zana.test.defs.data.saturated_color SaturatedColor]
           [zana.test.defs.data.uempty UEmpty]))
;;------------------------------------------------------------------------------
(comment
  (test/run-tests 'zana.test.data.enum)
  )
;;------------------------------------------------------------------------------
;; edge case
(test/deftest empty-enum
  (test/is (thrown? AssertionError (oempty/singleton "foo")))
  (test/is (thrown? AssertionError (uempty/singleton "bar")))
  (test/is (z/descendant? Enumb OEmpty))
  (test/is (z/enum? OEmpty)))
;;------------------------------------------------------------------------------
(test/deftest color-enums
  (let [red0 (primary-color/singleton "red")
        green0 (primary-color/singleton "green")
        blue0 (primary-color/singleton "blue")
        red1 (saturated-color/singleton "red")]
    (test/is (thrown? AssertionError (primary-color/singleton "cyan")))
    (test/is (z/descendant? Enumb PrimaryColor))
    (test/is (z/enum? PrimaryColor))
    (test/is (z/enum? red0))
    (test/is (= (name red0) (name red1)))
    (test/is (= "red" (print-str  red0)))
    (test/is (not= red0 red1))
    (test/is (= [red0 green0 blue0] (sort [blue0 green0 red0])))))
;;------------------------------------------------------------------------------