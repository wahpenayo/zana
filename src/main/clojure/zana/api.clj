(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2018-04-02"
      :doc "Primary external interface to Zana, providing a subset
            of the functions found in other <code>zana.xxx</code> 
            namespaces, created using 
            [Potemkin](https://github.com/ztellman/potemkin)." }
    
    zana.api
  
  (:refer-clojure 
    :exclude [any? assoc compare concat contains? count cube? 
              distinct doall double-array drop empty? every? 
              filter first frequencies get group-by index 
              intersection keys last list map map? map-indexed 
              mapcat max merge min name next nth partition pmap 
              range remove repeatedly second shuffle some 
              sort sort-by sorted-map split-at split-with take 
              with-meta union vals vector? zipmap])
  
  (:require [potemkin.namespaces :as pn]
            [zana.commons.core :as cc]
            [zana.io.cheshire :as zheshire]
            [zana.io.edn :as edn]
            [zana.io.paths :as paths]
            [zana.io.gz :as gz]
            [zana.io.tsv :as tsv]
            [zana.collections.clojurize :as clojurize] 
            [zana.collections.generic :as generic]
            [zana.collections.guava :as guava]
            [zana.collections.hppc :as hppc]
            [zana.collections.maps :as maps]
            [zana.collections.sets :as sets]
            [zana.collections.cube :as cube]
            [zana.collections.io :as cio]
            [zana.collections.table :as table]
            [zana.geometry.r1 :as r1]
            [zana.geometry.z1 :as z1]
            [zana.geometry.compose :as compose]
            [zana.geometry.functions :as gf]
            [zana.geometry.generic :as gg]
            [zana.functions.generic :as fgeneric]
            [zana.functions.inverse :as inverse]
            [zana.functions.wrappers :as wrap]
            [zana.data.datum :as datum]
            [zana.data.enum :as enum]
            [zana.data.missing :as missing]
            [zana.data.flatten :as flatten]
            [zana.html.slides :as slides]
            [zana.optimization.math3.cg :as math3cg]
            [zana.optimization.math3.lp :as math3lp]
            [zana.stats.prng :as prng]
            [zana.stats.accumulators :as accumulators]
            [zana.stats.ranks :as ranks]
            [zana.stats.statistics :as stats]
            [zana.prob.empirical :as empirical]
            [zana.prob.seed :as seed]
            [zana.prob.prng :as probprng]
            [zana.prob.measure :as measure]
            [zana.time.core :as time]))
;;----------------------------------------------------------------
;; commons
;;----------------------------------------------------------------
(pn/import-vars cc/letters
                cc/letters-and-digits
                cc/binding-var-root
                cc/byte-array?
                cc/char-array?
                cc/descendant?
                cc/digits
                cc/double-array?
                cc/echo
                cc/float-array?
                cc/gen-keyword
                cc/int-array?
                cc/jvm-args
                cc/long-array?
                cc/make-archetyper
                cc/make-labeler
                cc/name
                cc/name-keyword
                cc/object-array?
                cc/ordered?
                cc/pprint-map-str
                cc/pprint-str 
                cc/ref?
                cc/safe
                cc/seconds
                cc/short-array?
                cc/skewer)
;;----------------------------------------------------------------
;; io
;;----------------------------------------------------------------
(pn/import-vars paths/delete-files
                paths/directory?
                paths/file?
                paths/file-exists?
                paths/filename
                paths/ls
                paths/mkdir
                paths/mkdirs
                paths/parent-file
                paths/pathname)
(pn/import-vars gz/input-stream
                gz/object-input-stream
                gz/object-output-stream
                gz/output-stream
                gz/print-writer
                gz/reader
                gz/writer)
(pn/import-vars edn/edn-readers
                edn/add-edn-readers!
                edn/read-edn
                edn/write-edn)
(pn/import-vars tsv/write-tsv-file)
;;----------------------------------------------------------------
;; collections
;;----------------------------------------------------------------
(pn/import-vars generic/add!
                generic/add-all!
                guava/any?
                clojurize/clojurize
                generic/concat
                generic/count
                guava/doall
                guava/drop
                generic/empty?
                generic/every?
                generic/filter
                generic/first
                generic/get
                generic/has-next?
                generic/iterator
                guava/keep-map
                generic/last
                guava/lexicographical-compare
                generic/map
                #_generic/map-indexed
                generic/map-to-doubles
                generic/map-to-objects
                generic/mapc
                #_generic/mapc-indexed
                generic/mapcat
                generic/next-item
                generic/nmap-doubles
                #_generic/nmap-indexed
                generic/nmap
                generic/nmapc
                generic/nmapcat
                guava/not-nil
                generic/partition
                generic/pmap
                generic/pmap-doubles
                #_generic/pmap-indexed
                generic/pmapc
                generic/pmapcat
                guava/repeatedly
                generic/second
                generic/shuffle
                guava/some
                guava/sort
                guava/sort-by
                guava/split-at
                #_guava/split-by
                generic/!split-with
                generic/split-with
                guava/take)
;;----------------------------------------------------------------
(pn/import-vars maps/assoc
                maps/map?
                maps/merge
                maps/entry-pairs
                maps/entry-triples 
                maps/frequencies
                maps/frequencies-sorted-by-key
                maps/group-by
                maps/group-by-not-nil
                ;; TODO: dispatch to this from group-by-not-nil, don't expose
                maps/group-enums-by-not-nil-random-access
                maps/has-key?
                maps/index
                #_maps/indexf
                #_maps/invert
                maps/keys
                maps/sorted-map
                maps/vals
                maps/zipmap)
;;----------------------------------------------------------------
(pn/import-vars sets/contains?
                sets/count-distinct
                sets/distinct
                sets/distinct-identity
                sets/intersection
                sets/intersects?
                sets/union)
;;----------------------------------------------------------------
(pn/import-vars hppc/add-values
                hppc/double-array
                hppc/entries
                #_hppc/frequencies
                hppc/inner-product ;; TODO: replace with generic function in geometry package
                hppc/object-float-map
                hppc/object-float-map?
                hppc/object-double-map
                hppc/object-double-map?
                hppc/object-long-map
                hppc/object-long-map?)
;;----------------------------------------------------------------
(pn/import-fn cube/attributes cube-attributes)
(pn/import-fn cube/has-attribute? cube-has-attribute?)
(pn/import-fn cube/range cube-range)
#_(pn/import-fn cube/project project-cube)
(pn/import-fn cube/slice slice-cube)
(pn/import-vars cube/cube cube/cube?)
;;----------------------------------------------------------------
(pn/import-vars cio/split-lines)
;;----------------------------------------------------------------
(pn/import-fn table/get table-get)
#_(pn/import-fn table/map table-map)
(pn/import-fn table/nrows table-nrows)
(pn/import-fn table/ncols table-ncols)
(pn/import-fn table/row table-row)
(pn/import-fn table/col table-col)
(pn/import-fn table/values table-values)
(pn/import-vars table/table?
                table/cell-col
                table/cell-row
                table/cell-value
                table/col-keys
                table/row-keys
                table/tabulate)
;;----------------------------------------------------------------
;; functions
;;----------------------------------------------------------------
(pn/import-vars wrap/array-lookup
                wrap/array-lookup?
                wrap/dataset-id
                wrap/double-lookup
                wrap/long-lookup
                wrap/lookup-function
                wrap/map-lookup
                wrap/map-lookup?
                wrap/odm-lookup
                wrap/olm-lookup
                wrap/with-meta)
(pn/import-vars inverse/inverse)
(pn/import-vars fgeneric/cuts 
                fgeneric/declared-value
                fgeneric/domain
                fgeneric/enum-valued?
                fgeneric/codomain
                fgeneric/support
                fgeneric/range
                fgeneric/restrict
                fgeneric/numerical?
                fgeneric/ordinal?
                fgeneric/interval?
                fgeneric/continuous-domain?
                fgeneric/integral-domain?
                fgeneric/ordinal-domain?
                fgeneric/vector?)
;;----------------------------------------------------------------
;; data
;;----------------------------------------------------------------
(pn/import-vars datum/archetyper)
(pn/import-macro datum/define define-datum)
(pn/import-macro enum/ordered define-ordered-enum)
(pn/import-macro enum/unordered define-unordered-enum)
(pn/import-vars enum/enum?)
(pn/import-vars missing/missing?
                missing/count-distinct-finite-doubles
                missing/count-distinct-longs
                missing/count-distinct-not-missing
                missing/distinct-finite-doubles
                missing/distinct-longs
                missing/distinct-not-missing
                missing/count-not-missing
                missing/drop-missing
                missing/finite?
                #_missing/select-finite
                missing/select-finite-values
                flatten/attribute-bindings
                flatten/embedding-dimension
                flatten/linear-embedding
                flatten/affine-embedding)
;;----------------------------------------------------------------
;; prng
;;----------------------------------------------------------------
(pn/import-vars prng/bernoulli-generator
                prng/continuous-uniform-generator
                prng/gaussian-generator
                prng/mersenne-twister-generator
                prng/mersenne-twister-seed
                prng/random-element-generator
                prng/reset-mersenne-twister-seeds
                prng/sample
                prng/sample-with-replacement)
;;----------------------------------------------------------------
;; geometry
;;----------------------------------------------------------------
(pn/import-fn z1/interval integer-interval)
(pn/import-fn r1/interval real-interval)
(pn/import-fn z1/interval? integer-interval?)
(pn/import-fn r1/interval? real-interval?)
(pn/import-vars r1/centered-interval
                compose/compose
                r1/cspan
                gf/affine-functional
                gf/affine-dual
                gf/dual
                gf/generate-affine-functional
                gf/generate-linear-functional
                gf/l2distance2-from
                gf/linear-part
                gf/linear-functional
                gf/sampler
                gf/translation
                gg/interval-contains?
                gg/interval-max
                gg/interval-min
                gg/interval-length)
;;----------------------------------------------------------------
;; stats
;;----------------------------------------------------------------
(pn/import-vars stats/float-approximately==
                stats/approximately<=
                stats/approximately==
                stats/approximately>=
                stats/approximatelyEqual
                stats/bounding-box
                stats/bounds
                stats/constantly-0d
                stats/constantly-1d
                stats/l1-norm
                stats/l1-distance
                stats/mean-absolute-difference
                stats/l2-norm
                stats/l2-distance
                stats/max
                stats/min
                stats/minmax
                stats/rms-difference
                stats/numerical?
                stats/quantiles
                stats/singular?
                stats/sum
                stats/mean)
;;----------------------------------------------------------------
;; prob
;;----------------------------------------------------------------
#_(pn/import-vars empirical/quantile
                  empirical/cdf)
