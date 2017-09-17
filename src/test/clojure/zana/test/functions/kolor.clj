(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed) 
(ns ^{:author "John Alan McDonald" :date "2016-11-10"
      :doc "Enum data type for zana unit tests." }

    zana.test.functions.kolor
  
  (:require [zana.api :as z])
  (:import [zana.test.java Kolor]))
;;------------------------------------------------------------------------------
(def kolors [Kolor/RED Kolor/YELLOW Kolor/GREEN 
             Kolor/CYAN Kolor/BLUE Kolor/MAGENTA])
(defn generator [seed] (z/random-element-generator kolors seed))
(def ^java.util.EnumSet primaries
  (java.util.EnumSet/of Kolor/RED Kolor/GREEN Kolor/BLUE))
(defn primary? [^Kolor k] (.contains primaries k))
;;------------------------------------------------------------------------------
(defmethod z/clojurize Kolor [^Kolor this] (.name this))
(defmethod print-method 
  zana.test.java.Kolor 
  [^Kolor this ^java.io.Writer w]
  (.write w (.toString this)))
;;------------------------------------------------------------------------------
(z/add-edn-readers! 
  {'zana.test.java.Kolor #(Kolor/valueOf zana.test.java.Kolor %)})
;;------------------------------------------------------------------------------
