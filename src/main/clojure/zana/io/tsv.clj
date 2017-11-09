(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :since "2017-11-08"
      :date "2017-11-08"
      :doc "Read and write tab-separated files.
           <p>
           Reading not yet implemented.
           Reading should produce a list of maps, where the keys
           are keywords derived from sanitized column headers
           and the values are produced by applying a parser 
           function specified per column.)" }
    
    zana.io.tsv
  
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [zana.collections.generic :as zgc]
            [zana.io.gz :as gz])
  
  (:import [java.util Map]
           [java.io File]))
;;----------------------------------------------------------------
(defn write-tsv-file 
  "Write the chosen <code>attributes</cpde>data</code> to the
   file <code>f</code>, seperating the value strings by 
   <codesep</code>.
   <dl>
   <dt><code>^Map attributes</code></dt>
   <dd>A map from <code>Keyword</code> to function.
       The name of the keyword is used as a column header.
       The function is applied to each element of 
       <code>data</code> to get the corresponding column values,
       which are converted to strings by calling 
       <code>clojure.core/str</code>. 
       <i>(TODO: a map of attribute keyword to stringify function 
       as another arg?).</i>
    </dd>
    <dt><code>^Iterable data</code></dt>
    <dd>An iterable containing anything to which the attribute
        functions can  be applied. Must be finite, or the writing
        will never end.
    </dd>
    <dt><code>^File f</code></dt>
    <dd>The file to write the data to. 
       <i>(TODO: could generalize beyound files.)</i>
    </dd>
    <dt><code>^String sep</code></dt> 
    <dd>String written between every 2 values in each 
        output row. <b>Note:</b> no escaping of the output values
        is done, so output will be broken if any value string 
        contains <code>sep</code>. 
        <i>(TODO: stringify function map would permit user work-
        around.</i>
     </dd>"
  ([^Map attributes ^Iterable data ^File f ^String sep]
    (assert (every? keyword? (keys attributes)))
    (assert (every? fn? (vals attributes)))
    (assert (not (zgc/empty? data)))
    (assert (not (nil? f)))
    (assert (and (not (empty? sep)) (string? sep)))
    (io/make-parents f)
    (let [ks (sort-by name (keys attributes))
          header (s/join sep (map name ks))
          line (fn line [datum]
                 (s/join 
                   sep 
                   (map (fn value-string [k] 
                          (str ((.get attributes k) datum)))
                        ks)))]
      (with-open [w (gz/print-writer f)]
        (.println w header)
        (zgc/mapc #(.println w (line %)) data))))
  ([^Map attributes ^Iterable data ^File f] 
    (write-tsv-file attributes data f "\t")))
;;----------------------------------------------------------------
;; TODO: read-tsv-file
;;----------------------------------------------------------------
