(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :since "2016-09-06"
      :date "2017-11-15"
      :doc "Text IO." }
    
    zana.data.textout
  
  (:require [clojure.string :as s]
            [clojure.pprint :as pp]
            [zana.commons.core :as cc]
            [zana.io.gz :as gz]
            [zana.collections.generic :as g]
            [zana.data.reflect :as r]))
;;----------------------------------------------------------------
;; Text Output
;;----------------------------------------------------------------
;; TODO: output format isn't the same as input format...
;; TODO: put public functions in data.api
;;----------------------------------------------------------------
(defn- field-header-tokens [field]
  (let [f (str field)
        c (r/type field)
        child-tokens (when (r/datum-class? c)
                       (eval (r/qualified-symbol c "header-tokens")))]
    (if-not (r/datum-class? c)
      [f]
      (mapv #(str f "-" %) child-tokens))))
;;----------------------------------------------------------------
(defn- emit-header-tokens [fields]
  (let [tokens (mapcat field-header-tokens fields)]
    `(def ~(with-meta 'header-tokens {:no-doc true}) [~@tokens])))
;;----------------------------------------------------------------
;; TODO: field to column header mapping
(defn- emit-header [fields]
  (let [sep (gensym "sep")]
    `((require '[clojure.string])
       (defn ~(with-meta 'header {:no-doc true}) 
         (~(with-meta `[~sep] {:tag 'java.lang.String})
           (clojure.string/join ~sep ~'header-tokens))
         ([] (~'header "\t"))))))
;;----------------------------------------------------------------
;; TODO: handle recursive datums with missing (nil) subobjects
(defn- field-value-string [field datum sep]
  (let [a `(~(r/accessor field) ~datum)
        c (r/type field)
        child-values (when (r/datum-class? c)
                       (r/qualified-symbol c "values-string"))]
    (if-not (r/datum-class? c)
      `(print-str ~a)
      `(~child-values ~a ~sep))))
;;----------------------------------------------------------------
(defn- emit-values-string [classname fields]
  (let [datum (gensym "datum")
        sep (gensym "sep")
        value-strings (mapv #(field-value-string % datum sep) fields)]
    `((require 'clojure.string)
       (defn ~(with-meta 'values-string {:no-doc true}) 
         (^String 
          [~(with-meta datum {:tag (r/munge classname)})
           ~(with-meta sep {:tag 'String})]
           (clojure.string/join ~sep [~@value-strings]))
         (^String [~(with-meta datum {:tag (r/munge classname)})]
           (~'values-string ~datum "\t"))))))
;;----------------------------------------------------------------
(defn tsv-file-writer [classname fields]
  (let [r (with-meta (gensym "r") {:tag (r/munge classname)})
        rs (with-meta (gensym "rs") {:tag 'Iterable})
        w (with-meta (gensym "w") {:tag 'java.io.PrintWriter})
        f (with-meta (gensym "f") {:tag 'java.io.File})
        sep (gensym "sep")]
    `((require '[clojure.java.io] '[zana.api])
       ~(emit-header-tokens fields)
       ~@(emit-header fields)
       ~@(emit-values-string classname fields)
       (defn ~(with-meta 'write-tsv-file {:deprecated "2016-09-06"})
         ~(str "Write the instances of <code>" classname 
               "</code> in <code>^Iterable " rs 
               "</code> to the file <code>" f 
               "</code>, with the field values separated by <code>" 
               sep
               "</code> (which defaults to <code>\"\\t\"</code>.<br>"
               "This implements a complicated and restrictive strategy for "
               "dealing with nested datum classes, and should be replaced by "
               "something simpler and more flexible.")
         ([~rs ~f ~sep]
           (assert (not (empty? ~rs)))
           (assert (not (nil? ~f)))
           (assert (string? ~sep))
           (clojure.java.io/make-parents ~f)
           (with-open [~w (zana.api/print-writer ~f)]
             (.println ~w (~'header ~sep))
             (zana.api/mapc
               (fn [~r] (.println ~w (~'values-string ~r ~sep)))
               ~rs)))
         ([~rs ~f] (~'write-tsv-file ~rs ~f "\t"))))))
;;----------------------------------------------------------------