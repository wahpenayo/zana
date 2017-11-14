(set! *warn-on-reflection* true)
(set! *unchecked-math* false) ;; warnings in cheshire.generate
(ns ^{:author "wahpenayo at gmail dot com" 
      :since "2017-11-13"
      :date "2017-11-13"
      :doc "Generally useful cheshire (JSON) encoders.
            <p>
            Decoding (input) not yet supported, because
            obvious JSON encoding is lossy." }
    
    zana.io.cheshire
  
  (:require [cheshire.generate]))
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(doseq [a [(boolean-array 0) (char-array 0) (double-array 0)
           (float-array 0) (int-array 0) (long-array 0)
           (short-array 0) (object-array 0)]]
  (cheshire.generate/add-encoder 
    (class a) 
    (fn encoder [x json-generator]
      (cheshire.generate/encode-seq x json-generator))))
;;----------------------------------------------------------------
