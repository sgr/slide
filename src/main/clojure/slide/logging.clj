;; -*- coding: utf-8-unix -*-
(ns slide.logging
  (:require [clojure.string :as s]
            [clojure.tools.logging :as log]
            [logutil :as lu]
            [seesaw.core :as sc]
            [seesaw.bind :as sb]
            [seesaw.mig :as sm]
            [slide.core :as slc])
  (:import [java.awt Dialog$ModalityType Dimension Toolkit]
           [java.awt.datatransfer StringSelection]
           [java.util.logging Level Logger LogManager]
           [javax.swing JOptionPane JScrollPane JTable ListSelectionModel SwingConstants WindowConstants]
           [javax.swing.table JTableHeader TableColumn]
           [com.github.sgr.slide MultiLineTable]
           [com.github.sgr.slide.logging LogRecordRenderer LogRecordRow TableModelHandler]))

(def COLS {:date    {:midx 0 :width 180 :display-name "Date"    :resizable true}
           :thread  {:midx 1 :width 50  :display-name "Thread"  :resizable true}
           :level   {:midx 2 :width 50  :display-name "Level"   :resizable true}
           :logger  {:midx 3 :width 150 :display-name "Logger"  :resizable true}
           :class   {:midx 4 :width 150 :display-name "Class"   :resizable true}
           :method  {:midx 5 :width 80  :display-name "Method"  :resizable true}
           :message {:midx 6 :width 450 :display-name "Message" :resizable true}})

(def LOG-TABLE-HEIGHT 400)

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

(defn- ^TableModelHandler handler [capacity level]
  (doto (TableModelHandler. capacity)
    (.setLevel ({:severe Level/SEVERE :warning Level/WARNING :info Level/INFO
                 :fine Level/FINE :finer Level/FINER :finest Level/FINEST}
                level))))

(let [hdrs {:severe  (atom nil)
            :warning (atom nil)
            :info    (atom nil)
            :fine    (atom nil)
            :finer   (atom nil)
            :finest  (atom nil)}
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
      (doseq [[k ref-hdr] hdrs]
        (let [hdr (handler capacity k)]
          (reset! ref-hdr hdr)
          (.addHandler root-logger hdr)))))

  (defn copy-log-to-clipboard-from-handler
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
       {:pre [(contains? (set (keys hdrs)) level)]}
       (copy-log-to-clipboard-from-handler (hdrs level)))
    ([] (copy-log-to-clipboard :fine)))

  (defn log-panel
    "Return a JPanel for viewing logs.
     Call \"configure-logging-swing\" before calling this function."
    []
    (letfn [(gen-cols [col-keys rdr]
              (map #(let [{:keys [midx width display-name resizable]} (get COLS %)]
                      (table-column midx width (name %) display-name rdr resizable))
                   col-keys))
            (reset-model [tbl hdr cols]
              (reset! current-handler @hdr)
              (.setModel tbl (.getModel @hdr))
              (-> tbl .getTableHeader (.setReorderingAllowed false))
              (-> tbl .getColumnModel (.setColumnMargin 0))
              (doseq [col cols] (.addColumn tbl col)))]
              
      (let [col-keys [:date :thread :level :message]
            tbl (doto (MultiLineTable.)
                  (.setPreferredScrollableViewportSize
                   (Dimension. (->> (map #(get-in COLS [% :width]) col-keys) (apply +)) LOG-TABLE-HEIGHT))
                  (.setSelectionMode ListSelectionModel/SINGLE_SELECTION)
                  (.setAutoResizeMode JTable/AUTO_RESIZE_NEXT_COLUMN))
            rdr (LogRecordRenderer.)
            cols (gen-cols col-keys rdr)
            cbtn (sc/button
                  :text "Copy log to clipboard"
                  :listen [:action
                           (fn [_] (copy-log-to-clipboard-from-handler current-handler))])
            combo (sc/combobox
                   :model (keys hdrs)
                   :renderer (fn [rdr m]
                               (.setHorizontalAlignment rdr SwingConstants/CENTER)
                               (.setText rdr (-> (:value m) name s/upper-case)))
                   :listen [:selection
                            (fn [e]
                              (when-let [hdr (hdrs (sc/selection (.getSource e)))]
                                (reset-model tbl hdr cols)))])]
        (sc/selection! combo :info)
        (sc/border-panel :north (sm/mig-panel
                                 :constraints ["wrap 3, ins 5 10 5 10" "[:450:][:150:][:200:]" ""]
                                 :items [["Log level" "align right"] [combo "grow"] [cbtn "align right"]])
                         :center (sc/scrollable tbl)))))

  (defn log-dlg
    "Return a dialog for viewing logs.
      (log-dlg parent title options)
     \"parent\" is a parent frame.
     options can also be one of:
       :visible? is set to true, the dialog will be displayed. (default value is false)
     Call \"configure-logging-swing\" before calling this function."
    [parent title & {:keys [visible?] :or {visible? false}}]
    (let [op (doto (JOptionPane.)
               (.setOptionType JOptionPane/DEFAULT_OPTION)
               (.setMessage (log-panel)))
          dlg (doto (.createDialog op parent title)
                (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE)
                (.setLocationRelativeTo parent)
                (.setModalityType Dialog$ModalityType/MODELESS)
                (.pack))]
      (if visible?
        (doto dlg (.setVisible true))
        dlg))))
