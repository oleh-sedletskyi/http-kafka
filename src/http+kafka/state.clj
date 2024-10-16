(ns http+kafka.state)

(defonce filters (atom {}))
(defonce topics (atom {}))
(defonce messages (atom ()))
(defonce consumers (atom {}))

(defn continue? [consumer]
  (get @consumers consumer))

(defn set-consumer-status! [consumer continue?]
  (swap! consumers assoc consumer continue?))

(defn drop-filter! [id]
  (swap! filters dissoc id))

(defn get-consumer-by-topic [topic]
  (get @topics topic))

(defn drop-consumer! [consumer]
  (swap! consumers dissoc consumer))

(defn add-topic! [topic consumer]
  (swap! topics assoc topic consumer))

(defn get-topics []
  (->> @filters vals (map :topic)))

(defn drop-topic! [topic]
  (swap! topics dissoc topic))

(defn get-filter [id]
  (get @filters id))

(defn get-filters []
  (->> @filters
       vals))

(defn add-to-filters! [f]
  (swap! filters (fn [fs]
                   (let [exists? (->> (vals fs)
                                      (filter #(and (= (:topic f) (:topic %))
                                                    (= (:q f) (:q %))))
                                      first
                                      boolean)]
                     (if exists?
                       fs
                       ;; Generate id inside swap! to avoid race condition
                       (let [id (if (= fs {})
                                  0
                                  (->> fs keys sort last inc))]
                         (assoc fs id (assoc f :id id))))))))

(defn get-messages []
  @messages)

(defn add-to-messages! [topic msg]
  (swap! messages conj {:topic topic
                        :msg msg}))

(defn remove-messages-by-topic! [topic]
  (swap! messages #(remove (fn [m] (= (:topic m) topic)) %)))
