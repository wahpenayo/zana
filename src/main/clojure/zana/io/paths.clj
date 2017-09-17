(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-08-31"
      :doc "Filesystem stuff." }
    
    zana.io.paths
  
  (:require [clojure.pprint :as pp]
            [clojure.string :as s]
            [zana.collections.generic :as g]))
;;------------------------------------------------------------------------------
(defn filename "For example, \"foo.txt\"" ^String [^java.io.File f] (.getName f))
(defn pathname "" ^String [^java.io.File f] (.getPath f))
(defn parent-file "" ^java.io.File [^java.io.File f] (.getParent f))
(defn mkdir "" ^java.io.File [^java.io.File f] (.mkdir f))
(defn mkdirs "" ^java.io.File [^java.io.File f] (.mkdirs f))
;;------------------------------------------------------------------------------
(defn file? "" [f] (instance? java.io.File f))
(defn file-exists? "" [^java.io.File f] (.exists f))
(defn directory? "" [^java.io.File f] (.isDirectory f))
;;------------------------------------------------------------------------------
(defn- filter-ls ^Iterable [^clojure.lang.IFn p ^java.io.File folder]
  (g/filter p (java.util.Arrays/asList (.listFiles folder))))
(defn- predicate ^clojure.lang.IFn [p]
  (cond (ifn? p) 
        p
        (instance? java.util.regex.Pattern p)
        (fn match [^java.io.File f] (re-find p (pathname f)))
        
        (instance? String p)
        (fn match [^java.io.File f] (re-find (re-pattern p) (pathname f)))
        
        :else
        (throw (IllegalArgumentException.
                 (pr-str "Can't create a predicate function from" p)))))
;;------------------------------------------------------------------------------
(defn ls 
  "List the files in <code>folder</code> that satisfy <code>p</code>,
  or all the files if <code>p</code> is not supplied." 
  (^Iterable [p ^java.io.File folder]
    (when (file-exists? folder)
      (assert (directory? folder) 
              (print-str "Not a directory:\n" (pathname folder)))
      (filter-ls (predicate p) folder)))
  (^Iterable [^java.io.File folder] 
    (when (file-exists? folder)
      (assert (directory? folder) 
              (print-str "Not a directory:\n" (pathname folder)))
      (java.util.Collections/unmodifiableList
        (java.util.Arrays/asList 
          (.listFiles folder))))))
;;------------------------------------------------------------------------------
(defn delete-files

  "Delete any file under <code>f</code> for which <code>(p f)</code> is truthy.<br>
   If <code>f</code> is a directory, delete its contents first."
  
  ([p ^java.io.File f]
    (let [p (predicate p)]
      (if (directory? f)
        (if (p f)
          ;; delete everything
          (do 
            (g/mapc delete-files (ls f))
            (.delete f))
          ;; else look for more files that match the predicate
          (g/mapc #(delete-files p %) (ls f)))
      (when (p f) (.delete f)))))
  ([^java.io.File f] (delete-files (constantly true) f)))
;;------------------------------------------------------------------------------
