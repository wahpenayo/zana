(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com"
      :date "2018-02-06"
      :doc "Tests for <code>zana.data</code>." }
    
    zana.test.data.setup
  
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [clojure.test :as test]
            [zana.test.defs.data.empty :as empty]
            [zana.test.defs.data.primitive :as primitive]
            [zana.test.defs.data.typical :as typical]
            [zana.test.defs.data.change :as change])
  (:import [java.time LocalDateTime LocalDate]
           [java.io File]
           [zana.java.data FlatEmbedding]
           [zana.test.defs.data.empty Empty]
           [zana.test.defs.data.primitive Primitive]
           [zana.test.defs.data.typical Typical]
           [zana.test.defs.data.change Change]))
;;----------------------------------------------------------------

(defn empties [] 
  [(Empty.) (Empty.) (empty/map->Empty {})])

;;----------------------------------------------------------------

(defn primitives [] 
  (let [p2 (Primitive. false 1 2 3 4 5 6 \b)]
    [(Primitive. true (byte 0) (short 1) (int 2) (long 3) 4 5 \a)
     (Primitive. true 0 1 2 3 4 5 \a)
     p2
     (assoc p2 :d 0.0)
     (primitive/map->Primitive
       {:tf true :b 0 :sh 1 :i 2 :l 3 :f 4.0 :d 5.0 :c \c})]))

;; datums have identity semantics, so = won't work for testing io roundtrips.
(defn equal-primitives? [p0 p1]
  (and (= (primitive/tf p0) (primitive/tf p1))
       (== (primitive/b p0) (primitive/b p1))
       (== (primitive/sh p0) (primitive/sh p1))
       (== (primitive/i p0) (primitive/i p1))
       (== (primitive/l p0) (primitive/l p1))
       (== (primitive/f p0) (primitive/f p1))
       (== (primitive/d p0) (primitive/d p1))
       (= (primitive/c p0) (primitive/c p1))))
       
;;----------------------------------------------------------------

(defn galileo-birthdate [] (LocalDate/parse "1564-02-15"))

(defn typicals []
  (let [ymd (galileo-birthdate)
        p0 (Primitive. true 0 1 2 3 4 5 \a)
        p1 (Primitive. true 0 1 2 3 4 5.5 \a)
        p2 (Primitive. false 1 2 3 4 5 6 \b)
        t0 (Typical. 17 Math/PI "Galileo" p0 ymd
                     (LocalDateTime/parse "2015-02-20T10:15:30"))]
    [t0
     (Typical.
       17 Math/PI "Galileo" p1
       (galileo-birthdate)
       (LocalDateTime/parse "2015-02-20T10:15:30"))
     (Typical.
       13 Math/E "Darwin" p2             
       (LocalDate/parse "1776-07-03")
       (LocalDateTime/parse "1826-07-04T09:00:00"))
     (assoc t0 :n (inc (int (:n t0))))
     (assoc-in t0 [:p :i] (inc (int (get-in t0 [:p :i]))))]))

(defn equal-typicals? [t0 t1]
  (and (== (typical/n t0) (typical/n t1)) 
       (== (typical/x t0) (typical/x t1)) 
       (= (typical/string t0) (typical/string t1)) 
       (equal-primitives? (typical/p t0) (typical/p t1)) 
       (= (typical/ymd t0) (typical/ymd t1)) 
       (= (typical/dt t0) (typical/dt t1)))) 
  
;;----------------------------------------------------------------

(defn changes []
  (let [t0 (Typical.
             17 Math/PI "Galileo" 
             (Primitive. true 0 1 2 3 4 5 \a)
             (galileo-birthdate)
             (LocalDateTime/parse "1642-01-08T10:15:30"))
        t1 (Typical.
             13 Math/E "Darwin"             
             (Primitive. false 1 2 3 4 5 6 \b)
             (LocalDate/parse "1809-02-12")
             (LocalDateTime/parse "1882-04-19T03:14:15"))
        t2 (Typical.
             23 (* Math/E Math/PI) "Kepler" 
             (Primitive. true 0 2 4 8 16 32 \c)
             (LocalDate/parse "1571-12-27")
             (LocalDateTime/parse "1630-11-15T13:17:23"))
        c01 (Change. t0 t1)
        c00 (assoc c01 :after t0)
        c02 (Change. t0 t2)
        c21 (Change. t2 t1)
        c10 (Change. t1 t0)
        c11 (assoc c01 :before t1)]
    [c00 c01 c10 c11 c02 c21
     (assoc-in c01 [:before :p :i] (get-in c11 [:after :p :i]))
     (assoc-in c00 [:before :p] (get-in c11 [:after :p]))]))

(defn equal-changes? [c0 c1]
  (and (equal-typicals? (change/before c0) (change/before c1))
       (equal-typicals? (change/after c0) (change/after c1))))

;;----------------------------------------------------------------

(defn embed-file ^File [nss embed]
  (let [tokens (s/split nss #"\.")
        folder (apply io/file "tst" tokens)
        fname (str (.getSimpleName (class embed))
                   "-" (.name ^FlatEmbedding embed) ".edn")
        ^File file (io/file folder fname)]
    (io/make-parents file) 
    #_(println (.getPath file))
    file))

;;----------------------------------------------------------------
