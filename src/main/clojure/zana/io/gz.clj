(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com"
      :since "2015-11-17"
      :date "2017-11-16"
      :doc "File reader/writer with compression based on name." }
    
    zana.io.gz
  
  (:require [clojure.java.io :as io])
  (:import [java.io 
            BufferedInputStream BufferedOutputStream 
            BufferedReader BufferedWriter File FileInputStream 
            FileOutputStream FileReader FileWriter InputStream 
            InputStreamReader ObjectInputStream 
            ObjectOutputStream OutputStream OutputStreamWriter 
            PrintWriter Reader]
           [java.util.zip GZIPInputStream GZIPOutputStream 
            ZipEntry ZipInputStream ZipOutputStream]
           [org.apache.commons.compress.compressors.bzip2
            BZip2CompressorInputStream 
            BZip2CompressorOutputStream]))
;; TODO: unit tests
;;----------------------------------------------------------------
;; readers
;;----------------------------------------------------------------
(defn- gz-input-stream ^java.io.InputStream [x]
  (GZIPInputStream. (io/input-stream x)))

(defn- zip-input-stream ^java.io.InputStream [x]
  (let [zis (ZipInputStream. (io/input-stream x))
        ze (.getNextEntry zis) ]
    (assert ze)
    (assert (not (.isDirectory ze)))
    zis))

(defn- bzip2-input-stream ^java.io.InputStream [x]
  (BZip2CompressorInputStream. (io/input-stream x)))
;;----------------------------------------------------------------
;; TODP: could use Commons compress to detect the input type?
(defn input-stream
  "Create an input stream that handles compression based on file 
   name ending."
  ^java.io.InputStream [x]
  (assert (not (nil? x)) "Can't create an input stream for nil")
  (let [^String name (str x)]
    (cond (.endsWith name ".gz") (gz-input-stream x)
          (.endsWith name ".svgz") (gz-input-stream x)
          (.endsWith name ".zip") (zip-input-stream x)
          (.endsWith name ".kmz") (zip-input-stream x)
          (.endsWith name ".bz2") (bzip2-input-stream x)
          :else (io/input-stream x))))
;;----------------------------------------------------------------
(defn object-input-stream
  "Create an object input stream that handles compression based on 
   file name ending."
  ^java.io.ObjectInputStream [x]
  (ObjectInputStream. (BufferedInputStream. (input-stream x))))
;;----------------------------------------------------------------
(defn reader
  "Create a reader that handles compression based on file name 
   ending."
  ^java.io.BufferedReader [x]
  (assert (not (nil? x)) "Can't create an input stream for nil")
  (cond (instance? BufferedReader x) 
        x
        (instance? Reader x) 
        (BufferedReader. x)
        (instance? InputStream x)
        (BufferedReader. (InputStreamReader. x))
        :else 
        (BufferedReader. (InputStreamReader. (input-stream x)))))
;;----------------------------------------------------------------
;; output streams
;;----------------------------------------------------------------
(defn- gz-output-stream ^java.io.OutputStream [^String name]
  (GZIPOutputStream. (FileOutputStream. name)))
(defn- bzip2-output-stream ^java.io.OutputStream [^String name]
  (BZip2CompressorOutputStream. 
    (BufferedOutputStream.
      (FileOutputStream. name))))
(defn- zip-output-stream ^java.io.OutputStream [^String path]
  (let [file (File. path)
        name (.getName file)
        zos (ZipOutputStream. (FileOutputStream. file) )
        end (- (.length name) 4)
        ^String prefix (.substring name 0 end)
        ze  (cond (.endsWith name ".zip") 
                  (ZipEntry. prefix)
                  (.endsWith name ".kmz") 
                  (ZipEntry. (str prefix ".kml"))
                  :else 
                  (throw (IllegalArgumentException.
                           (str "Can't handle " file))))]
    (.putNextEntry zos ze)
    zos))
;;----------------------------------------------------------------
(defn output-stream
  "Create an output-stream that handles compression based on file 
   name ending."
  ^java.io.OutputStream [f]
  (let [^String name (str f)]
    (cond (.endsWith name ".gz") (gz-output-stream name)
          (.endsWith name ".zip") (zip-output-stream name)
          (.endsWith name ".kmz") (zip-output-stream name)
          (.endsWith name ".svgz") (gz-output-stream name)
          :else (FileOutputStream. name))))
;;----------------------------------------------------------------
(defn object-output-stream
  "Create an object output stream that handles compression based 
   on file name ending."
  ^java.io.ObjectOutputStream [f]
  (ObjectOutputStream. (BufferedOutputStream. (output-stream f))))
;;----------------------------------------------------------------
;; writers
;;----------------------------------------------------------------
(defn writer
  "Create a writer that handles compression based on file name 
   ending."
  ^java.io.BufferedWriter [f]
  (BufferedWriter. (OutputStreamWriter. (output-stream f))))
;;----------------------------------------------------------------
(defn print-writer
  "Create a writer that handles compression based on file name 
   ending."
  ^java.io.PrintWriter [f] (PrintWriter. (writer f)))
;;----------------------------------------------------------------