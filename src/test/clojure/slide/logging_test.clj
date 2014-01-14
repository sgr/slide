(ns slide.logging-test
  (:require [clojure.test :refer :all]
            [clojure.tools.logging :as log]
            [slide.logging :refer :all])
  (:import [java.awt Dimension GraphicsEnvironment]
           [java.awt.event ActionListener WindowAdapter]
           [javax.swing JButton JFrame SwingUtilities WindowConstants]))

(defn- test-frame []
  (let [e (GraphicsEnvironment/getLocalGraphicsEnvironment)
        p (.getCenterPoint e)
        width 300
        height 200
        frame (JFrame. "test frame")
        btn (doto (JButton. "display log dialog")
              (.addActionListener
               (proxy [ActionListener][]
                 (actionPerformed [evt]
                   (doto (log-dlg frame "Application log")
                     (.setVisible true))))))]
    (doto frame
      (.setContentPane btn)
      (.setPreferredSize (Dimension. width height))
      (.pack)
      (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE)
      (.setLocation (- (.x p) (int (/ width 2))) (- (.y p) (int (/ height 2)))))))

(defn- wait-closing [frame]
  (let [p (promise)]
    (.addWindowListener frame (proxy [WindowAdapter][]
                                (windowClosed [evt]
                                  (deliver p true))))
    (SwingUtilities/invokeAndWait (fn [] (.setVisible frame true)))
    @p))

(defn- log-all-level [i]
  (doseq [level [:info :debug :trace :warn :error :fatal]]
    (log/logf level "logged [%d] as %s" i (name level))))

(def ^{:private true} PMAP-GUI
  {".level" "ALL"
   "logutil-test.level" "ALL"})

(deftest ^:integration gui-log-test
  (testing "display log dialog"
    (configure-logging-swing 30 PMAP-GUI)
    (doseq [i (range 50)] (log-all-level i))
    (wait-closing (test-frame))))

