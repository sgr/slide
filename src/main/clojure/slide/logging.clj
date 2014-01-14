;; -*- coding: utf-8-unix -*-
(ns slide.logging
  (:require [clojure.string :as s]
            [clojure.tools.logging :as log]
            [logutil :as lu])
  (:import [java.awt BorderLayout Dialog$ModalityType Dimension Toolkit]
           [java.awt.datatransfer StringSelection]
           [java.awt.event ActionListener ItemEvent ItemListener]
           [java.io ByteArrayInputStream ByteArrayOutputStream InputStream]
           [java.util.logging Level Logger LogManager]
           [javax.swing GroupLayout JButton JComboBox JDialog JLabel JPanel JOptionPane
            JScrollPane JTable ListSelectionModel SwingConstants WindowConstants]
           [javax.swing.table JTableHeader TableColumn]
           [com.github.sgr.slide MultiLineTable]
           [com.github.sgr.slide.logging LogRecordRenderer LogRecordRow TableModelHandler]))

(def ^{:private true} PMAP-GUI-EXCLUDE
  {"java.level" "INFO"
   "javax.level" "INFO"
   "sun.level" "INFO"})

(defn- ^TableColumn table-column [idx width identifier display-name renderer resizable]
  (doto (TableColumn.)
    (.setModelIndex idx)
    (.setPreferredWidth width)
    (.setIdentifier identifier)
    (.setHeaderValue display-name)
    (.setCellRenderer renderer)
    (.setResizable resizable)))

