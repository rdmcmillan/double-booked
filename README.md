# double-booked
[![rdmcmillan](https://circleci.com/gh/rdmcmillan/double-booked.svg?style=svg)](https://app.circleci.com/pipelines/github/rdmcmillan/double-booked)

When maintaining a calendar of events, it is important to know if an event overlaps with another event.

Given a sequence of events, each having a start and end time, this program will return the sequence of all pairs of overlapping events.

Relying heavily on: [juxt tick](https://github.com/juxt/tick)

## Dependencies
Local jvm and [leingingen](https://leiningen.org/)

## Usage
run all test via `lein test` from the project root.

## Examples

From a repl, load the `double-booked.events` namespace.

Call `create-event!` with the following args:

event-name      ;; String representing the name of the calendar event

start-date      ;; String representing the starting date of the event yyyy-mm-dd

start-time      ;; String representing the starting time (24hr) of the event hh:mm

end-date        ;; String representing the ending date of the event yyyy-mm-dd

end-time        ;; String representing the ending time (24hr) of the event hh:mm

Like such:
`(create-event! "first-event" "2020-01-01" "10:45" "2020-01-01" "11:15")`

This event, and all subsequent events, are populated into the events vector atom
ordered by start date-time as a convenience for temporarily persisting a sequence
of events.

Continue to add events.

`(create-event! "second-event" "2020-01-01" "12:00" "2020-01-01" "13:00")`

`(create-event! "third-event" "2020-01-01" "12:30" "2020-01-01" "13:30")`

`(create-event! "fourth-event" "2020-01-02" "15:00" "2020-01-02" "16:30")`

To check for double booked (overlapping/colliding) events, call:

`(double-booked-vec @events)`

This will return a vector representing any double booked events as vector pairs:  

`[[{:beginning #time/instant"2020-01-01T12:00:00Z",
:end #time/instant"2020-01-01T13:00:00Z",
:interval #:tick{:beginning #time/instant"2020-01-01T12:00:00Z", :end #time/instant"2020-01-01T13:00:00Z"},
:name "second-event"}
{:beginning #time/instant"2020-01-01T12:30:00Z",
:end #time/instant"2020-01-01T13:30:00Z",
:interval #:tick{:beginning #time/instant"2020-01-01T12:30:00Z", :end #time/instant"2020-01-01T13:30:00Z"},
:name "third-event"}]]`

When no double-booked events are present, `(double-booked-vec @events)` will return an empty vector.
Like such:

`[]`

The convenience events atom can be cleared by reloading the namespace, or by calling:

`(clear-events)`

## TODO
- CLI
- web interface
- RESTful API
- more tests




