(ns http+kafka.routes
  (:require
   [compojure.api.sweet :as api]
   [http+kafka.handlers :as handlers]
   [ring.util.http-response :as response]
   [schema.core :as schema]))

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
                          (response/ok (handlers/add-filter! filter)))
                (api/GET "/filter/" []
                         :query-params [id :- schema/Int]
                         :summary "Get filter messages"
                         (response/ok (handlers/get-messages id)))
                (api/GET "/filter" []
                         :summary "Get all filters"
                         (response/ok (handlers/get-filters)))
                (api/DELETE "/filter/" []
                            :query-params [id :- schema/Int]
                            :summary "Delete filter"
                            (response/ok (handlers/delete-filter! id))))))
