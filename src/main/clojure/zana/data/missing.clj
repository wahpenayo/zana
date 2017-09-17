(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-11-07"
      :doc "Missing data" }
    
    zana.data.missing
  
  (:require [zana.collections.generic :as g])
  (:import [java.util Arrays Collections HashSet]
           [com.carrotsearch.hppc DoubleArrayList LongHashSet]))
;;------------------------------------------------------------------------------
;; TODO: what about clojure.lang.IFn$OL attributes? No missing data allowed?
;; TODO: what if we want to return NaN as a possible category of a categorical
;; attribute? Hack>> use :NaN or something like that.
;;------------------------------------------------------------------------------
(defn missing? 
  "For a <code>double</code> values missing == <code>Double/NaN</code>.
   For <code>Object</code> values, missing == <code>nil</code>, 
   <code>Double/NaN</code>, or <code>Float/NaN</code>."
  ([zi]
    (or (nil? zi)
        (and (instance? Double zi) (.isNaN ^Double zi))
        (and (instance? Float zi) (.isNaN ^Float zi))))
  ([^clojure.lang.IFn z datum]
    (if (instance? clojure.lang.IFn$OD z) 
      (Double/isNaN (.invokePrim ^clojure.lang.IFn$OD z datum))
      (missing? (z datum)))))
;;------------------------------------------------------------------------------
(defn- not-missing? [^clojure.lang.IFn z datum] (not (missing? z datum)))
;;------------------------------------------------------------------------------
;; counting distinct non-missing data
;;------------------------------------------------------------------------------
(defn count-distinct-finite-doubles 
  "Return the number of the distinct finite (not infinite or NaN) values of z 
   over data."
  ^long [^clojure.lang.IFn$OD z ^Iterable data]
  ;; see http://issues.carrot2.org/browse/HPPC-144
  (let [hs (LongHashSet.)]
    (g/mapc 
      #(let [zi (.invokePrim z %)] 
         (when (Double/isFinite zi) (.add hs (Double/doubleToLongBits zi))))
      data)
    (.size hs)))
