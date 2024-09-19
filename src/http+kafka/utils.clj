(ns http+kafka.utils
  (:require [clojure.string :as str]))

(defn in?
  "true if coll contains elm"
  [coll elm]
  (some #(= elm %) coll))

(defn match-by-patterns [s patterns]
  (->> patterns
       (some #(str/includes? (str/lower-case s) (str/lower-case %)))))
