;; -*- coding: utf-8-unix -*-
(ns slide.core
  (:require [seesaw.core :as sc])
  (:import [java.awt Dimension GraphicsEnvironment Window]))

(let [locale (java.util.Locale/getDefault)]
  (defn btn-label [name]
    (javax.swing.UIManager/getString (str "OptionPane." name "ButtonText") locale)))

(defn dialog
  "An extension of seesaw.core/dialog.
   In this dialog, [OK] button is disabled and default button is [Cancel].

   You can use options same as seesaw dialog.
   In addition to this, :id-ok for setting id of [OK] button."
  [& {:as opts}]
  (let [{:keys [id-ok options option-type success-fn cancel-fn]} opts]
    (if (and (nil? options) (= :ok-cancel option-type))
      (let [id-ok (or id-ok :btn-ok)
            btn-ok (sc/button :id id-ok :text (btn-label "ok") :enabled? false)
            btn-cancel (sc/button :id :btn-cancel :text (btn-label "cancel"))
            dlg (apply sc/dialog (apply concat (-> opts
                                                   (dissoc :option-type :id-ok)
                                                   (assoc :options [btn-ok btn-cancel]
                                                          :default-option btn-cancel))))]
        (sc/listen btn-ok :action
                   (fn [e] (sc/return-from-dialog e ((or success-fn (fn [_] :success)) dlg))))
        (sc/listen btn-cancel :action
                   (fn [e] (sc/return-from-dialog e ((or cancel-fn (fn [_])) dlg))))
        dlg)
      (apply sc/dialog (apply concat opts)))))

(defn confirm-dlg
  "デフォルトボタンが「いいえ」であるような、はい/いいえ確認ダイアログを表示する。
   ユーザーが「はい」を押すと:yes、「いいえ」を押すと:noが返る。"
  [title message]
  (let [btn-yes (sc/button :id :yes :text (btn-label "yes")
                           :listen [:action (fn [e] (sc/return-from-dialog e :yes))])
        btn-no  (sc/button :id :no  :text (btn-label "no")
                           :listen [:action (fn [e] (sc/return-from-dialog e :no))])]
    (-> (sc/dialog :title title
                   :content message
                   :options [btn-yes btn-no]
                   :default-option [btn-no]
                   :on-close :dispose)
        sc/pack!)))

(defn move-to-center!
  ([child parent]
     (let [root (sc/to-root parent)
           p (.getLocationOnScreen root)
           w (.getWidth root)
           h (.getHeight root)]
       (sc/move! child :to [(+ (.x p) (int (/ (- w (.getWidth child)) 2)))
                            (+ (.y p) (int (/ (- h (.getHeight child)) 2)))])))
  ([target]
     (if (instance? java.awt.Window target)
       (let [p (.getCenterPoint (GraphicsEnvironment/getLocalGraphicsEnvironment))
             w (.getWidth target)
             h (.getHeight target)]
         (sc/move! target :to [(- (.x p) (int (/ w 2)))
                               (- (.y p) (int (/ h 2)))]))
       (throw (IllegalArgumentException. (format "Invalid class: %s" (class target)))))))
