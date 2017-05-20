package example.streams

import net.manub.embeddedkafka.Codecs._
import com.example.prototutorial.addressbook.Person
import net.manub.embeddedkafka.ConsumerExtensions._
import com.example.prototutorial.addressbook.Person.{PhoneNumber, PhoneType}
import net.manub.embeddedkafka.EmbeddedKafkaConfig
import net.manub.embeddedkafka.streams.EmbeddedKafkaStreamsAllInOne
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.kstream.{KStream, KStreamBuilder}
import org.scalatest.{Matchers, WordSpec}


class ProtoBufKafkaSpec extends WordSpec
  with Matchers
  with EmbeddedKafkaStreamsAllInOne {

  val person: Person = Person("michal", 2, "lolo@xyx.xy", Seq(PhoneNumber("213123123123", PhoneType.HOME)))
  private val p1ByteArr = person.toByteArray
  val person2: Person = Person("rtherth", 254, "trhgert@xyx.xy", Seq(PhoneNumber("567547645", PhoneType.HOME)))
  private val p2ByteArr = person2.toByteArray


  implicit val config =
    EmbeddedKafkaConfig(kafkaPort = 7000, zooKeeperPort = 7001)

  val (inTopic, outTopic) = ("in", "out")

  val byteArrSerde: Serde[Array[Byte]] = Serdes.ByteArray()
  val stringSerde = Serdes.String()

  "should send through Kafka correct byte arrays representing persons" in {
    val streamBuilder = new KStreamBuilder
    val stream: KStream[String, Array[Byte]] =
      streamBuilder.stream(stringSerde, byteArrSerde, inTopic)

    stream.to(stringSerde, byteArrSerde, outTopic)

    runStreams(Seq(inTopic, outTopic), streamBuilder) {
      publishToKafka(inTopic, "hello", person.toByteArray)
      publishToKafka(inTopic, "foo", person2.toByteArray)
      val consumer = newConsumer[String, Array[Byte]]()

      consumer.consumeLazily(outTopic).take(2).toList.map(_._2).map(Person.parseFrom) should be (
        person :: person2 :: Nil
      )


      consumer.close()
    }
  }

}