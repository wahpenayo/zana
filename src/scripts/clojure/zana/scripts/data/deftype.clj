(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-04-21"
      :doc "deftype vs defrecord space." }
    
    zana.scripts.data.deftype
  
  (:require [zana.api :as z]))
;;------------------------------------------------------------------------------
(deftype Foo [^int i ^double x ^String s])
(defrecord Bar [^int i ^double x ^String s])
;;------------------------------------------------------------------------------
(def foos (z/map #(Foo. (int %) (double 0.0) "s") (range 1000000))) 
(def bars (z/map #(Bar. (int %) (double 0.0) "s") (range 1000000))) 

