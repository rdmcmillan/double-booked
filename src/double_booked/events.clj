(ns double-booked.events
  (:require [tick.alpha.api :as t]))

;Double Booked
;When maintaining a calendar of events, it is important to know if an
; event overlaps with another event.
;
;Given a sequence of events, each having a start and end time,
; write a program that will return the sequence of all pairs of
; overlapping events.

; Event collection ref
(def events (atom []))

(defn clear-events
  "Clear the event collection."
  []
  (reset! events []))

(defn date-time-str
  "Given a date yyyy-mm-dd, and a 24hr time hh:mm
  creates a date time string for creating Tick instances."
  [date time]
  (str date "T" time ":00Z"))

(defn construct-event
  "Given an event name, start and end dates and times strings,
  creates and event map including the Tick interval for the event."
  [event-name start-date start-time end-date end-time]
  (let [start (t/instant (date-time-str
                           start-date
                           start-time))
        end (t/instant (date-time-str
                         end-date
                         end-time))]
    {:beginning start
     :end       end
     :interval  (t/new-interval start end)
     :name      event-name}))

(defn add-event!
  "Adds an event to the events atom, in chronological order."
  [event-map]
  (swap! events
         (fn [events-atom]
           (sort-by                                         ;; Sort by the starting date/time of events
             :beginning
             (conj events-atom event-map)))))

(defn create-event!
  "Creates an event map  and adds it to the ordered events atom (vec)."
  [event-name start-date start-time end-date end-time]
  (let [event (construct-event
                event-name
                start-date
                start-time
                end-date
                end-time)]
    (add-event! event)))

; Tick relation keys returning from colliding intervals
(def collision-keys
  #{:overlaps
    :overlapped-by
    :starts :started-by
    :during
    :contains
    :finishes
    :finished-by
    :equals})

(defn is-overlapped?
  "Given two event maps, checks for collisions and returns bool."
  [event-1 event-2]
  (boolean
    (some
      #(= (t/relation (:interval event-1) (:interval event-2)) %) ;; Check for relation key in collision key set
      collision-keys)))

(defn over-lapped-pairs-vec
  "Tests the two events to for a collision.
  If the two events represent a double booking, returns
  both events in a vec, else nil."
  [event-1 event-2]
  (when (is-overlapped? event-1 event-2)
    [event-1 event-2]))

(defn recur-over-lapped
  "Recursively iterate oven a sorted event vec,
  composing a vector of vectors containing pairs of colliding events.
  Empty vec returned when there are no double booked events."
  [events-vec current-event the-rest]
  (if (empty? the-rest)
    events-vec
    (recur
      (into events-vec
            (map
              #(over-lapped-pairs-vec current-event %) the-rest))    ;; Compose double booked vec
      (first the-rest)
      (rest the-rest))))

(defn double-booked-vec
  "Initialize recur-over-lapped.
  Given a collection of evnt maps, returns a collection of vectors
  containing pairs of double booked events.
  If the resulting collection is nil, there are no conflicts"
  [schedule-vec]
  (vec
   (remove nil?
           (recur-over-lapped
             []
             (first schedule-vec)
             (rest schedule-vec)))))
