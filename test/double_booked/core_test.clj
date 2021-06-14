(ns double-booked.core-test
  (:require [clojure.test :refer :all]
            [double-booked.core :refer :all]))

(deftest reality-check
  (testing "Reality check."
    (is (= 1 1))))