(let [handler-severe  (atom nil)
      handler-warning (atom nil)
      handler-info    (atom nil)
      handler-fine    (atom nil)
      handler-finer   (atom nil)
      handler-finest  (atom nil)
      current-handler (atom nil)
      formatter (logutil.Log4JLikeFormatter.)]
  (defn configure-logging-swing
  "Initialize logging configuration for Swing GUI.
   The format of property-map is similar to configure-logging."
    [capacity property-map]
    (let [root-logger (Logger/getLogger "")]
      (doseq [h (.getHandlers root-logger)] (.removeHandler root-logger h))
      (doto (LogManager/getLogManager) (.reset))
      (lu/configure-logging (merge property-map PMAP-GUI-EXCLUDE))
      (reset! handler-severe  (doto (TableModelHandler. capacity) (.setLevel Level/SEVERE)))
      (reset! handler-warning (doto (TableModelHandler. capacity) (.setLevel Level/WARNING)))
      (reset! handler-info    (doto (TableModelHandler. capacity) (.setLevel Level/INFO)))
      (reset! handler-fine    (doto (TableModelHandler. capacity) (.setLevel Level/FINE)))
      (reset! handler-finer   (doto (TableModelHandler. capacity) (.setLevel Level/FINER)))
      (reset! handler-finest  (doto (TableModelHandler. capacity) (.setLevel Level/FINEST)))
      (doto root-logger
        (.addHandler @handler-severe)
        (.addHandler @handler-warning)
        (.addHandler @handler-info)
        (.addHandler @handler-fine)
        (.addHandler @handler-finer)
        (.addHandler @handler-finest))))

  (defn- copy-log-to-clipboard-from-handler
    ([hdr-ref]
       (if-let [tk (Toolkit/getDefaultToolkit)]
         (if-let [cboard (.getSystemClipboard tk)]
           (let [log-text (if (instance? TableModelHandler @hdr-ref)
                            (s/join (map #(.format formatter %) (.records @hdr-ref)))
                            (format "Log handler is nil: (%s)" (pr-str @hdr-ref)))
                 ss (StringSelection. log-text)]
             (.setContents cboard ss ss))
           (log/error "couldn't get system clipboard"))
         (log/error "couldn't get default toolkit")))
    ([] (copy-log-to-clipboard-from-handler current-handler)))

  (defn copy-log-to-clipboard
    "Copy log message to system clipboard.
     \"level\" is either of :severe :warning :info :fine :finer :finest.
     The number of log records is specified to configure-logging-swing as \"capacity\"."
    ([^clojure.lang.Keyword level]
       {:pre [(contains? #{:severe :warning :info :fine :finer :finest} level)]}
       (copy-log-to-clipboard-from-handler (condp = level
                                             :severe  handler-severe
                                             :warning handler-warning
                                             :info    handler-info
                                             :fine    handler-fine
                                             :finer   handler-finer
                                             :finest  handler-finest)))
    ([] (copy-log-to-clipboard :fine)))

  (defn ^JPanel log-panel
    "Return a JPanel for viewing logs.
     Call \"configure-logging-swing\" before calling this function."
    []
    (letfn [(reset-model [tbl hdr cols]
              (reset! current-handler @hdr)
              (.setModel tbl (.getModel @hdr))
              (-> tbl .getTableHeader (.setReorderingAllowed false))
              (-> tbl .getColumnModel (.setColumnMargin 0))
              (doseq [col cols] (.addColumn tbl col)))]
              
      (let [rdr (LogRecordRenderer.)
            cols [(table-column 0 180 "date"    "Date"    rdr true)
                  (table-column 1 20  "thread"  "Thread"  rdr true)
                  (table-column 2 80  "level"   "Level"   rdr true)
                  (table-column 3 150 "logger"  "Logger"  rdr true)
                  ;; (table-column 4 150 "class"   "Class"   rdr true)
                  ;; (table-column 5 80  "method"  "Method"  rdr true)
                  (table-column 6 450 "message" "Message" rdr true)]
            tbl (doto (MultiLineTable.)
                  (.setPreferredScrollableViewportSize (Dimension. 850 400))
                  (.setSelectionMode ListSelectionModel/SINGLE_SELECTION)
                  (.setAutoResizeMode JTable/AUTO_RESIZE_NEXT_COLUMN))
            stbl (JScrollPane. tbl)
            levels ["SEVERE" "WARNING" "INFO" "FINE" "FINER" "FINEST"]
            combo (doto (JComboBox. (into-array String levels))
                    (.setSelectedItem "INFO")
                    (.addItemListener
                     (proxy [ItemListener][]
                       (itemStateChanged [evt]
                         (when (= ItemEvent/SELECTED (.getStateChange evt))
                           (when-let [hdr (condp = (nth levels (-> evt .getSource .getSelectedIndex))
                                            "SEVERE"  handler-severe
                                            "WARNING" handler-warning
                                            "INFO"    handler-info
                                            "FINE"    handler-fine
                                            "FINER"   handler-finer
                                            "FINEST"  handler-finest)] 
                             (reset-model tbl hdr cols)))))))
            cbtn (doto (JButton. "Copy log to clipboard")
                   (.addActionListener
                    (proxy [ActionListener][]
                      (actionPerformed [evt]
                        (copy-log-to-clipboard-from-handler)))))
            clabel (doto (JLabel. "Log level " SwingConstants/TRAILING)
                     (.setMinimumSize (Dimension. 450 (->> combo .getPreferredSize .height))))
            cpanel (JPanel.)
            layout (GroupLayout. cpanel)
            hgrp (doto (.createSequentialGroup layout)
                   (.addGroup (.. layout createParallelGroup
                                  (addGroup (.. layout createSequentialGroup
                                                (addComponent clabel)
                                                (addComponent combo)
                                                (addComponent cbtn)))
                                  (addComponent stbl))))
            vgrp (doto (.createSequentialGroup layout)
                   (.addGroup (.. layout createParallelGroup
                                  (addComponent clabel)
                                  (addComponent combo)
                                  (addComponent cbtn)))
                   (.addGroup (.. layout createParallelGroup
                                  (addComponent stbl))))]
        (reset-model tbl handler-info cols)

        (doto layout
          (.setHorizontalGroup hgrp)
          (.setVerticalGroup vgrp)
          (.setAutoCreateGaps true)
          (.setAutoCreateContainerGaps true))
        (doto cpanel
          (.setLayout layout)))))

  (defn ^JDialog log-dlg
    "Display a dialog for viewing logs.
     \"parent\" is a parent frame.
     Call \"configure-logging-swing\" before calling this function."
    [parent title]
    (when-not @handler-info (configure-logging-swing 100 {}))
    (let [op (doto (JOptionPane.)
               (.setOptionType JOptionPane/DEFAULT_OPTION)
               (.setMessage (log-panel)))]
      (if parent
        (doto (.createDialog op parent title)
          (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE)
          (.setLocationRelativeTo parent)
          (.setModalityType Dialog$ModalityType/MODELESS)
          (.pack))
        (doto (.createDialog op title)
          (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE)
          (.setModalityType Dialog$ModalityType/MODELESS)
          (.pack))))))

