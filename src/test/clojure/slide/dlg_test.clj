;; -*- coding: utf-8-unix -*-
(ns slide.dlg-test
  (:require [clojure.test :refer :all]
            [clojure.tools.logging :as log]
            [seesaw.core :as sc]
            [slide.core :refer :all]))

(defn- confirm [title msg]
  (-> (confirm-dlg title msg) move-to-center! sc/show!))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest ^:dlg confirm-dlg-test
  (testing "confirm dialog test"
    (is (= :yes (confirm "Question" "Press yes!")))
    (is (= :no  (confirm "Question" "Press no!")))))

(deftest ^:dlg slc-dlg-test
  (testing "slc dialog test"
    (is (= :success (-> (dialog :title "pass-through seesaw dialog"
                                :content "This is seesaw dialog"
                                :size [300 :by 200]
                                :on-close :dispose)
                        move-to-center!
                        sc/show!)))
    (is (nil? (-> (dialog :title "slc dialog"
                          :content "Press [cancel]"
                          :option-type :ok-cancel
                          :size [300 :by 200]
                          :on-close :dispose)
                  move-to-center!
                  sc/show!)))
    (let [dlg (dialog :title "slc dialog enabled [ok]"
                      :content "Press [ok]"
                      :id-ok :foo
                      :option-type :ok-cancel
                      :size [300 :by 200]
                      :on-close :dispose)
          btn (sc/select dlg [:#foo])]
      (sc/config! btn :enabled? true)
      (is (= :success (-> dlg move-to-center! sc/show!))))
    (let [dlg (dialog :title "slc dialog set success-fn"
                      :content "Press [ok]"
                      :id-ok :foo
                      :success-fn (fn [_] :hoge)
                      :option-type :ok-cancel
                      :size [300 :by 200]
                      :on-close :dispose)
          btn (sc/select dlg [:#foo])]
      (sc/config! btn :enabled? true)
      (is (= :hoge (-> dlg move-to-center! sc/show!))))
    (is (= :huga (-> (dialog :title "slc dialog set cancel-fn"
                             :content "Press [cancel]"
                             :option-type :ok-cancel
                             :cancel-fn (fn [_] :huga)
                             :size [300 :by 200]
                             :on-close :dispose)
                     move-to-center!
                     sc/show!)))
    ))
