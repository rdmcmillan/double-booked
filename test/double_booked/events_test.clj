(ns double-booked.events-test
  (:require [clojure.test :refer :all]
            [double-booked.events :as db-events]
            [tick.alpha.api :as t]))

; event 0
(def test-event-0-name "event 0")
(def test-event-0-start-date "1970-01-01")
(def test-event-0-start-time "00:00")
(def test-event-0-end-date "1970-01-01")
(def test-event-0-end-time "01:00")
(def test-event-0-start-datetime-str "1970-01-01T00:00:00Z")
(def test-event-0 {:beginning    #time/instant"1970-01-01T00:00:00Z"
                       :end      #time/instant"1970-01-01T01:00:00Z"
                       :interval #:tick{:beginning #time/instant"1970-01-01T00:00:00Z" :end #time/instant"1970-01-01T01:00:00Z"}
                       :name     "event 0"})

(def test-event-1
  {:beginning #time/instant"2020-01-01T11:00:00Z"
   :end #time/instant"2020-01-01T11:45:00Z"
   :interval #:tick{:beginning #time/instant"2020-01-01T11:00:00Z"
                    :end #time/instant"2020-01-01T11:45:00Z"}
   :name "event 1"})

(def test-event-1-overlap-right
  {:beginning #time/instant"2020-01-01T11:30:00Z"
   :end #time/instant"2020-01-01T12:45:00Z"
   :interval #:tick{:beginning #time/instant"2020-01-01T11:30:00Z"
                    :end #time/instant"2020-01-01T12:45:00Z"}
   :name "event 1"})

(def test-event-2
  {:beginning #time/instant"2021-01-01T11:00:00Z"
   :end #time/instant"2021-01-01T12:45:00Z"
   :interval #:tick{:beginning #time/instant"2021-01-01T11:00:00Z"
                    :end #time/instant"2021-01-01T12:45:00Z"}
   :name "event 2"})

(def test-event-2-overlap-left
  {:beginning #time/instant"2021-01-01T10:45:00Z"
   :end #time/instant"2021-01-01T11:45:00Z"
   :interval #:tick{:beginning #time/instant"2021-01-01T10:45:00Z"
                    :end #time/instant"2021-01-01T11:45:00Z"}
   :name "event 2"})

(def test-event-3
  {:beginning #time/instant"2021-01-01T13:00:00Z"
   :end #time/instant"2021-01-01T14:45:00Z"
   :interval #:tick{:beginning #time/instant"2021-01-01T13:00:00Z" 
                    :end #time/instant"2021-01-01T14:45:00Z"}
   :name "event 3"})

(def test-event-duplicates-3
  {:beginning #time/instant"2021-01-01T13:00:00Z"
   :end #time/instant"2021-01-01T14:45:00Z"
   :interval #:tick{:beginning #time/instant"2021-01-01T13:00:00Z" 
                    :end #time/instant"2021-01-01T14:45:00Z"}
   :name "event 3 duplicate"})

(def test-create-event-name "create event")
(def test-create-event-start-date "2070-01-31")
(def test-create-event-start-time "23:00")
(def test-create-event-end-date "2070-02-01")
(def test-create-event-end-time "01:00")

(defn clear-events-fixture [f]
  (reset! db-events/events [])
  (f)
  (reset! db-events/events []))

(use-fixtures :each clear-events-fixture)

(deftest date-time-str-test
  (testing "data-time-str")
  (is (= test-event-0-start-datetime-str
         (db-events/date-time-str
           test-event-0-start-date
           test-event-0-start-time))))

(deftest construct-event-test
  (testing "construct-event")
  (is (= test-event-0
         (db-events/construct-event
           test-event-0-name
           test-event-0-start-date
           test-event-0-start-time
           test-event-0-end-date
           test-event-0-end-time))))

