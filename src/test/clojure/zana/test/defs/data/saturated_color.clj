(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-03-16"
      :doc "Tests for zana.data.enum." }
     
    zana.test.defs.data.saturated-color
  
  (:require [zana.api :as z]))
;;------------------------------------------------------------------------------
(z/define-ordered-enum SaturatedColor ["red" "green" "blue" "cyan" "magenta" "yellow"])
;;------------------------------------------------------------------------------