;;------------------------------------------------------------------------------
(defn count-distinct-longs 
  "Return the number of distinct long values of z over data.
   No support at present for missing integer-valued data."
  ^long [^clojure.lang.IFn$OL z ^Iterable data]
  (let [hs (LongHashSet.)]
    (g/mapc #(.add hs (.invokePrim z %)) data)
    (.size hs)))
;;------------------------------------------------------------------------------
(defn- count-distinct-objects 
  "Return the number of distinct non-nil values of z over data."
  ^long [^clojure.lang.IFn z ^Iterable data]
  (assert (ifn? z) (print-str "Not a function:" z))
  (let [hs (HashSet.)]
    (g/mapc #(let [zi (z %)] (when-not (nil? zi) (.add hs zi))) data)
    (.size hs)))
;;------------------------------------------------------------------------------
(defn count-distinct-not-missing
  "Return the number of distinct non-missing/non-infinite values of z over data."
  ^long [^clojure.lang.IFn z ^Iterable data]
  (assert (ifn? z) (print-str "Not a function:" z))
  (cond (instance? clojure.lang.IFn$OD z) (count-distinct-finite-doubles z data)
        (instance? clojure.lang.IFn$OL z) (count-distinct-longs z data)
        :else (count-distinct-objects z data)))
;;------------------------------------------------------------------------------
;; collecting distinct non-missing values
;; TODO: group-by distinct non-missing values
;;------------------------------------------------------------------------------
(defn distinct-finite-doubles 
  "Return a collection of the distinct finite (not infinite or NaN) values of z 
   over data."
  ^Iterable [^clojure.lang.IFn$OD z ^Iterable data]
  ;; see http://issues.carrot2.org/browse/HPPC-144
  (let [hs (LongHashSet.)
        _ (g/mapc 
            #(let [zi (.invokePrim z %)] 
               (when (Double/isFinite zi) 
                 (.add hs (Double/doubleToLongBits zi))))
            data)
        n (.size hs)
        ls (.toArray hs)
        ds (double-array n)]
    (dotimes [i n] (aset-double ds i (Double/longBitsToDouble (aget ls i))))
    (Collections/unmodifiableList (Arrays/asList ds))))
;;------------------------------------------------------------------------------
(defn distinct-longs 
  "Return a collection of the distinct long values of z over data.
   No support at present for missing integer-valued data."
  ^Iterable [^clojure.lang.IFn$OL z ^Iterable data]
  (let [hs (LongHashSet.)]
    (g/mapc #(.add hs (.invokePrim z %)) data)
    (Collections/unmodifiableList (Arrays/asList (.toArray hs)))))
;;------------------------------------------------------------------------------
(defn- distinct-objects 
  "Return a collection of the distinct non-nil values of z over data."
  ^Iterable [^clojure.lang.IFn z ^Iterable data]
  (let [hs (HashSet.)]
    (g/mapc #(let [zi (z %)] (when-not (missing? zi) (.add hs zi))) data)
    (Collections/unmodifiableSet hs)))
;;------------------------------------------------------------------------------
(defn distinct-not-missing
  "Return a collection of the distinct non-missing values of z over data."
  ^Iterable [^clojure.lang.IFn z ^Iterable data]
  (cond (instance? clojure.lang.IFn$OD z) (distinct-finite-doubles z data)
        (instance? clojure.lang.IFn$OL z) (distinct-longs z data)
        :else (distinct-objects z data)))
;;------------------------------------------------------------------------------
;; Special cases for plotting, where we might want to requite multiple attibutes
;; to be not missing.
(defn count-not-missing
  "Return the number of elements of <code>data</code> where none of the
   function arguments return a missing value. For a <code>double</code> valued 
   function (<code>clojure.lang.IFn$OD</code>) missing == <code>Double/NaN</code>.
   For <code>Object</code> valued functions, missing == <code>nil</code>, 
   <code>Double/NaN</code>, or <code>Float/NaN</code>."
  (^long [^clojure.lang.IFn z ^Iterable data]
    (g/count #(not-missing? z %) data))
  (^long [^clojure.lang.IFn x ^clojure.lang.IFn y ^Iterable data]
    (g/count #(and (not-missing? x %) 
                   (not-missing? y %))
             data))
  (^long [^clojure.lang.IFn x 
          ^clojure.lang.IFn y 
          ^clojure.lang.IFn z 
          ^Iterable data]
    (g/count #(and (not-missing? x %) 
                   (not-missing? y %) 
                   (not-missing? z %))
             data))
  (^Long [^clojure.lang.IFn u 
          ^clojure.lang.IFn x 
          ^clojure.lang.IFn y 
          ^clojure.lang.IFn z 
          ^Iterable data]
    (g/count #(and (not-missing? u %) 
                   (not-missing? x %) 
                   (not-missing? y %) 
                   (not-missing? z %))
             data))
  (^Long [^clojure.lang.IFn u 
          ^clojure.lang.IFn v 
          ^clojure.lang.IFn x 
          ^clojure.lang.IFn y 
          ^clojure.lang.IFn z 
          ^Iterable data]
    (g/count #(and (not-missing? u %) 
                   (not-missing? v %) 
                   (not-missing? x %) 
                   (not-missing? y %) 
                   (not-missing? z %))
             data)))
;;------------------------------------------------------------------------------
;; Special cases for plotting, where we might want to requite multiple attibutes
;; to be not missing.
(defn drop-missing
  "Return the elements of <code>data</code> where none of the
   function arguments return a missing value. For a <code>double</code> valued 
   function (<code>clojure.lang.IFn$OD</code>) missing == <code>Double/NaN</code>.
   For <code>Object</code> valued functions, missing == <code>nil</code>, 
   <code>Double/NaN</code>, or <code>Float/NaN</code>"
  
  (^Iterable [^clojure.lang.IFn z ^Iterable data]
    (g/filter #(not-missing? z %) data))
  
  (^Iterable [^clojure.lang.IFn x ^clojure.lang.IFn y ^Iterable data]
    (let [f (if (instance? clojure.lang.IFn$OD x)
              (if (instance? clojure.lang.IFn$OD y)
                #(let [^clojure.lang.IFn$OD x x
                       ^clojure.lang.IFn$OD y y]
                   (not (or (Double/isNaN (.invokePrim x %))
                            (Double/isNaN (.invokePrim y %)))))
                #(let [^clojure.lang.IFn$OD x x]
                   (and (not (Double/isNaN (.invokePrim x %)))
                            (not-missing? y %))))
              (if (instance? clojure.lang.IFn$OD y)
                #(let [^clojure.lang.IFn$OD y y]
                   (and (not-missing? x %)
                        (not (Double/isNaN (.invokePrim y %)))))
                #(and (not-missing? x %) (not-missing? y %))))]
    (g/filter f data)))
  
  (^Iterable [^clojure.lang.IFn x 
              ^clojure.lang.IFn y 
              ^clojure.lang.IFn z 
              ^Iterable data]
    (let [f (if (instance? clojure.lang.IFn$OD y)
              (if (instance? clojure.lang.IFn$OD x)
                (if (instance? clojure.lang.IFn$OD z)
                  #(let [^clojure.lang.IFn$OD x x
                         ^clojure.lang.IFn$OD y y
                         ^clojure.lang.IFn$OD z z]
                   (not (or (Double/isNaN (.invokePrim x %))
                            (Double/isNaN (.invokePrim y %))
                            (Double/isNaN (.invokePrim z %)))))
                  #(let [^clojure.lang.IFn$OD x x
                         ^clojure.lang.IFn$OD y y]
                     (and (not (or (Double/isNaN (.invokePrim x %))
                                   (Double/isNaN (.invokePrim y %))))
                          (not-missing? z %))))
                #(let [^clojure.lang.IFn$OD y y]
                   (and (not (Double/isNaN (.invokePrim y %)))
                        (not-missing? x %)
                        (not-missing? z %))))
              #(and (not-missing? x %) 
                    (not-missing? y %) 
                    (not-missing? z %)))]
    (g/filter f data)))
  
  (^Iterable [^clojure.lang.IFn u 
              ^clojure.lang.IFn x 
              ^clojure.lang.IFn y 
              ^clojure.lang.IFn z 
              ^Iterable data]
    (g/filter #(and (not-missing? u %) 
                    (not-missing? x %) 
                    (not-missing? y %) 
                    (not-missing? z %))
              data))
  (^Iterable [^clojure.lang.IFn u 
              ^clojure.lang.IFn v 
              ^clojure.lang.IFn x 
              ^clojure.lang.IFn y 
              ^clojure.lang.IFn z 
              ^Iterable data]
    (g/filter #(and (not-missing? u %) 
                    (not-missing? v %) 
                    (not-missing? x %) 
                    (not-missing? y %) 
                    (not-missing? z %))
              data)))
