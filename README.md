# http+kafka

A Clojure service for filtering messages from Kafka.

## Usage

Start Kafka container and service using the following command:

`docker-compose up -d && lein ring server`

(this should automatically open swagger page `http://localhost:3000/index.html` where you can add/delete/retrieve filters and get its messages)

You can create a new topic using the following command (here `books`):

`docker exec -it kafka kafka-topics --create --topic books --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1`

You can produce the message to the topic (here `books`) using command:

`echo "Hello SICP" | kcat -b localhost:9092 -t books -D" " -P`

To stop the service use `Ctrl+C` and then run `docker-compose down`.
