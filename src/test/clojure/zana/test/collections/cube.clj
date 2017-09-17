(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-09-09"
      :doc "Tests for zana.collections.cube" }
    
    zana.test.collections.cube
  
  (:require [clojure.test :as test]
            [clojure.pprint :as pp]
            [zana.test.defs.data.empty :as empty]
            [zana.test.defs.data.primitive :as primitive]
            [zana.test.defs.data.typical :as typical]
            [zana.test.defs.data.change :as change]
            [zana.test.data.setup :as setup]
            [zana.api :as z]))

;; mvn -Dtest=zana.test.collections.cube clojure:test
;;------------------------------------------------------------------------------
;; no recursion

;; TODO: test something
(test/deftest primitive
  (let [data (setup/primitives)
        cube (z/cube primitive/non-numerical-attributes data)]
;    (println cube)
;    (println)
;    (println (z/pprint-str cube))
;    (println)
;    (println (z/pprint-str (into {} cube)))
;    (println)
;    (println
;      (z/pprint-str
;        (z/map (fn [k v] (z/count v)) cube)))
    ))
;;------------------------------------------------------------------------------
