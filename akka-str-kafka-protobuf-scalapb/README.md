# akka-str-kafka-protobuf-scalapb

# Info

Simple POC for using Protocol Buffer with ScalaPB witk akka-streams-kafka

# Building

* Docker image, locally: `sbt docker:publishLocal`
* Docker image: `sbt docker:publish`
* Distribution package (bare): `sbt 'show stage'`
* Distribution package (zip): `sbt 'show stage'` or `sbt 'show dist'`

## Configuration options

* `input-topic`
* `output-topic`
* `akka.kafka.consumer.kafka-clients.group.id`
* `akka.kafka.consumer.kafka-clients.bootstrap.servers`
* `akka.kafka.producer.kafka-clients.bootstrap.servers`

example:

sbt protocGenerate

-Dinput-topic=kfk_str_test 
-Doutput-topic=kfk_str_test
-Dakka.kafka.consumer.kafka-clients.group.id=akka_streams_ex_gr
-Dakka.kafka.consumer.kafka-clients.bootstrap.servers=localhost:9092
-Dakka.kafka.producer.kafka-clients.bootstrap.servers=localhost:9092

## Running locally

```
$ ./target/universal/stage/bin/mirror \
    -Dinput-topic=blah -Doutput-topic=bleh ...
```

## Running in Docker

```
$ export JAVA_OPTS=" \
-Dinput-topic=blah \
-Doutput-topic=blah \
...
"
$ docker run -e JAVA_OPTS -i mirror
```

## Further detail

Built using [akka-stream-kafka-template.g8](https://github.com/ScalaWilliam/akka-stream-kafka-template.g8)
