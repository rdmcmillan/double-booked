(ns double-booked.core
  (:require [tick.alpha.api :as tick])
  (:gen-class))

; TODO cli
(defn -main
  "I still don't do a whole lot ... yet."
  [& args]
  (println "Enter an event name, ")
  (let [name (read-line)]
    (println "event name is: " name)))
