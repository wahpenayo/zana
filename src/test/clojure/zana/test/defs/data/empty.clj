(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-03-30"
      :doc "Tests for zana.data.datum." }

    zana.test.defs.data.empty
  
  (:require [zana.api :as z]))
;;------------------------------------------------------------------------------
#_(binding [*print-meta* true]
  (require '[clojure.pprint])
  (clojure.pprint/pprint
    (macroexpand
      '(z/define-datum Empty []))))
;;------------------------------------------------------------------------------
(z/define-datum Empty [])
;;------------------------------------------------------------------------------