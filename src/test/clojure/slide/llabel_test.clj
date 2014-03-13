;; -*- coding: utf-8-unix -*-
(ns slide.llabel-test
  (:require [clojure.test :refer :all]
            [seesaw.core :as sc]
            [seesaw.border :as sb]
            [seesaw.icon :as si]
            [seesaw.mig :as sm]
            [slide.core :as slc])
  (:import [com.github.sgr.slide DefaultLinkHandler LinkHandler LinkHandlers LinkLabel]
           [java.net URI]
           [javax.swing JPanel]))

(def url "http://www.aozora.gr.jp/cards/000042/files/2446_10267.html")
(def txt "蓄音機 寺田寅彦")

(defn- frame [title content]
  (sc/frame
   :title title
   :content content
   :size [640 :by 480]
   :on-close :dispose))

(defn- wait-closing [frame]
  (let [p (promise)]
    (sc/listen frame :window-closing (fn [_] (deliver p true)))
    (-> frame slc/move-to-center! sc/show!)
    @p))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest ^{:llabel true :gui true} llabel-text-test
  (let [l (doto (LinkLabel.)
            (.setBorder (sb/line-border :color :red :thickness 1))
            (.setText txt))
        p (sm/mig-panel
           :constraints ["wrap 2, ins 5 10 5 10"]
           :items [["llabel (text):"] [l]])
        frm (frame "test link label" p)]
    (wait-closing frm)))

(deftest ^{:llabel true :gui true} llabel-text-link-test
  (let [l (doto (LinkLabel.)
            (.setText txt)
            (.setBorder (sb/line-border :color :red :thickness 1))
            (.setURI (URI. url)))
        p (sm/mig-panel
           :constraints ["wrap 2, ins 5 10 5 10"]
           :items [["llabel (text with link):"] [l]])
        frm (frame "test link label" p)]
    (wait-closing frm)))

(deftest ^{:llabel true :gui true} llabel-icon-test
  (let [l (doto (LinkLabel.)
            (.setBorder (sb/line-border :color :red :thickness 1))
            (.setIcon (si/icon "slide_color_mini.png")))
        p (sm/mig-panel
           :constraints ["wrap 2, ins 5 10 5 10"]
           :items [["llabel (icon):"] [l]])
        frm (frame "test link label" p)]
    (wait-closing frm)))

(deftest ^{:llabel true :gui true} llabel-icon-link-test
  (let [ls [(DefaultLinkHandler.)
            (proxy [LinkHandler] ["Safari"]
              (browse [uri]
                (.start (ProcessBuilder.
                         ["open" "-a" "/Applications/Safari.app"
                          (.. uri toURL toString)]))))]
        lhdrs (proxy [LinkHandlers] []
                (getHandlerCount [] (count ls))
                (getHandler [idx] (get ls idx)))
        l (doto (LinkLabel.)
            (.setBorder (sb/line-border :color :red :thickness 1))
            (.setIcon (si/icon "slide_color_mini.png"))
            (.setLinkHandlers lhdrs)
            (.setURI (URI. url)))
        p (sm/mig-panel
           :constraints ["wrap 2, ins 5 10 5 10"]
           :items [["llabel (icon with link):"] [l]])
        frm (frame "test link label" p)]
    (wait-closing frm)))
