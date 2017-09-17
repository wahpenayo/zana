(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-09-09"
      :doc "Function inversion." }

    zana.functions.inverse
  
  (:require [clojure.set :as set]))
;;------------------------------------------------------------------------------
(defprotocol Invertible
  (^clojure.lang.IFn ^:no-doc inverse [this]))

;; TODO: wrappers for java.lang.Math functions (eg sqrt) so we can special case
;; the inverses.
(extend-type clojure.lang.IFn
  Invertible
  (inverse [this] 
    (when (= identity this) identity)))

;; TODO: doesn't handle non-unique values in a predicable way. Could use guava
;; Multimap.invert for a more general solution.
(extend-type java.util.Map
  Invertible
  (inverse [this] (set/map-invert this)))

;; TODO: doesn't handle non-unique values in a predicable way. Could use guava
;; Multimap.invert for a more general solution.
(extend-type clojure.lang.PersistentArrayMap
  Invertible
  (inverse [this] (set/map-invert this)))
;;------------------------------------------------------------------------------