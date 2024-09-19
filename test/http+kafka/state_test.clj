(ns http+kafka.state-test
  (:require [http+kafka.state :as sut]
            [clojure.test :refer [deftest testing is]]))

(deftest add-filter-test
  (with-redefs [sut/filters (atom {})
                sut/update-filters? (atom false)]
    (testing "Add new filter"
      (sut/add-filter {:topic "books"
                       :q     "sicp"})
      (is (= {0 {:id    0
                 :topic "books"
                 :q     "sicp"} } @sut/filters))
      (is (= true @sut/update-filters?)))
    (testing "Add existing filter"
      (reset! sut/update-filters? false)
      (sut/add-filter {:topic "books"
                       :q      "sicp"})
      (is (= {0 {:id    0
                 :topic "books"
                 :q     "sicp"} } @sut/filters))
      (is (= false @sut/update-filters?)))
    (testing "Add another filter"
      (sut/add-filter {:topic "books"
                       :q     "python"})
      (is (= {0 {:id    0
                 :topic "books"
                 :q     "sicp"}
              1 {:id    1
                 :topic "books"
                 :q     "python"}} @sut/filters))
      (is (= true @sut/update-filters?)))))

(deftest delete-filter-test
  (with-redefs [sut/filters (atom {})
                sut/update-filters? (atom false)
                sut/topics (atom {})]
    (testing "delete existing filter"
      (is (= false @sut/update-filters?))
      (sut/add-filter {:topic "books"
                       :q     "sicp"})
      (sut/add-filter {:topic "books"
                       :q     "python"})
      (is (= true @sut/update-filters?))
      (sut/delete-filter 0)
      (is (= {1 {:id    1
                 :topic "books"
                 :q     "python"}} @sut/filters))
      (is (= true @sut/update-filters?)))))

(deftest delete-non-existing-filter-test
  (with-redefs [sut/filters (atom {})
                sut/update-filters? (atom false)
                sut/topics (atom {})]
    (testing "delete non-existing filter"
      (sut/add-filter {:topic "books"
                       :q     "sicp"})
      (is (= true @sut/update-filters?))
      (reset! sut/update-filters? false)
      (sut/delete-filter 1)
      (is (= {0 {:id    0
                 :topic "books"
                 :q     "sicp"}} @sut/filters))
      (is (= false @sut/update-filters?)))))