(pn/import-vars seed/seed)
(pn/import-vars probprng/well44497b
                probprng/double-generator)
(pn/import-vars measure/cdf
                measure/gaussian-distribution
                measure/make-wecdf
                measure/make-wepdf
                measure/pointmass
                measure/quantile
                measure/uniform-distribution
                measure/wecdf-to-wepdf
                measure/wepdf-to-wecdf)
;;----------------------------------------------------------------
;; ranks
;;----------------------------------------------------------------
(pn/import-vars ranks/franks 
                ranks/ranks)
;;----------------------------------------------------------------
;; accumulators
;;----------------------------------------------------------------
(pn/import-fn accumulators/mean mean-accumulator)
(pn/import-fn accumulators/mssn mssn-accumulator)

(pn/import-fn accumulators/vector-mean vector-mean-accumulator)
(pn/import-fn accumulators/vector-mssn vector-mssn-accumulator)

(pn/import-fn accumulators/majority-vote 
              majority-vote-accumulator)
(pn/import-fn accumulators/minimum-expected-cost-class 
              minimum-expected-cost-class-accumulator)
(pn/import-fn accumulators/gini
              gini-accumulator)
(pn/import-fn accumulators/positive-fraction 
              positive-fraction-accumulator)

(pn/import-fn accumulators/weighted-mean 
              weighted-mean-accumulator)
