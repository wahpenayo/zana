(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-10-20"
      :doc "Guava 2d tables"}
    
    zana.collections.table
  
  (:refer-clojure :exclude [get map])
  (:require [zana.commons.core :as tcc]
            [zana.collections.generic :as g])
  (:import [com.google.common.base Function]
           [com.google.common.collect ArrayTable HashBasedTable ImmutableList
            ImmutableList$Builder ImmutableTable Table Table$Cell Tables]
           [clojure.lang IFn]))
;;------------------------------------------------------------------------------
;; 2D Tables of data sets
;; TODO: 1D and nD tables?
;; TODO: y,x vs row,col argument ordering
;;------------------------------------------------------------------------------
(defn ^:no-doc table? [x] (instance? Table x))
(defn ^:no-doc get [^Table table r c] (.get table r c))
(defn ^:no-doc row-keys ^Iterable [^Table table] (.rowKeySet table))
(defn ^:no-doc nrows ^long [^Table table] (g/count (row-keys table)))
(defn ^:no-doc col-keys ^Iterable [^Table table] (.columnKeySet table))
(defn ^:no-doc ncols ^long [^Table table] (g/count (col-keys table)))
(defn ^:no-doc cells ^Iterable [^Table table]
  (assert table "no table!")
  (.cellSet table))
(defn ^:no-doc ncells ^long [^Table table] (.size table))
(defn ^:no-doc values ^Iterable [^Table table] (.values table))
(defn ^:no-doc row (^java.util.Map [^Table table r] (.row table r)))
(defn ^:no-doc col (^java.util.Map [^Table table c] (.column table c)))
(defn ^:no-doc cell-row [^Table$Cell cell] (.getRowKey cell))
(defn ^:no-doc cell-col [^Table$Cell cell] (.getColumnKey cell))
(defn ^:no-doc cell-value [^Table$Cell cell] (.getValue cell))
;;TODO: defmethod for generic map function
(defn ^:no-doc map ^com.google.common.collect.Table [f ^Table table]
  (assert (ifn? f))
  (assert table "no table to map over")
  (let [builder (ImmutableTable/builder)]
    (doseq [cell (cells table)]
      (assert (not (nil? cell)) (print-str "Nil cell:" table))
      (let [r (cell-row cell)
            c (cell-col cell)
            v (f cell)]
        (assert (not (nil? r)) (print-str "Nil cell-row:" cell))
        (assert (not (nil? c)) (print-str "Nil cell-col:" cell))
        (assert (not (nil? v)) (print-str "Nil value:" cell f))
        (.put builder r c v)))
    (.build builder)))
;;------------------------------------------------------------------------------
;; Aggregate data by 1 or 2, row and col functions
(defn ^:no-doc tabulate ^com.google.common.collect.Table [^IFn row
                                                          ^IFn col
                                                          ^Iterable data]
  (assert (or row col)) ;; at least 1 key function
  (assert (not (empty? data)))
  ;; TODO: tabulate into a multikey table, get rid of distinct call
  (let [row (if row row (constantly ""))
        col (if col col (constantly ""))
        tbl (HashBasedTable/create)]
    (doseq [datum data]
      (let [r (row datum)
            c (col datum)
            ^ImmutableList$Builder b (or (.get tbl r c)
                                         (ImmutableList/builder))]
        (.add b datum)
        (.put tbl r c b)))
    (doseq [r (.rowKeySet tbl)
            c (.columnKeySet tbl)]
      (let [^ImmutableList$Builder b (.get tbl r c)]
        (.put tbl r c (if b (.build b) (ImmutableList/of)))))
    (ImmutableTable/copyOf tbl)))
;;------------------------------------------------------------------------------
