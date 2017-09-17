(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-08-31"
      :doc "A slide show based on
            http://tympanus.net/codrops/2011/09/20/responsive-image-gallery/" }
    
    zana.html.slides
  
  (:require [clojure.java.io :as io]
            [hiccup.page :as hiccup]))
;;------------------------------------------------------------------------------
(defn- thumb-path [dirname ^java.io.File f]
  (str dirname "/thumbs/" (.getName f)))

(defn- png-file? [^java.io.File f]
  (.endsWith (.getName f) ".png"))

(defn- image-list-item [dirname ^java.io.File f]
  (let [fpath (str dirname "/" (.getName f))
        thumb (thumb-path dirname f)]
    [:li [:a {:href "#"} [:img {:src thumb :data-large fpath}]]]))

(def ^:private css-files ["css/demo.css"
                "css/style.css"
                "css/elastislide.css"])

(def ^:private js-files ["js/jquery-1.7.1.min.js"
               "js/jquery.tmpl.min.js"
               "js/jquery.easing.1.3.js"
               "js/jquery.elastislide.js"
               "js/gallery.js"])

(def ^:private icon-files ["images/ajax-loader.gif"
                 "images/black.png"
                 "images/nav.png"
                 "images/nav_thumbs.png"
                 "images/pattern.png"
                 "images/views.png"])

(defn- header [dirname]
  [:head
   [:title (str dirname " plots")]
   [:meta {:charset "UTF-8"}]
   [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"}]
   (apply hiccup/include-css css-files)
   (apply hiccup/include-js js-files)
   [:script {:id "img-wrapper-tmpl" :type "text/x-jquery-tmpl"}
    [:div {:class "rg-image-wrapper"}
     "{{if itemsCount > 1}}"
     [:div {:class "rg-image-nav"}
      [:a {:href "#" :class "rg-image-nav-prev"}"Previous Image"]
      [:a {:href "#" :class "rg-image-nav-next"}"Next Image"]]
     "{{/if}}"
     [:div {:class "rg-image"}]
     [:div {:class "rg-loading"}]
     [:div {:class "rg-caption-wrapper"}
      [:div {:class "rg-caption" :style "display:none;"}
       [:p]]]]]])

(defn- body [dirname files]
  [:body
   [:div {:class "container"}
    [:div {:class "content"}
     [:div {:id "rg-gallery" :class "rg-gallery"}
      [:div {:class "rg-thumbs"}
       [:div {:class "es-carousel-wrapper"}
        [:div {:class "es-nav"}
         [:span {:class "es-nav-prev"} "Previous"]
         [:span {:class "es-nav-next"} "Next"]]
        [:div {:class "es-carousel"}
         [:ul (map #(image-list-item dirname %) files)]]]]]]]])

(defn- html [dirname files]
  (hiccup/html5 {:encoding "UTF-8" :lang "en"}
                (header dirname)
                (body dirname files)))

(defn- html-file [^java.io.File folder dirname]
  (io/file (.getParentFile folder) (str dirname ".html")))

(defn- copy [^String path ^java.io.File folder]
  (let [url (io/resource path)
        outfile (io/file (.getParentFile folder) path)]
    (io/make-parents outfile)
    (io/copy (io/input-stream url) outfile)))
;;------------------------------------------------------------------------------
;; TODO: replace image-type int with keyword
(defn- image
  (^java.awt.image.BufferedImage [^double w ^double h ^long image-type]
    (java.awt.image.BufferedImage. (int (Math/ceil w))
                                   (int (Math/ceil h))
                                   (int image-type)))
  (^java.awt.image.BufferedImage [^double w ^double h]
    (image w h java.awt.image.BufferedImage/TYPE_INT_ARGB)))
;;------------------------------------------------------------------------------
(defn- read-png ^java.awt.image.BufferedImage [^java.io.File f]
  (assert (.exists f)
          (print-str "Trying to read a png from a non-existing file:"
                     (.getPath f)))
  (try
    (javax.imageio.ImageIO/read f)
    (catch Throwable t
      (println (print-str "Error reading" (.getPath f)))
      (println (class t))
      (println (.getMessage t))
      (.printStackTrace t)
      (throw t))))
;;------------------------------------------------------------------------------
(defn- write-png [^java.awt.image.RenderedImage image 
                  ^java.io.File f]
  (assert image)
  (assert f)
  (io/make-parents f)
  (javax.imageio.ImageIO/write image "png" f))
;;------------------------------------------------------------------------------
(defn- thumbnail
  (^java.awt.image.BufferedImage  [a b]
    (if (instance? java.io.File b)
      (thumbnail a b 256)
      (let [^java.awt.image.BufferedImage img (if (instance? java.io.File a) 
                                                (read-png a) 
                                                a)
            w (.getWidth img)
            h (.getHeight img)
            r (/ (double b) (Math/max w h))
            a (java.awt.geom.AffineTransform.)
            _ (.setToScale a r r)
            ^java.awt.image.BufferedImage thumb (image (* r w) 
                                                       (* r h) 
                                                       (.getType img))
            g (.createGraphics thumb)]
        (.drawRenderedImage g img a)
        (.dispose g)
        thumb)))
  (^java.awt.image.BufferedImage  [img ^java.io.File thumbfile ^long size]
    (assert img)
    (assert thumbfile)
    (let [thumb (thumbnail img size)]
      (write-png thumb thumbfile)
      thumb)))
;;------------------------------------------------------------------------------
(defn show 
  "Write the necessary html, css, js, etc. to create a minimal slide show
   in <code>folder</code> for all the png files in <code>folder</code>."
  [^java.io.File folder]
  (let [dirname (.getName folder)
        thumbs (io/file folder "thumbs")
        files (sort-by #(.getName ^java.io.File %)
                       (filter png-file? (.listFiles folder)))]
    (when-not (empty? files)
      (doseq [^java.io.File f files]
        (let [^java.io.File thumb (io/file thumbs (.getName f))]
          (when-not (.exists thumb) (thumbnail f thumb))))
      (doseq [path css-files] (copy path folder))
      (doseq [path icon-files] (copy path folder))
      (doseq [path js-files] (copy path folder))
      (spit (html-file folder dirname) (html dirname files)))))
;;------------------------------------------------------------------------------