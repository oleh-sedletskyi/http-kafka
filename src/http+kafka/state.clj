(ns http+kafka.state
  (:require [http+kafka.utils :as utils]
            [http+kafka.kafka :as kafka]))

(def filters (atom {}))
(def update-filters? (atom false))

(def topics (atom {}))
(def messages (atom ()))
(def consumers (atom {}))

(defn continue? [consumer]
  (get @consumers consumer))

(defn set-consumer-status [consumer continue?]
  (swap! consumers assoc consumer continue?))

(defn subscribe-to-topic [topic]
  (when-not (get @topics topic)
    (kafka/start-consumer-thread! topic)))

(defn add-filter [f]
  (let [elem (->> @filters
                  vals
                  (filter #(and (= (:topic f) (:topic %))
                                (= (:q f) (:q %))))
                  first)]
    (when (nil? elem)
      (let [id (if (= @filters {})
                 0
                 (->> @filters keys sort last inc))]
        (swap! filters assoc id (assoc f :id id))
        (reset! update-filters? true)
        (subscribe-to-topic (:topic f))))))

(defn clean-messages [topic]
  (swap! messages #(remove (fn [m] (= (:topic m) topic)) %)))

(defn clean-topics [topic]
  (let [remaining-topics (->> @filters vals (map :topic))]
    (when-not (utils/in? remaining-topics topic)
      (let [consumer (get @topics topic)]
        (swap! topics dissoc topic)
        (swap! consumers dissoc consumer))
      (clean-messages topic))))

(defn delete-filter [id]
  (let [f (get @filters id)]
    (when f
      (swap! filters dissoc (:id f))
      (reset! update-filters? true)
      (clean-topics (:topic f)))))

(defn get-messages [id]
  (let [{:keys [topic q]} (get @filters id)]
    (->> @messages
         (filter #(and (= topic (:topic %))
                       (utils/match-by-patterns (:msg %) [q])))
         (map :msg))))

(comment
  (add-filter {:topic "books"
                 :q "sicp"}
              #_{:topic "books"
                 :q "python"}
              #_{:topic "movies"
               :q "mart"})

  (delete-filter 0)

  (get-messages 1)

  @filters
  @messages
  @topics
  @consumers

  @update-filters?

  ;;
  )
