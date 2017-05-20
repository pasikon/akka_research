package example.streams

import net.manub.embeddedkafka.Codecs._
import net.manub.embeddedkafka.ConsumerExtensions._
import net.manub.embeddedkafka.EmbeddedKafkaConfig
import net.manub.embeddedkafka.streams.EmbeddedKafkaStreamsAllInOne
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.kstream.{KStream, KStreamBuilder}
import org.scalatest.{Matchers, WordSpec}

class ExampleKafkaStreamsSpec
    extends WordSpec
    with Matchers
    with EmbeddedKafkaStreamsAllInOne {

  implicit val config =
    EmbeddedKafkaConfig(kafkaPort = 7000, zooKeeperPort = 7001)

  val (inTopic, outTopic) = ("in", "out")

  val stringSerde: Serde[String] = Serdes.String()

  "A Kafka streams test" should {
    "be easy to run with streams and consumer lifecycle management" in {
      val streamBuilder = new KStreamBuilder
      val stream: KStream[String, String] =
        streamBuilder.stream(stringSerde, stringSerde, inTopic)

      stream.to(stringSerde, stringSerde, outTopic)

      runStreams(Seq(inTopic, outTopic), streamBuilder) {
        publishToKafka(inTopic, "hello", "world")
        publishToKafka(inTopic, "foo", "bar")
        publishToKafka(inTopic, "baz", "yaz")
        withConsumer[String, String, Unit] { consumer =>
          val consumedMessages: Stream[(String, String)] =
            consumer.consumeLazily(outTopic)
          consumedMessages.take(2) should be(
            Seq("hello" -> "world", "foo" -> "bar"))
          consumedMessages.drop(2).head should be("baz" -> "yaz")
        }
      }
    }

    "allow support creating custom consumers" in {
      val streamBuilder = new KStreamBuilder
      val stream: KStream[String, String] =
        streamBuilder.stream(stringSerde, stringSerde, inTopic)

      stream.to(stringSerde, stringSerde, outTopic)

      runStreams(Seq(inTopic, outTopic), streamBuilder) {
        publishToKafka(inTopic, "hello", "world")
        publishToKafka(inTopic, "foo", "bar")
        val consumer = newConsumer[String, String]()
        consumer.consumeLazily(outTopic).take(2) should be(
          Seq("hello" -> "world", "foo" -> "bar"))
        consumer.close()
      }
    }

    "allow for easy string based testing" in {
      val streamBuilder = new KStreamBuilder
      val stream: KStream[String, String] =
        streamBuilder.stream(stringSerde, stringSerde, inTopic)

      stream.to(stringSerde, stringSerde, outTopic)

      runStreamsWithStringConsumer(Seq(inTopic, outTopic), streamBuilder) {
        consumer =>
          publishToKafka(inTopic, "hello", "world")
          consumer.consumeLazily(outTopic).head should be("hello" -> "world")
      }
    }
  }
}