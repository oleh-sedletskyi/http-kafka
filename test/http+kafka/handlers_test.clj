(ns http+kafka.handlers-test
  (:require [http+kafka.state :as state]
            [http+kafka.handlers :as sut]
            [clojure.test :refer [deftest testing is]]))

(deftest add-filter-test
  (with-redefs [state/filters (atom {})
                state/topics (atom {})
                state/consumers (atom {})
                state/messages (atom ())
                sut/subscribe-to-topic! identity]
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
                state/topics (atom {})
                state/consumers (atom {})
                state/messages (atom ())
                sut/subscribe-to-topic! identity]
    (testing "Delete existing filter. Check messages of certain topic are deleted as well"
      (sut/add-filter! {:topic "books"
                        :q     "sicp"})
      (sut/add-filter! {:topic "movies"
                        :q     "mar"})
      (reset! state/messages '({:topic "movies"
                                :msg "Martian"}
                               {:topic "books"
                                :msg "SICP"}))
      (sut/delete-filter! 0)
      (is (= {1 {:id    1
                 :topic "movies"
                 :q     "mar"}} @state/filters))
      (is (= '({:topic "movies"
                :msg "Martian"}) @state/messages)))))

(deftest delete-non-existing-filter-test
  (with-redefs [state/filters (atom {})
                state/topics (atom {})
                state/consumers (atom {})
                state/messages (atom ())
                sut/subscribe-to-topic! identity]
    (testing "delete non-existing filter"
      (sut/add-filter! {:topic "books"
                       :q     "sicp"})
      (sut/delete-filter! 1)
      (is (= {0 {:id    0
                 :topic "books"
                 :q     "sicp"}} @state/filters)))))
