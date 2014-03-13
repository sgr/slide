;; -*- coding: utf-8-unix -*-
(ns slide.mlabel-test
  (:require [clojure.test :refer :all]
            [seesaw.core :as sc]
            [seesaw.border :as sb]
            [seesaw.color :as color]
            [seesaw.mig :as sm]
            [slide.core :as slc])
  (:import [com.github.sgr.slide DefaultLinkHandler Link LinkHandler LinkHandlers MultiLineLabel]
           [java.net URI]
           [javax.swing JPanel]))

(def url "http://www.aozora.gr.jp/cards/000042/files/2446_10267.html")

(def txt "　エジソンの蓄音機の発明が登録されたのは一八七七年でちょうど西南戦争せいなんせんそうの年であった。太平洋を隔てて起こったこの二つの出来事にはなんの関係もないようなものの、わが国の文化発達の歴史を西洋のと引き合わせてみる時の一つの目標にはなる。のみならず少なくとも私にはこの偶然の合致が何事かを暗示する象徴のようにも思われる。
　エジソンの最初の蓄音機は、音のために生じた膜の振動を、円筒の上にらせん形に刻んだみぞに張り渡した錫箔すずはくの上に印するもので、今から見ればきわめて不完全なものであった。ある母音や子音は明瞭めいりょうに出ても、たとえばＳの音などはどうしても再現ができなかったそうである。その後にサムナー・テーンターやグラハム・ベルらの研究によって錫箔すずはくの代わりに蝋管ろうかんを使うようになり、さらにベルリナーの発明などがあって今日のグラモフォーンすなわち平円盤蓄音機ができ、今ではこれが世界のすみずみまで行き渡っている。もしだれか極端に蓄音機のきらいな人があってこの器械の音の聞こえない国を捜して歩くとしたら、その人はきっとにがにがしい幻滅を幾度となく繰り返したあげくにすごすご故郷に帰って来るだろうと思われる。
　蓄音機の改良進歩の歴史もおもしろくない事はないが、私にとっては私自身と蓄音機との交渉の歴史のほうがより多く痛切で忘れ難いものである。
　西南戦争に出征していた父が戦乱平定ののち家に帰ったその年の暮れに私が生まれた。その私が中学校の三年生か四年生の時であったからともかくも蓄音機が発明されてから十六七年後の話である。ある日の朝Ｋ市の中学校の掲示場の前におおぜいの生徒が集まって掲示板に現われた意外な告知を読んで若い小さな好奇心を動揺させていた。今度文学士何某という人が蓄音機を携えて来県し、きょう午後講堂でその実験と説明をするから生徒一同集合せよというのであった。これはたしかに単調で重苦しい学校の空気をかき乱して、どこかのすきまから新鮮な風が不時に吹き込んで来たようなものであった。生徒の喜んだことはいうまでもない。おもしろいものが見られ聞かれてその上に午後の課業が休みになるのだから、文学士と蓄音機との調和不調和などを考える暇いとまはないくらい喜んだに相違ない。その時歓声をあげた生徒の中に無論私も交じっていた。")

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

(deftest ^:mlabel mlabel-test
  (let [l (MultiLineLabel.)
        frm (frame "test mlabel" l)]
    (.setText l txt)
    (wait-closing frm)))

(deftest ^:mlabel mlabel-restriction-size-test
  (let [l (doto (MultiLineLabel.)
            (.setBorder (sb/line-border :color :red))
            (.setText txt))
        p (doto (proxy [JPanel] []
                  (paintComponent [g]
                    (.setBounds l 10 10 260 80)
                    (println (.getBounds l))
                    (proxy-super paintComponent g)))
            (.setSize 300 300)
            (.add l))
        frm (frame "test restriction size" p)]
    (wait-closing frm)))

(deftest ^:mlabel mlabel-restriction-width-test
  (let [l (doto (MultiLineLabel.)
            (.setBorder (sb/line-border :color :red))
            (.setText txt))
        p (doto (proxy [JPanel] []
                  (paintComponent [g]
                    (.setSize l 300 0)
                    (let [sz (.getPreferredSize l)
                          w (.width sz)
                          h (.height sz)]
                      (println (format "label size [%d, %d]" w h))
                      (.setBounds l 30 20 300 h))
                    (println (.getBounds l))
                    (proxy-super paintComponent g)))
            (.setSize 300 300)
            (.add l))
        frm (frame "test restriction width" p)]
    (wait-closing frm)))

(deftest ^{:mlabel true :gui true} mlabel-link-test
  (let [ls [(DefaultLinkHandler.)
            (proxy [LinkHandler] ["Safari"]
              (browse [uri]
                (.start (ProcessBuilder.
                         ["open" "-a" "/Applications/Safari.app"
                          (.. uri toURL toString)]))))]
        lhdrs (proxy [LinkHandlers] []
                (getHandlerCount [] (count ls))
                (getHandler [idx] (get ls idx)))
        l (doto (MultiLineLabel.)
            (.setText txt (into-array Link [(Link. 5 10 (URI. url)) (Link. 13 20 (URI. url))]))
            (.setLinkColor (color/to-color :blue))
            (.setLinkHandlers lhdrs)
            (.setBorder (sb/line-border :color :red)))
        p (sm/mig-panel
           :constraints ["wrap 2, ins 5 10 5 10" "[][:400:]"]
           :items [["mlabel (with link):"] [l "grow"]])
        frm (frame "test multiLine label" p)]
    (wait-closing frm)))
