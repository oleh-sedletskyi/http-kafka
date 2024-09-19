(ns http+kafka.state-test
  (:require [http+kafka.state :as state]
            [http+kafka.handlers :as sut]
            [http+kafka.kafka :as kafka]
            [clojure.test :refer [deftest testing is]]))

(deftest add-filter-test
  (with-redefs [state/filters (atom {})
                kafka/start-consumer-thread! identity]
    (testing "Add new filter"
      (sut/add-filter! {:topic "books"
                       :q     "sicp"})
      (is (= {0 {:id    0
                 :topic "books"
                 :q     "sicp"} } @state/filters)))
    (testing "Add existing filter"
      (sut/add-filter! {:topic "books"
                       :q      "sicp"})
      (is (= {0 {:id    0
                 :topic "books"
                 :q     "sicp"} } @state/filters)))
    (testing "Add another filter"
      (sut/add-filter! {:topic "books"
                       :q     "python"})
      (is (= {0 {:id    0
                 :topic "books"
                 :q     "sicp"}
              1 {:id    1
                 :topic "books"
                 :q     "python"}} @state/filters)))))

(deftest delete-filter-test
  (with-redefs [state/filters (atom {})
                state/topics (atom {})]
    (testing "delete existing filter"
      (sut/add-filter! {:topic "books"
                       :q     "sicp"})
      (sut/add-filter! {:topic "books"
                       :q     "python"})
      (sut/delete-filter! 0)
      (is (= {1 {:id    1
                 :topic "books"
                 :q     "python"}} @state/filters)))))

(deftest delete-non-existing-filter-test
  (with-redefs [state/filters (atom {})
                state/topics (atom {})]
    (testing "delete non-existing filter"
      (sut/add-filter! {:topic "books"
                       :q     "sicp"})
      (sut/delete-filter! 1)
      (is (= {0 {:id    0
                 :topic "books"
                 :q     "sicp"}} @state/filters)))))
