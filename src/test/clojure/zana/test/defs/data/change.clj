(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com"
      :date "2018-01-31"
      :doc "Tests for zana.data.datum." }
    
    zana.test.defs.data.change
  
  (:require [zana.api :as z]
            [zana.test.defs.data.typical :as typical])
  (:import [zana.test.defs.data.typical Typical]))
;;----------------------------------------------------------------
(z/define-datum Change [^Typical before ^Typical after])
;;----------------------------------------------------------------
#_(binding [*print-meta* true]
    (require '[clojure.pprint])
    (clojure.pprint/pprint
      (macroexpand
        '(z/define-datum Change
           [^Typical before 
            ^Typical after]))))
;;----------------------------------------------------------------
