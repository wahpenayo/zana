(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-11-14"
      :doc "Text input." }
    
    zana.data.textin
  
  (:require [clojure.string :as s]
            [clojure.pprint :as pp]
            [zana.commons.core :as cc]
            [zana.io.gz :as gz]
            [zana.collections.generic :as g]
            [zana.data.reflect :as r]))
;;------------------------------------------------------------------------------
;; Text Input
;;------------------------------------------------------------------------------
;; TODO: output format isn't the same as input format...
;; TODO: put public functions in data.api
;;------------------------------------------------------------------------------
(defn- child-header-key [^clojure.lang.Symbol field]
  (let [c (r/type field)]
    (when (r/datum-class? c) 
      [(keyword
         (clojure.string/replace 
           (clojure.string/lower-case (str field)) "_" "-"))
       (r/qualified-symbol c "default-header-key")])))
;;------------------------------------------------------------------------------
;; Return a definition for a function that converts a sequence of header String 
;; tokens into a sequence of keys. The key for a non-Datum (non-recursive) field
;; is a Keyword. The key for a Datum field is a sequence of Keywords 
;; corresponding to the field names of the nested fields.

(defn emit-default-header-key [fields]
  (let [token (gensym "token")
        arg (with-meta token {:tag 'String})
        standardize (gensym "standardize")
        child-keys (into {} (keep identity (map child-header-key fields)))
        prefix (gensym "prefix")
        suffix (gensym "suffix")
        k (gensym "k")
        child-key (gensym "child-key")]
    `(let []
       (require '[clojure.string])
       ~(if-not (empty? child-keys)
          `(let 
             [;; Assuming field names are lower case and have "-" not "_".
              ~standardize (fn ~'standardize ~(with-meta [arg] {:tag 'String})
                             (.replaceAll
                               (.replaceAll 
                                 ^String (clojure.string/lower-case ~token) 
                                 "[^\\-\\w]+" "")
                               "[\\s_]+" "-"))
              ]
             (defn ~(with-meta 'default-header-key {:no-doc true}) 
               [~arg]
               (let [~token (~standardize ~token)
                     [~prefix ~suffix] (clojure.string/split ~token #"\-" 2)
                     ~k (keyword ~prefix)
                     ~child-key (~child-keys ~k)]
                 (if ~child-key
                   (flatten (cons ~k [(~child-key ~suffix)]))
                   ~k))))
          `(defn ~(with-meta 'default-header-key {:no-doc true}) 
             [~arg]
             (keyword 
               (.replaceAll
                 (.replaceAll 
                   ^String (clojure.string/lower-case ~token) 
                   "[^\\-\\w]+" "")
                 "[\\s_]+" "-")))))))
;;------------------------------------------------------------------------------
;; return a function that takes a sequence of token strings and returns a tuple 
;; tree. A tuple is a hashmap where the keys are keyword versions of the datum's
;; field names. Most values are Strings, When the field type is a descendent of 
;; Datum, the value is a tuple tree for that field's Datum.
;; Note: this is equivalent to zipmap in the simple case.

(defn tuple-tree [^Iterable header ^Iterable tokens]
  #_(assert (== (count header)(count tokens))
            (with-out-str 
              (println (count header) (count tokens))
              (pp/pprint header)
              (println)
              (pp/pprint tokens)))
  (let [ih (.iterator header)
        it (.iterator tokens)]
    (loop [m {}]
      ;; hackkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk
      ;; some files we need to process have an extra \tab at the end of some lines,
      ;; creating an empty extra token.
      ;; This ignores any mismatch between header and data token count, using the
      ;; shorter of the 2 (like zipmap).
      (if-not (and (.hasNext ih) (.hasNext it)) 
        m
        (let [k (.next ih)
              v (.next it)
              m ((if (keyword? k) assoc assoc-in) m k v)]
          (recur m))))))
;;------------------------------------------------------------------------------
;; TODO: eliminate the dependency on zana.data.textin
;; at least move to zana.api

(defn- default-parser [field]
  (let [hint (:tag (meta field))
        c (r/type field)
        k (keyword field)
        value (gensym "value") 
        tuple (gensym "tuple")
        arg (with-meta tuple {:tag 'java.util.Map})
        archetype (gensym "archetype")]
    (if (r/datum-class? c)
      `(fn parse-datum ~(with-meta `[~arg ~archetype] {:tag c})
         (let [^String ~value (~k ~tuple)]
           (~(r/qualified-symbol c "parse-tuple") ~value ~archetype)))
      (case hint
        ;; map empty Strings, "nil", etc, to nil == missing
        ;; TODO: support general codes for missing
        String `(fn parse-string ~(with-meta `[~arg ~archetype] {:tag 'String})
                  (let [~(with-meta value {:tag 'String}) (~k ~tuple)]
                    (when ~value
                      (case (.toLowerCase ~value)
                        ("" "nil" "null") nil 
                        (~archetype ~value)))))
        ;; missing not possible for boolean, char, byte, short, int, and long
        boolean `(fn parse-boolean ~(with-meta `[~arg ~archetype] {:tag 'Boolean})
                   (let [^String ~value (~k ~tuple)]
                     (if ~value (Boolean/parseBoolean ~value) false)))
        char `(fn parse-char ~(with-meta `[~arg ~archetype] {:tag 'Character})
                (let [^String ~value (~k ~tuple)]
                  (when ~value 
                    (assert (== 1 (count ~value)) (pr-str ~value)) 
                    (first ~value))))
        byte `(fn parse-byte ~(with-meta `[~arg ~archetype] {:tag long})
                (let [^String ~value (~k ~tuple)]
                  (long (Byte/parseByte ~value))))
        short `(fn parse-short ~(with-meta `[~arg ~archetype] {:tag long})
                 (let [^String ~value (~k ~tuple)]
                   (long (Short/parseShort ~value))))
        int `(fn parse-int ~(with-meta `[~arg ~archetype] {:tag long})
               (let [^String ~value (~k ~tuple)]
                 (long (Integer/parseInt ~value))))
        long `(fn parse-long ~(with-meta `[~arg ~archetype] {:tag long})
                (let [^String ~value (~k ~tuple)]
                  (Long/parseLong ~value)))
        ;; missing == NaN, map "NaN" and empty strings to NaN
        float `(fn parse-float ~(with-meta `[~arg ~archetype] {:tag double})
                 (let [^String ~value (~k ~tuple)]
                   (double (if-not (empty? ~value) 
                             (Float/parseFloat ~value) 
                             Float/NaN))))
        double `(fn parse-double ~(with-meta `[~arg ~archetype] {:tag double})
                  (let [^String ~value (~k ~tuple)]
                    (if-not (empty? ~value) 
                      (Double/parseDouble ~value) 
                      Double/NaN)))
        java.time.LocalDate `(fn parse-local-date 
                               ~(with-meta `[~arg ~archetype] {:tag java.time.LocalDate})
                               (let [^String ~value (~k ~tuple)]
                                 (when-not (empty? ~value)
                                   (~archetype 
                                     (java.time.LocalDate/parse ~value)))))
        java.time.LocalDateTime `(fn parse-local-date-time
                                   ~(with-meta `[~arg ~archetype] 
                                      {:tag java.time.LocalDateTime})
                                   (let [^String ~value (~k ~tuple)]
                                     (when-not (empty? ~value)
                                       ;; don't archetype since too many values
                                       (java.time.LocalDateTime/parse ~value))))
        ;; else
        nil))))
;;------------------------------------------------------------------------------
(defn- parser [field-spec]
  (if (sequential? field-spec)
    (do
      (assert (= 2 (count field-spec)))
      field-spec)
    [field-spec (default-parser field-spec)]))
;;------------------------------------------------------------------------------
(defn- field-to-parser [field-specs] (into {} (map parser field-specs)))
;;------------------------------------------------------------------------------
;; assume the tuple is a map from (keyword field) to String
;; TODO: column header to field mapping

(defn tuple-parser [classname field-specs]
  (let [fields (mapv r/extract-field field-specs)
        field-parser (field-to-parser field-specs)
        throwable (gensym "throwable")
        tuple (gensym "tuple")
        archetype (gensym "archetype")]
    `(defn ~'parse-tuple 
       ~(print-str 
          "Constructs an instance of" 
          classname
          "from a tuple-tree, a nested map whose keys are keyword versions"
          "of the fields, where the nesting following the nesting of"
          "of datum-valued fields,")
       ~(with-meta `[~(with-meta tuple 
                        {:tag 'java.util.Map})
                     ~archetype]
          {:tag (r/munge classname)})
       (try
         (assert ~tuple "no tuple to parse.")
         (assert ~archetype "no de-duping function.")
         (~(r/constructor classname)
           ~@(mapv (fn generate-field-parser [field]
                     (let [prsr (field-parser field)]
                       (if prsr
                         `(~prsr ~tuple ~archetype)
                         `(~archetype (~(keyword field) ~tuple)))))
                   fields))
         (catch Throwable ~throwable
           (binding [*out* *err*]
             (println ~tuple)
             (throw ~throwable)))))))
;;------------------------------------------------------------------------------
;; only thing here specific to the particular data is the reference to
;; parse-tuple
;; Should this be a global function rather than a datum specific one emitted
;; by macro expansion?

(defn tsv-file-reader [classname field-specs]
  (let [;; TODO: generalize File arg
        f (gensym "f") 
        sep (gensym "sep")  
        n (gensym "n") 
        hk (gensym "hk") 
        r (gensym "r")
        lines (gensym "lines")
        splitter (with-meta (gensym "splitter")
                   {:tag 'com.google.common.base.Splitter})
        header (gensym "header")
        parse (gensym "parse")
        line (gensym "line")
        tokens (gensym "tokens")
        parse-tuple (gensym "parse-tuple")
        tuple (gensym "tuple")
        archetype (gensym "archetype")
        fields (mapv r/extract-field field-specs)]
    `((require '[clojure.string] '[zana.api])
       ~(emit-default-header-key fields)
       ~(tuple-parser classname field-specs)
       (defn ~(with-meta 'read-tsv-file {:deprecated "2016-09-06"}) 
         ~(str "Read instances of <code>" classname 
               "</code> from the file <code>" f 
               "</code>, assuming the field values separated by <code>" sep
               "</code> (which defaults to <code>\"\\t\"</code>.<br>"
               "This implements a complicated and restrictive strategy for "
               "dealing with nested datum classes, and should be replaced by "
               "something simpler and more flexible.")
         (~(with-meta `[~f ;; File, URL, etc.
                        ~(with-meta sep {:tag 'java.util.regex.Pattern})
                        ~(with-meta hk {:tag 'clojure.lang.IFn})
                        ~(with-meta n {:tag Long/TYPE})] 
             {:tag java.util.Collection})
           (with-open [~r (zana.api/reader ~f)]
             (let [~archetype (zana.api/archetyper)
                   ~lines (line-seq ~r)
;                   ~header (mapv ~hk (clojure.string/split (first ~lines) ~sep -1))
;                   ~parse (fn ~'parse-line [~line]
;                            (let [~tokens (clojure.string/split ~line ~sep -1)
;                                  ~tuple (tuple-tree ~header ~tokens)]
;                              (try
;                                (~'parse-tuple ~tuple ~archetype)
;                                (catch Throwable t#
;                                  (println "failed to parse:")
;                                  (pp/pprint ~tuple)
;                                  (.printStackTrace t#)
;                                  (throw t#)))))
                   ~splitter (com.google.common.base.Splitter/on ~sep)
                   ~header (mapv ~hk (.split ~splitter (first ~lines)))
                   ~parse (fn ~'parse-line [~line]
                            (let [~tokens (.split ~splitter ~line)
                                  ~tuple (tuple-tree ~header ~tokens)]
                              (try
                                (~'parse-tuple ~tuple ~archetype)
                                (catch Throwable t#
                                  (println "failed to parse:")
                                  (pp/pprint ~tuple)
                                  (.printStackTrace t#)
                                  (throw t#)))))]
               ;; TODO: a chunked concurrent version of this
               (zana.api/map ~parse (take ~n (rest ~lines))))))
         (~(with-meta `[~f ~sep ~hk] {:tag 'java.util.Collection})
           (~'read-tsv-file ~f ~sep ~hk Long/MAX_VALUE))
         (~(with-meta `[~f ~sep] {:tag 'java.util.Collection})
           (~'read-tsv-file ~f ~sep ~'default-header-key))
         (~(with-meta `[~f] {:tag 'java.util.Collection})
           (~'read-tsv-file ~f #"\t"))))))
;;------------------------------------------------------------------------------
