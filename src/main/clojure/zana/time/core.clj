(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-09-21"
      :doc "standard date formats, etc." }
    
    zana.time.core
  
  (:require [zana.stats.statistics :as stat]
            [clojure.string :as s])  
  
  (:import [java.time Instant LocalDate LocalDateTime ZoneId ZoneOffset]
           [java.time.temporal ChronoUnit]
           [java.time.format DateTimeFormatter]))
;;------------------------------------------------------------------------------
;; TODO: make functions work for more than just LocaDate, LocalDateTime
;;------------------------------------------------------------------------------
(defmethod clojure.core/print-method 
  LocalDate 
  [^LocalDate x ^java.io.Writer w]
  (.write w (.toString x)))
(defmethod clojure.core/print-method 
  LocalDateTime 
  [^LocalDateTime x ^java.io.Writer w]
  (.write w (.toString x)))
;;------------------------------------------------------------------------------
(defn before? 
  "Is <code>d0</code> strictly before <code>d1</code>?<br>
   Only works for <code>java.time.LocalDate</code> at present.
   **TODO:** replace with more general approach using 
   <code>Comparable</code>/<code>Comparator</code>."
  [^LocalDate d0 ^LocalDate d1] 
  (.isBefore d0 d1))

(defn after? 
  "Is <code>d0</code> strictly after <code>d1</code>?<br>
   Only works for <code>java.time.LocalDate</code> at present.
   **TODO:** replace with more general approach using 
   <code>Comparable</code>/<code>Comparator</code>."
  [^LocalDate d0 ^LocalDate d1] 
  (.isAfter d0 d1))

(defn between? 
  "Is <code>date</code> in the half-open interval
   <code>[inclusive-start,exclusive-end)</code>?<br>
   Only works for <code>java.time.LocalDate</code> at present.
   **TODO:** replace with more general approach using 
   <code>Comparable</code>/<code>Comparator</code>."
  
  [^LocalDate inclusive-start 
   ^LocalDate exclusive-end 
   ^LocalDate date]
  (and (not (before? date inclusive-start))
       (before? date exclusive-end)))
;;------------------------------------------------------------------------------
(defn minus-days 
  "Subtract <code>^long days</code> from <code>^LocalDate d</code>."
  [^LocalDate d ^long days] (.minusDays d (int days)))
(defn plus-days 
  "Add <code>^long days</code> to <code>^LocalDate d</code>."
  [^LocalDate d ^long days] (.plusDays d (int days)))
;;------------------------------------------------------------------------------
(def ^:private ymdhms (DateTimeFormatter/ofPattern "yyyy-MM-dd-HH-mm-ss"))
(defn now 
  "A string, in format \"yyyy-MM-dd-HH-mm-ss\"."
  ^String [] (.format (LocalDateTime/now) ymdhms))

(defn today 
  "A string, in format \"yyyy-MM-dd\"."
  ^String [] (str (LocalDate/now)))

(defn basic-iso 
  "A string, in format \"yyyyMMdd\"."
  ^String [^java.time.temporal.TemporalAccessor date]
  (.format DateTimeFormatter/BASIC_ISO_DATE date))

(defn parse-basic-iso 
  "A local date, from format \"yyyyMMdd\"."
  ^java.time.LocalDate [^String s]
  (LocalDate/parse s DateTimeFormatter/BASIC_ISO_DATE))

(defn iso-ymd 
  "A string, in format \"yyyy-MM-dd\"."
  ^String [^java.time.temporal.TemporalAccessor date]
  (.format DateTimeFormatter/ISO_DATE date))

(defn parse-iso-ymd
  "A local date, from format \"yyyy-MM-dd\"."
  ^java.time.LocalDate [^String s]
  (LocalDate/parse s DateTimeFormatter/ISO_DATE))
;;------------------------------------------------------------------------------
;; treating local date as a utc start of day

(defn localdate-to-instant 
  "The instant at start of day in UTC." 
  ^java.time.Instant [^java.time.LocalDate ld]
  (Instant/from (.atStartOfDay ld (ZoneId/of "UTC"))))

(defn localdate-to-seconds  
  "Epoch seconds at start of day in UTC." 
  ^long [^java.time.LocalDate ld]
  (.getEpochSecond (localdate-to-instant ld)))

(defn localdate-to-milliseconds  
  "Epoch milliseconds at start of day in UTC." 
  ^long [^java.time.LocalDate ld]
  (.toEpochMilli (localdate-to-instant ld)))

(defn instant-to-localdate 
  "The date (in UTC) containing the instant." 
  ^java.time.LocalDate [^java.time.Instant i]
  (.toLocalDate (.atOffset i ZoneOffset/UTC)))

(defn seconds-to-localdate 
  "The date (in UTC) containing the epoch seconds." 
  ^java.time.LocalDate [^long s]
  (instant-to-localdate (Instant/ofEpochSecond s)))

