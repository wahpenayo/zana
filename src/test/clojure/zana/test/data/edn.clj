(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-05-20"
      :doc "Tests for zana.data.datum." }
    
    zana.test.data.edn
  
  (:require [clojure.edn :as edn]
            [clojure.test :as test]))
;;------------------------------------------------------------------------------
;;  mvn -Dtest=zana.test.data.edn clojure:test
#_(test/run-tests 'zana.test.data.edn)
;;------------------------------------------------------------------------------
(defrecord Record [^long n ^double x ^String s]
  Object
  (toString [this] (str "#zana.test.data.edn.Record" (into {} this))))
;;------------------------------------------------------------------------------
(test/deftest defrecords
  (let [r0 (Record. 17 Math/PI "forty-two")
        m0 (into {} r0)
        r1 (->Record 17 Math/PI "forty-two")
        r2 (map->Record m0)
        r3 (edn/read-string {:readers {'zana.test.data.edn.Record map->Record}} 
                            (str r0))]
    (test/is (= r0 r1))
    (test/is (= r1 r2))
    (test/is (= r2 r3))))
;;------------------------------------------------------------------------------
;; edge case
;(test/deftest empty-datum
;    (let [e0 (Empty.)
;          s (pr-str e0)]
;      ;; Datums have identity semantics
;      (test/is (empty? empty/fields))
;      (test/is (not= e0 e1))
;      (test/is (not= (.hashCode e0) (.hashCode e1)))))
;;------------------------------------------------------------------------------
;(test/deftest primitive-datum
;    (let [p0 (Primitive. (byte 0) (short 1) (int 2) (long 3) 4 5 \a)
;          p1 (Primitive. 0 1 2 3 4 5 \a)
;          p2 (Primitive. 1 2 3 4 5 6 \b)
;          p3 (assoc p2 :d 0.0)]
;      (test/is (== 6.0 (:d p2)))
;      (test/is (== 0.0 (primitive/d p3)))
;      ;; Datums have identity semantics
;      (test/is (= primitive/fields
;                  [primitive/b primitive/sh primitive/i primitive/l
;                   primitive/f primitive/d primitive/c]))
;      (test/is (not= p0 p1))
;      (test/is (not= p0 p2))
;      (test/is (not= (.hashCode p0) (.hashCode p1)))
;      (test/is (not= (.hashCode p0) (.hashCode p2)))
;      (test/is (== (primitive/b p0) (primitive/b p1)))
;      (test/is (not (== (primitive/b p0) (primitive/b p2))))
;      (test/is (instance? clojure.lang.IFn$OL primitive/b))
;      (test/is (instance? clojure.lang.IFn$OL primitive/sh))
;      (test/is (instance? clojure.lang.IFn$OL primitive/i))
;      (test/is (instance? clojure.lang.IFn$OL primitive/l))
;      (test/is (instance? clojure.lang.IFn$OD primitive/f))
;      (test/is (instance? clojure.lang.IFn$OD primitive/d))
;      ;; could simplify  z/define-datum code if memfn type-hinted the return
;      ;; value
;      ;;(test/is (not (instance? clojure.lang.IFn$OD (memfn ^Primitive d))))
;      ))
;;------------------------------------------------------------------------------
;(test/deftest typical-datum
;    (let [p0 (Typical.
;               17 Math/PI "Galileo"
;               (Primitive. 0 1 2 3 4 5 \a)
;               (LocalDate/parse "2015-02-20")
;               (LocalDateTime/parse "2015-02-20T10:15:30"))
;          p1 (Typical.
;               17 Math/PI "Galileo"
;               (Primitive. 0 1 2 3 4 5 \a)
;               (LocalDate/parse "2015-02-20")
;               (LocalDateTime/parse "2015-02-20T10:15:30"))
;          p2 (Typical.
;               13 Math/E "Darwin"
;               (Primitive. 1 2 3 4 5 6 \b)
;               (LocalDate/parse "1776-07-03")
;               (LocalDateTime/parse "1826-07-04T09:00:00"))]
;      ;; Datums have identity semantics
;      (test/is (not= p0 p1))
;      (test/is (not= p0 p2))
;      (test/is (not= (.hashCode p0) (.hashCode p1)))
;      (test/is (not= (.hashCode p0) (.hashCode p2)))
;      (test/is (not= (typical/p p0) (typical/p p1)))
;      (test/is (not= (typical/p p0) (typical/p p2)))
;      (test/is (== (typical/x p0) (typical/x p1)))
;      (test/is (not (== (typical/n p0) (typical/n p2))))
;      (test/is (instance? clojure.lang.IFn$OL typical/n))
;      (test/is (instance? clojure.lang.IFn$OD typical/x))))
;;------------------------------------------------------------------------------