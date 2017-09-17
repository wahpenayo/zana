(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-06-30"
      :doc "Tests for zana.data.datum." }
    
    zana.test.defs.data.typical
  
  (:require [zana.api :as z]
            [zana.test.defs.data.primitive :as primitive])
  (:import [java.time LocalDateTime LocalDate]
           [zana.test.defs.data.primitive Primitive]))
;; mvn clean -Dtest=zana.test.defs.data.typical clojure:test > tests.txt
;;------------------------------------------------------------------------------
(defn header-key [^String token]
  (let [k (keyword (.replace (.toLowerCase token) "_" "-"))]
    (case k 
      :ptf [:p :tf] 
      :pb [:p :b] 
      :psh [:p :sh] 
      :pi [:p :i] 
      :pl [:p :l] 
      :pf [:p :f] 
      :pc [:p :c] 
      :pd [:p :d]
      ;; else
      k)))
;;-----------------------------------------------------------------------------
(z/define-datum Typical
  [^long n 
   ^double x 
   ^String string 
   ^Primitive p 
   ^LocalDate [ymd (fn [tuple archetype]
                     (archetype (LocalDate/parse ^String (:ymd tuple))))]
   ^LocalDateTime [dt (fn [tuple archetype]
                        (LocalDateTime/parse ^String (:dt tuple)))]])
;;------------------------------------------------------------------------------
;; macro debugging
#_(with-open [w (clojure.java.io/writer 
                  "src/test/clojure/zana/test/defs/data/typical1.clj")]
  (println w)
  (binding [*out* w
            *print-meta* true]
    (require '[clojure.pprint])
    (println "(set! *warn-on-reflection* true)") 
    (println "(set! *unchecked-math* :warn-on-boxed)")
    (println "(ns zana.test.defs.data.typical1
  (:require [zana.api :as z]
            [zana.test.defs.data.primitive :as primitive])
  (:import [java.time LocalDateTime LocalDate]
           [java.time.format DateTimeFormatter]
           [zana.test.defs.data.primitive Primitive]))")
    (println "(defn header-key [^String token]
  (let [token (keyword (.replace (.toLowerCase token) \"_\" \"-\"))]
    (case token 
      :ptf [:p :tf] :pb [:p :b] :psh [:p :sh] :pi [:p :i] :pl [:p :l] 
      :pf [:p :f] :pc [:p :c] :pd [:p :d]
      ;; else
      token)))")
    (clojure.pprint/pprint
      (macroexpand
        '(z/define-datum Typical
  [^long n 
   ^double x 
   ^String string 
   ^Primitive p 
   ^LocalDate [ymd #(LocalDate/parse ^String (:ymd %))]
   ^LocalDateTime [dt 
                   #(LocalDateTime/parse 
                      ^String (:dt %)
                      DateTimeFormatter/ISO_OFFSET_DATE_TIME)]])))))
;;------------------------------------------------------------------------------

