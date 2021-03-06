package com.example.ask

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.scaladsl._
import akka.kafka._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.example.prototutorial.addressbook.Person
import com.example.prototutorial.addressbook.Person.{PhoneNumber, PhoneType}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization._

import scala.concurrent._
import scala.concurrent.duration._


/*

https://github.com/foundweekends/giter8/wiki/giter8-templates

sbt protocGenerate

-Dinput-topic=kfk_str_test
-Doutput-topic=kfk_str_test
-Dakka.kafka.consumer.kafka-clients.group.id=akka_streams_ex_gr
-Dakka.kafka.consumer.kafka-clients.bootstrap.servers=localhost:9092
-Dakka.kafka.producer.kafka-clients.bootstrap.servers=localhost:9092

 */
object TransformApp extends App with LazyLogging {

  private val config = ConfigFactory.load()
  private val inputTopic = config.getString("input-topic")
  private val outputTopic = config.getString("output-topic")
  private implicit val actorSystem =
    ActorSystem("kafka-mirror-system", config)


  try {
    val producerSettings = ProducerSettings(actorSystem,
      new ByteArraySerializer,
      new ByteArraySerializer)
    val consumerSettings = ConsumerSettings(actorSystem,
      new ByteArrayDeserializer,
      new ByteArrayDeserializer)

    implicit val actorMaterializer = ActorMaterializer()

    val person: Person = Person("michal", 2, "lolo@xyx.xy", Seq(PhoneNumber("213123123123", PhoneType.HOME)))

    //one person to topic
    val done = Source(person :: Nil)
      .map(_.toByteArray)
      .map { elem =>
        new ProducerRecord[Array[Byte], Array[Byte]](inputTopic, elem)
      }
      .runWith(Producer.plainSink(producerSettings))

    /**
      * Using a committableSource and a committableSink,
      * we can resume from where we left off if our process crashes.
      */
    val completion: Future[Done] =
      Consumer
        .committableSource(consumerSettings, Subscriptions.topics(inputTopic))
        .map { msg =>

          println(msg.record.value)
          Thread.sleep(3000)
          /**
            * Basically copy from one topic to another.
            *
            * Note input timestamp may be -1 but output timestamp
            * cannot be -1!
            */
          val newTimestamp: java.lang.Long = {
            if (msg.record.timestamp() < 0) null
            else msg.record.timestamp()
          }

//          logger.info(s"Consumed ${msg.record.value()}")
          println(s"Consumed ${Person.parseFrom(msg.record.value)}")

          ProducerMessage.Message(new ProducerRecord(
            outputTopic,
            null,
            newTimestamp,
            msg.record.key,
            msg.record.value
          ),
            msg.committableOffset)
        }
        .runWith(Producer.commitableSink(producerSettings))

    Await.result(completion, Duration.Inf)
  } finally {
    Await.result(actorSystem.terminate(), Duration.Inf)
  }

}
