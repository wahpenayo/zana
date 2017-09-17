(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-08-31"
      :doc "Iteraable/Iterator wrappers for io" }

    zana.collections.io
  
  (:require [zana.commons.core :as tcc]
            [zana.io.gz :as gz])
  (:import [com.google.common.base Splitter]
           [clojure.lang IFn]
           [java.io Closeable]
           [java.util Collection HashMap IdentityHashMap Iterator Map Set]
           [java.util.concurrent Executors Future]
           [java.util.regex Pattern]))
;;------------------------------------------------------------------------------
;; Not thread-safe!
(deftype LineIterator [^java.io.BufferedReader r
                       ^:unsynchronized-mutable next-line]
  java.io.Closeable
  (close [this] (.close r))
  java.util.Iterator
  (hasNext [this]
    (when (nil? next-line) (set! next-line (.readLine r)))
    (not (nil? next-line)))
  (next [this]
    (if (.hasNext this)
      (let [line next-line]
        (set! next-line nil)
        line)
      (throw (java.util.NoSuchElementException.))))
  (remove [this]
    (throw (UnsupportedOperationException.))))
;;------------------------------------------------------------------------------
;; handles gzipped and zipped files
(defn lines ^java.util.Iterator [input]
  (LineIterator. (gz/reader input) nil))
;;------------------------------------------------------------------------------
(deftype LineIterable [input]
  Iterable
  (iterator [this] (lines input)))
;;------------------------------------------------------------------------------
;; Not thread-safe!
(deftype SplitIterator [^Splitter splitter ^LineIterator li]
  java.io.Closeable
  (close [this] (.close li))
  java.util.Iterator
  (hasNext [this] (.hasNext li))
  (next [this]
    (if (.hasNext this)
      (.split splitter (.next li))
      (throw (java.util.NoSuchElementException.))))
  (remove [this]
    (throw (UnsupportedOperationException.))))
;;------------------------------------------------------------------------------
(defn ^:no-doc split-lines ^java.util.Iterator [s input]
  (let [splitter (cond (instance? Splitter s) s
                       (instance? Character s) (Splitter/on (str s))
                       (string? s) (Splitter/on ^String s)
                       (instance? Pattern s) (Splitter/on ^Pattern s)
                       :else (throw (UnsupportedOperationException.
                                      (str "Can't split lines using " s))))]
    (SplitIterator. splitter (lines input))))
;;------------------------------------------------------------------------------
(defn next-split ^Iterable [^SplitIterator si] (.next si))
;;------------------------------------------------------------------------------
(deftype SplitIterable [splitter input]
  Iterable
  (iterator [this] (split-lines splitter (gz/reader input))))
;;------------------------------------------------------------------------------
;; TODO: multi method for more general cases.
;; Let splitter be a function.
(defn iterable
  (^Iterable [input]
    "Provides iterators over the lines in <input>."
    (LineIterable. input))
  (^Iterable [splitter input]
    "Provides iterators over split lines (themselves Iterables) in <input>."
    (SplitIterable. splitter input)))
;;------------------------------------------------------------------------------