(deftest add-event!-test
  (testing "add-event")
  (is (= test-event-1 (first (db-events/add-event! test-event-1))))
  (is (= 2 (count (db-events/add-event! test-event-3))))
  (is (= 3 (count (db-events/add-event! test-event-0))))
  (is (= 4 (count (db-events/add-event! test-event-2))))
  (testing "event order")
  (is (= test-event-0 (first @db-events/events)))
  (is (= test-event-1 (second @db-events/events)))
  (is (= test-event-3 (last @db-events/events))))

(deftest clear-events-test
  (testing "clear-events")
  (db-events/add-event! test-event-1)
  (is (empty? (db-events/clear-events))))

(deftest create-event!-test
  (testing "creat-event")
  (is (= 1 (count (db-events/create-event!
                    test-create-event-name
                    test-create-event-start-date
                    test-create-event-start-time
                    test-create-event-end-date
                    test-create-event-end-time))))
  (is (= test-create-event-name
         (-> @db-events/events first :name)))
  (is (= (db-events/date-time-str
           test-create-event-start-date
           test-create-event-start-time)
         (-> @db-events/events first :beginning str)))
  (is (= (db-events/date-time-str
           test-create-event-end-date
           test-create-event-end-time)
         (-> @db-events/events first :end str)))
  (is (= (t/new-interval
          (t/instant
            (db-events/date-time-str
              test-create-event-start-date
              test-create-event-start-time))
          (t/instant
            (t/instant
              (db-events/date-time-str
               test-create-event-end-date
               test-create-event-end-time))))
         (-> @db-events/events first :interval))))

(deftest is-overlapped?-test
  (testing "is-overlapped?")
  (testing "same event")
  (is (true? (db-events/is-overlapped? test-event-0 test-event-0)))
  (testing "different events")
  (is (false? (db-events/is-overlapped? test-event-1 test-event-2)))
  (testing "different events, same time")
  (is (true? (db-events/is-overlapped?
               test-event-3
               test-event-duplicates-3)))
  (testing "overlap left event")
  (is (true? (db-events/is-overlapped?
               test-event-2
               test-event-2-overlap-left)))
  (testing "overlap right event")
  (is (true? (db-events/is-overlapped?
               test-event-1
               test-event-1-overlap-right))))

(deftest over-lapped-pairs-vec-test
  (testing "overlapped pairs vec")
  (testing "non colliding events")
  (is (nil? (db-events/over-lapped-pairs-vec
              test-event-0
              test-event-1)))
  (testing "colliding events")
  (is (vector? (db-events/over-lapped-pairs-vec
                 test-event-3
                 test-event-duplicates-3)))
  (testing "event pairs in vec for collision")
  (is (= 2 (count (db-events/over-lapped-pairs-vec
                    test-event-3
                    test-event-duplicates-3))))
  (testing "testing correct event names in pair vec for collision")
  (is (some #{(:name test-event-3)
              (:name test-event-duplicates-3)}
            (map :name (db-events/over-lapped-pairs-vec
                         test-event-3
                         test-event-duplicates-3)))))


(deftest double-booked-vec-test
  (testing "recur-overlapped test from double-booked-vec")
  (testing "non over-lapping events")
  (doall
    (map #(db-events/add-event! %)
         [test-event-0
          test-event-1
          test-event-2
          test-event-3]))
  (is (empty? (db-events/double-booked-vec @db-events/events)))
  (testing "one duplicate event")
  (db-events/add-event! test-event-duplicates-3)
  (is (= 1 (count
             (db-events/double-booked-vec @db-events/events))))
  (db-events/add-event! test-event-1-overlap-right)
  (is (= 2 (count
             (db-events/double-booked-vec @db-events/events))))
  (db-events/add-event! test-event-2-overlap-left)
  (is (= 3 (count
             (db-events/double-booked-vec @db-events/events))))
  (db-events/add-event! test-event-0)
  (is (= 4 (count
             (db-events/double-booked-vec @db-events/events)))))
