(ns http+kafka.handlers
  (:require [http+kafka.state :refer [topics filters messages consumers]]
            [http+kafka.kafka :refer [start-consumer-thread!]]
            [http+kafka.utils :as utils]))

(defn subscribe-to-topic! [topic]
  (when-not (get @topics topic)
    (start-consumer-thread! topic)))

(defn add-filter! [f]
  (let [elem (->> @filters
                  vals
                  (filter #(and (= (:topic f) (:topic %))
                                (= (:q f) (:q %))))
                  first)]
    (when (nil? elem)
      (swap! filters (fn [fs]
                       ;; Generate id inside swap! to avoid race condition
                       (let [id (if (= fs {})
                                  0
                                  (->> fs keys sort last inc))]
                         (assoc fs id (assoc f :id id)))))
      (subscribe-to-topic! (:topic f)))))

(defn clean-messages! [topic]
  (swap! messages #(remove (fn [m] (= (:topic m) topic)) %)))

(defn clean-topics! [topic]
  (let [remaining-topics (->> @filters vals (map :topic))]
    (when-not (utils/in? remaining-topics topic)
      (let [consumer (get @topics topic)]
        (swap! topics dissoc topic)
        (swap! consumers dissoc consumer))
      (clean-messages! topic))))

(defn delete-filter! [id]
  (let [f (get @filters id)]
    (when f
      (swap! filters dissoc (:id f))
      (clean-topics! (:topic f)))))

(defn get-messages [id]
  (let [{:keys [topic q]} (get @filters id)]
    (->> @messages
         (filter #(and (= topic (:topic %))
                       (utils/match-by-patterns (:msg %) [q])))
         (map :msg))))

(defn get-filters []
  (->> @filters
       vals))

(comment
  (add-filter! {:topic "books"
                  :q "sicp"}
               #_{:topic "books"
                :q "python"}
               #_{:topic "movies"
                  :q "mart"})

  (get-messages 1)
  (get-filters)
  @filters
  @messages
  @topics
  @consumers

  (clean-topics! "books")
  (delete-filter! 0)
  (reset! consumers {})

  ;;
  )