(pn/import-fn accumulators/weighted-mssn 
              weighted-mssn-accumulator)
(pn/import-fn accumulators/weighted-majority-vote 
              weighted-majority-vote-accumulator)
(pn/import-fn accumulators/weighted-minimum-expected-cost-class 
              weighted-minimum-expected-cost-class-accumulator)
(pn/import-fn accumulators/weighted-gini 
              weighted-gini-accumulator)
(pn/import-fn accumulators/weighted-positive-fraction 
              weighted-positive-fraction-accumulator)

(pn/import-fn accumulators/make-calculator make-calculator)
(pn/import-fn accumulators/make-object-calculator make-object-calculator)
;;----------------------------------------------------------------
;; optimization
;;----------------------------------------------------------------
(pn/import-fn math3lp/optimize optimize-lp)
(pn/import-fn math3cg/optimize optimize-cg)
;;----------------------------------------------------------------
;; slide show
;;----------------------------------------------------------------
(pn/import-fn slides/show slide-show)
;;----------------------------------------------------------------
;; time
;;----------------------------------------------------------------
(pn/import-vars time/after?
                time/basic-iso
                time/before?
                time/between?
                time/date-range
                time/date-range?
                time/date-range-string
                time/days-between
                time/instant-to-localdate
                time/instant-to-localdatetime
                time/iso-ymd
                time/localdate-to-instant
                time/localdate-to-seconds
                time/localdate-to-milliseconds
                time/localdatetime-to-instant
                time/localdatetime-to-milliseconds
                time/localdatetime-to-seconds
                time/milliseconds-to-localdate
                time/milliseconds-to-localdatetime
                time/minus-days
                time/now
                time/parse-basic-iso
                time/parse-iso-ymd
                time/plus-days
                time/prior-sunday
                time/seconds-to-localdate
                time/seconds-to-localdatetime
                time/string-to-date-range
                time/today
                time/week-of)
;;----------------------------------------------------------------
