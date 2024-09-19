(ns http+kafka.state)

(defonce filters (atom {}))
(defonce topics (atom {}))
(defonce messages (atom ()))
(defonce consumers (atom {}))

(defn continue? [consumer]
  (get @consumers consumer))

(defn set-consumer-status! [consumer continue?]
  (swap! consumers assoc consumer continue?))