;;------------------------------------------------------------------------------
;; finite/infinite/NaN only applies to numerical attributes, but we could have
;; an object-valued attribute function returning Double or Float.
;;------------------------------------------------------------------------------
(defn finite? 
  "<code>(z datum)</code> is not NaN or infinite."
  [^clojure.lang.IFn z datum]
  (if (instance? clojure.lang.IFn$OD z) 
    (Double/isFinite (.invokePrim ^clojure.lang.IFn$OD z datum))
    (let [zi (z datum)]
      (or (nil? zi)
          (and (instance? Double zi) (not (.isInfinite ^Double zi)))
          (and (instance? Float zi) (not (.isInfinite ^Float zi)))))))
#_(defn select-finite 
  ^java.util.Collection [^clojure.lang.IFn z data] 
  (g/filter #(finite? z %) data))
;;------------------------------------------------------------------------------
(defn- select-finite-doubles ^doubles [^clojure.lang.IFn$OD z ^Iterable data]
  (let [i (g/iterator data)
        dal (DoubleArrayList. (g/count data))]
    (while (.hasNext i)
      (let [zi (.invokePrim z (.next i))]
        (when (Double/isFinite zi) (.add dal zi))))
    (.toArray dal)))

(defn select-finite-values 
  "Return a <code>double</code> array of the finite values of <code>z</code>
   applied to the elements of <code>data</code>."
  ^doubles [^clojure.lang.IFn z ^Iterable data]
  (if (instance? clojure.lang.IFn$OD z)
    (select-finite-doubles z data)
    (let [i (g/iterator data)
          dal (DoubleArrayList. (g/count data))]
      (while (.hasNext i)
        (let [zi (double (z (.next i)))]
          (when (Double/isFinite zi) (.add dal zi))))
      (.toArray dal))))
;;------------------------------------------------------------------------------