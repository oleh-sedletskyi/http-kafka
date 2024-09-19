(ns http+kafka.kafka
  (:require [http+kafka.state :as state]
            [http+kafka.utils :as utils]
            [jackdaw.client :as jc]
            [jackdaw.serdes :refer [string-serde edn-serde]]
            [taoensso.timbre :refer [info]])
  (:import [org.apache.kafka.common.errors WakeupException]))

(def kafka-config {"bootstrap.servers" "localhost:58686"})

(defn consumer-config [topic]
  (merge kafka-config
         {"group.id"          "example.group-id"
          "client.id"         (str "consumer-" (name topic))
          "auto.offset.reset" "earliest"}))

(defn topic-config [topic]
  {:topic-name topic
   :key-serde (string-serde)
   :value-serde (edn-serde)})

(defn poll-and-loop!
  "Continuously fetches records every `poll-ms`, processes them and commits offset after each poll."
  [consumer topic processing-fn]
  (let [poll-ms 5000]
    (loop []
      (when (state/continue? consumer)
        (let [records (jc/poll consumer poll-ms)]
          (when (seq records)
            (processing-fn topic records)
            (info "commit sync at offset" (-> records last :offset inc))
            (.commitSync consumer))
          (recur))))))

(defn stop-and-close-consumer!
  "Stops the consumer polling loop and closes the consumer."
  [consumer]
  (state/set-consumer-status consumer false)
  (.close consumer)
  (swap! state/consumers dissoc consumer)
  (info "Closed Kafka Consumer"))

(defn start-consumer!
  "Starts consumer loop to process events read from `topic`"
  [consumer topic processing-fn]
  (try
    (poll-and-loop! consumer topic processing-fn)
    (catch WakeupException e) ;; ignore for shutdown
    (finally
      (stop-and-close-consumer! consumer))))

(defn add-shutdown-hook-consumer!
  "Registers a shutdown hook to exit the consumer cleanly"
  [consumer]
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. (fn []
                               (info "Stopping Kafka Consumer...")
                               (state/set-consumer-status consumer false)
                               (.wakeup consumer)))))

(defn process-messages!
  "Creates Kafka Consumer and shutdown hook, and starts the consumer"
  [topic processing-fn]
  (let [topic-config    (topic-config topic)
        consumer-config (consumer-config topic)
        consumer        (jc/subscribed-consumer consumer-config [topic-config])]
    (swap! state/topics assoc topic consumer)
    (state/set-consumer-status consumer true)
    (add-shutdown-hook-consumer! consumer)
    (start-consumer! consumer topic processing-fn)))

(defn process-records [topic records]
  (let [msg (->> records (map :value) str)
        patterns (->> @state/filters
                      vals
                      (map :q))]
    (info "Message:" msg)
    (when (utils/match-by-patterns msg patterns)
      (info "Adding message: " {:topic topic :msg msg})
      (swap! state/messages conj {:topic topic
                                  :msg msg}))))

(defn start-consumer-thread! [topic]
  (-> (Thread. #(process-messages! topic process-records))
      (.start)))