(defn milliseconds-to-localdate 
  "The date (in UTC) containing the epoch milliseconds." 
  ^java.time.LocalDate [^long s]
  (instant-to-localdate (Instant/ofEpochMilli s)))

;;------------------------------------------------------------------------------

(defn localdatetime-to-instant 
  "The instant corresponding to the date-time (in UTC)."
  ^java.time.Instant [^java.time.LocalDateTime ldt]
  (.toInstant ldt ZoneOffset/UTC))

(defn instant-to-localdatetime 
  "The date-time (in UTC) corresponding to the instant."
  ^java.time.LocalDateTime [^java.time.Instant i]
  (.toLocalDateTime (.atOffset i ZoneOffset/UTC)))

(defn localdatetime-to-seconds 
  "Epoch seconds corresponding to the date-time (in UTC)."
  ^long [^java.time.LocalDateTime ldt]
  (.getEpochSecond (localdatetime-to-instant ldt)))

(defn seconds-to-localdatetime 
  "The date-time (in UTC) corresponding to the epoch seconds."
  ^java.time.LocalDateTime [^long s]
  (instant-to-localdatetime (Instant/ofEpochSecond s)))

(defn localdatetime-to-milliseconds 
  "Epoch milliseconds corresponding to the date-time (in UTC)."
  ^long [^java.time.LocalDateTime ldt]
  (.toEpochMilli (localdatetime-to-instant ldt)))

(defn milliseconds-to-localdatetime 
  "The date-time (in UTC) corresponding to the epoch milliseconds."
  ^java.time.LocalDateTime [^long s]
  (instant-to-localdatetime (Instant/ofEpochMilli s)))

;;------------------------------------------------------------------------------
;; Date ranges
;;
;; TODO: Handle more start, end implementations.
;; TODO: create Interval interface like JodaTime? 
;; why doesn't java.time have an Interval?
;;------------------------------------------------------------------------------

(defn date-range? 
  
  "Is this a date range?<br>
   <em>Note:</em> Representation of date ranges is very likely to change. 
   Currently, it's just a length 2 vector holding 
   <a href=\"https://docs.oracle.com/javase/8/docs/api/index.html?java/time/LocalDate.html\">
   LocalDates</a> where the first date is not after the second."

  [x]
  (and (instance? java.util.List x)
       (== 2 (.size ^java.util.List x))
       (every? #(instance? java.time.LocalDate %) x)
       (not (after? (first x) (second x)))))

(defn date-range 

  "Compute the range of the epoch seconds returned by 
   <code>count-epoch-seconds</code> over <code>data</code>, 
   and convert to a date range.<br>
   <em>Note:</em> this function is too specialized to survive for long.<br>
   <em>Note:</em> Representation of date ranges is very likely to change. 
   Currently, it's just a length 2 vector holding 
   <a href=\"https://docs.oracle.com/javase/8/docs/api/index.html?java/time/LocalDate.html\">
   LocalDates</a> where the first date is not after the second."

  [^clojure.lang.IFn$OL count-epoch-seconds data]
  
  (let [[^long epoch-seconds0 ^long epoch-seconds1] 
        (stat/minmax count-epoch-seconds data)
        ^LocalDate d0 (seconds-to-localdate epoch-seconds0)
        ^LocalDate d1 (seconds-to-localdate epoch-seconds1)]
    [d0 (.plusDays d1 (long 1))]))

;; Returns a short string representation.
(defn date-range-string 
  "Return a standard date range string: yyyyMMdd-yyyyMMdd.<br>
   <em>Note:</em> this function is too specialized to survive for long."
  ^String [^LocalDate d0 ^LocalDate d1] 
  (str (basic-iso d0) "-" (basic-iso d1)))

(defn string-to-date-range 
  "Parse a standard date range string: yyyyMMdd-yyyyMM-dd.<br>
   <em>Note:</em> this function is too specialized to survive for long."
  [^String dates]
  (assert (== (.length dates) 17))
  (let [[d0 d1] (s/split dates #"-")]
    [(parse-basic-iso d0) (parse-basic-iso d1)]))

(defn ^:no-doc days-between 
  ^long [^LocalDate d0 ^LocalDate d1] 
  (.between ChronoUnit/DAYS d0 d1))
;;------------------------------------------------------------------------------
(defn- prior-sunday ^java.time.LocalDate [^LocalDate ymd]
  ;; Sunday as 0 day-of-week, not 7 as in ISO-8601
  (let [dow (int (rem (.getValue (.getDayOfWeek ymd)) 7))]
    (.minusDays ymd dow)))

(defn week-of 
  "Return the Sunday (inclusive) to Sunday (exclusive) date range containing 
   <code>^LocalDate ymd</code>."
  [^LocalDate ymd]
  (let [start (prior-sunday ymd)]
    [start (.plusDays start 7)]))
;;------------------------------------------------------------------------------   
