(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-09-16"
      :doc "Data definition macro utilities." }
    
    zana.data.binaryio
  
  (:require [clojure.string :as s]
            [clojure.pprint :as pp]
            [zana.commons.core :as cc]
            [zana.io.gz :as gz]
            [zana.collections.generic :as g]
            [zana.data.reflect :as r]))
;;------------------------------------------------------------------------------
;; Binary Output
;;------------------------------------------------------------------------------
;; TODO: DateTime, etc., support
;; TODO: customize thru field-spec
;; TODO: define writeObject methods for datum classes, instead of write-binary
;; function.

(defn- binary-field-writer [field r o]
  (let [hint (:tag (meta field))
        c (r/type field)
        accessor (r/accessor field)
        o (with-meta o nil)
        x (gensym "x")
        xm (with-meta x {:tag c})]
    ;; TODO: check if c is Serializable
    (case hint
      boolean `(.writeBoolean ~o (boolean (~accessor ~r)))
      char `(.writeChar ~o (int (~accessor ~r))) ;; .writeChar takes int not char!
      byte `(.writeByte ~o (byte (~accessor ~r)))
      short `(.writeShort ~o (short (~accessor ~r)))
      int `(.writeInt ~o (int (~accessor ~r)))
      long `(.writeLong ~o (long (~accessor ~r)))
      float `(.writeFloat ~o (float (~accessor ~r)))
      double `(.writeDouble ~o (double (~accessor ~r)))
      (String java.lang.String) `(let [~xm (~accessor ~r)
                                       ~xm (or ~xm "nil")]
                                   (.writeUTF ~o ~xm))
      ;; TODO: is this better than just .writeObject?
      ;; TODO: or y,m,d,h,s as int,byte,byte,byte,byte?
      ;;java.time.LocalDate `(let [~xm (~accessor ~r)
      ;;                           ~xm (or (str ~xm) "nil")]
      ;;                       (.writeUTF ~o ~xm))
      ;;java.time.LocalDateTime `(let [~xm (~accessor ~r)
      ;;                               ~xm (or (str ~xm) "nil")]
      ;;                          (.writeUTF ~o ~xm))
      ;; else
      (if (r/datum-class? c)
        `(~(r/qualified-symbol c "write-binary") ~o (~accessor ~r))
        `(let [~xm (~accessor ~r)] (.writeObject ~o ~x))))))
;;------------------------------------------------------------------------------
;; TODO: this should be a local fn inside binary-file-writer

(defn binary-object-writer [classname fields]
  (let [c (str (r/munge classname))
        r (with-meta (gensym "r") {:tag (r/munge classname)})
        o (with-meta (gensym "o") {:tag 'java.io.ObjectOutput})]
    `(defn ~'write-binary 
       ~(str "Write the <code>^" classname " " r
             "</code> to <code>^java.io.ObjectOutput " o
             "</code> so that it can later be read with [[read-binary]].")
       [~o ~r]
       ;; classname first, to handle empty datum edge case, and safety
       (.writeUTF ~o ~c) 
       ~@(mapv #(binary-field-writer % r o) fields))))
;;------------------------------------------------------------------------------
;; Note: writing individual records to the file, rather than the collection.
;; This mean the binary file reader has to catch EOFException to determine the 
;; end of the input. 
(defn binary-file-writer [classname]
  (let [r (with-meta (gensym "r") {:tag (r/munge classname)})
        rs (with-meta (gensym "rs") {:tag 'Iterable})
        o (with-meta (gensym "o") {:tag 'java.io.ObjectOutput})
        f (with-meta (gensym "f") {:tag 'java.io.File})]
    `((require '[zana.api])
       (defn ~'write-binary-file 
         ~(str "Write the instances of <code>" classname 
               "</code> in <code>^Iterable " rs 
               "</code> to the file <code>" f 
               "</code> using [[write-binary]], so that the file"
               " can later be read with [[read-binary-file]].")
         [~rs ~f]
         (clojure.java.io/make-parents ~f)
         (with-open [~o (zana.api/object-output-stream ~f)]
           (zana.api/mapc
             (fn [~r] (~'write-binary ~o ~r))
             ~rs))))))
;;------------------------------------------------------------------------------
;; Binary Input
;;------------------------------------------------------------------------------
(defn- binary-field-reader [field i a]
  (let [i (with-meta i nil)
        a (with-meta a nil)
        s (gensym "s")
        hint (:tag (meta field))
        c (r/type field)]
    ;; TODO: check if c is Serializable
    (case hint
      boolean `(boolean (.readBoolean ~i))
      char `(char (.readChar ~i))
      byte `(byte (.readByte ~i))
      short `(short (.readShort ~i))
      int `(int (.readInt ~i))
      long `(long (.readLong ~i))
      float `(float (.readFloat ~i))
      double `(double (.readDouble ~i))
      (String java.lang.String) `(let [~s (.readUTF ~i)]
                                   (when (not= "nil" ~s) (~a ~s)))
      ;; TODO: is this any better than just .readObject?
      ;; TODO: y,m,d,h,s as int,byte,byte,byte,byte?
      ;; java.time.LocalDate `(~a (java.time.LocalDate/parse (.readUTF ~i)))
      ;; java.time.LocalDateTime `(~a (java.time.LocalDateTime/parse (.readUTF ~i)))
      ;; else 
      (if (r/datum-class? c)
        ;; datum classes currently have identity semantics, so we can't de-dupe instances
        `(~(r/qualified-symbol c "read-binary") ~i ~a) 
        `(~a (.readObject ~i ))))))
;;------------------------------------------------------------------------------
;; TODO: this should be a local fn inside binary-file-reader...

(defn binary-object-reader [classname fields]
  (let [i (with-meta (gensym "i") {:tag 'java.io.ObjectInput})
        a (with-meta (gensym "a") {:tag 'clojure.lang.IFn})
        c (with-meta (gensym "c") {:tag 'String})
        mc (str (r/munge classname))
        eof (gensym "eof")]
    ;; EOF is the only way to detect end of input when we write individual
    ;; objects to the output stream.
    ;; Alternatives are to write a Collection, or to adopt a convention where 
    ;; the number of instances is the first thing in the file.
    `(defn ~'read-binary 
       ~(str "Read an instance of <code>" classname 
             "</code> from the <code>^java.io.ObjectInput " i 
             "</code>, assuming it was written with [[write-binary]]."
             " Call the archetyper function <code>" a 
             "</code> to de-dupe (like <code>String/intern</code>"
             " any <code>Object</code> valued fields.")
       ~(with-meta `[~i ~a] {:tag (r/munge classname)})
       (try
         ;; classname first, to handle empty datum edge case, and safety
         (let [~c (.readUTF ~i)]
           (assert (= ~c ~mc) (pr-str ~c ~mc))
           (~(r/constructor classname)
             ~@(mapv #(binary-field-reader % i a) fields)))
         (catch java.io.EOFException ~eof nil)))))
;;------------------------------------------------------------------------------
(defn binary-file-reader [classname]
  (let [i (with-meta (gensym "i") {:tag 'java.io.ObjectInput})
        f (with-meta (gensym "f") {:tag 'java.io.File})
        args (with-meta [f] {:tag 'Iterable})
        a (with-meta (gensym "a") {:tag 'clojure.lang.IFn})
        b (with-meta (gensym "b") {:tag 'com.google.common.collect.ImmutableList$Builder})
        r (with-meta (gensym "r") {:tag (r/munge classname)})]
    `((require '[zana.api])
       (defn ~'read-binary-file 
         ~(str "Read a list of instances of <code>" classname 
               "</code> from the <code>^java.io.File " f 
               "</code>, assuming it was written with [[write-binary-file]]."
               " Creates an object input stream and an archetyper function"
               " to pass to [[read-binary]].")
         ~args
         (let
           [~a (zana.api/archetyper)
            ~b (com.google.common.collect.ImmutableList/builder)]
           (with-open [~i (zana.api/object-input-stream ~f)]
             (loop []
               (when-let
                 [~r (~'read-binary ~i ~a)]
                 (.add ~b ~r)
                 (recur)))
             (.build ~b)))))))
;;------------------------------------------------------------------------------