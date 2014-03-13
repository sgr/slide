;; -*- coding: utf-8-unix -*-
(ns slide.logging-test
  (:require [clojure.test :refer :all]
            [clojure.tools.logging :as log]
            [seesaw.core :as sc]
            [slide.core :as slc]
            [slide.logging :refer :all]))

(defn- test-frame [title content]
  (sc/frame
   :title title
   :content content
   :size [300 :by 200]
   :on-close :dispose))

(defn- wait-closing [frame]
  (let [p (promise)]
    (sc/listen frame :window-closing (fn [_] (deliver p true)))
    (-> frame slc/move-to-center! sc/show!)
    @p))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest ^:logging gui-log-test
  (testing "display log dialog"
    (configure-logging-swing 100 {"handlers" "java.util.logging.ConsoleHandler"
                                  "java.util.logging.ConsoleHandler.formatter" "logutil.Log4JLikeFormatter"
                                  ".level" "INFO"
                                  "slide.logging-test.level" "ALL"})
    ;; (configure-logging-swing 100 {".level" "INFO"
    ;;                               "slide.logging-test.level" "ALL"})
    (doseq [i (range 20)]
      (doseq [level [:info :debug :trace :warn :error :fatal]]
        (log/logf level "logged [%d] as %s" i (name level))))

    (let [btn (sc/button :text "display log dialog")
          frm (test-frame "gui-log-test" btn)]
      (sc/listen btn :action
                 (fn [_]
                   (try
                     (log-dlg frm "test log" :visible? true)
                     (catch Exception e
                       (log/errorf e "An error occurred")))))
      (wait-closing frm))))
