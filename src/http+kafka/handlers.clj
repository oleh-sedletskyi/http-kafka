(ns http+kafka.handlers
  (:require [http+kafka.kafka :as kafka]
            [http+kafka.state :as state]
            [http+kafka.utils :as utils]))

(defn subscribe-to-topic! [topic]
  (when-not (state/get-consumer-by-topic topic)
    (kafka/start-consumer-thread! topic)))

#_(defn add-filter! [f]
    (let [exists? (state/filter-exists? f)]
      (when-not exists?
        (state/add-to-filters! f)
        (subscribe-to-topic! (:topic f)))))

(defn add-filter! [f]
  (state/add-to-filters! f)
  (subscribe-to-topic! (:topic f)))

(defn clean-topics! [topic]
  (let [remaining-topics (state/get-topics)]
    (when-not (utils/in? remaining-topics topic)
      (let [consumer (state/get-consumer-by-topic topic)]
        (state/drop-topic! topic)
        (state/drop-consumer! consumer))
      (state/remove-messages-by-topic! topic))))

(defn delete-filter! [id]
  (let [f (state/get-filter id)]
    (when f
      (state/drop-filter! id)
      (clean-topics! (:topic f)))))

(defn get-messages [filter-id]
  (let [{:keys [topic q]} (state/get-filter filter-id)]
    (->> (state/get-messages)
         (filter #(and (= topic (:topic %))
                       (utils/match-by-patterns (:msg %) [q])))
         (map :msg))))

(defn get-filters []
  (state/get-filters))

(defn get-filter [id]
  (state/get-filter id))

(comment
  (add-filter! #_{:topic "books"
                  :q "sicp"}
   #_{:topic "books"
      :q "python"}
   {:topic "movies"
    :q "mar"})

  (get-messages 1)
  (get-filters)
  (get-filter 1)

  @state/filters
  @state/messages
  @state/topics
  @state/consumers

  (delete-filter! 1)

  ;;
  )
