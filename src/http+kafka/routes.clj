(ns http+kafka.routes
  (:require
   [compojure.api.sweet :as api]
   ;; [ring.util.http-response :refer :all]
   [ring.middleware.params :refer [wrap-params]]
   [ring.util.http-response :as response]
   [schema.core :as schema]
   ;; [clojure.spec.alpha :as s]
   [ring.swagger.schema :as rs]))

(schema/defschema filter
  {:topic        schema/Str
   :q            schema/Str})

(def app
  (api/api
   {:swagger
    {:ui   "/"
     :spec "/swagger.json"
     :data {:info {:title "Filters for Kafka messages"}
            :consumes ["application/json"]
            :produces ["application/json"]
            :tags [{:name "api"}]}}}
   (api/context "" []
                :tags ["api"]
                (api/POST "/filter" []
                          :body [filter (api/describe filter "Add new filter")]
                          :summary "Create filter"
                          (response/ok {:result filter}))
                (api/GET "/filter/" []
                         :query-params [id :- schema/Int]
                         :summary "Get filter"
                         (response/ok {:result id}))
                (api/GET "/filter" []
                         :summary "Get all filters"
                         (response/ok {:result :all-filters}))
                (api/DELETE "/filter/" []
                            :query-params [id :- schema/Int]
                            :summary "Delete filter"
                            (response/ok {:result {:deleted id}})))))
