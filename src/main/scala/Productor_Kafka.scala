


import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerConfig, ProducerRecord, RecordMetadata}
import java.util.Properties

object Productor_Kafka {


  val bootstrapServers = "localhost:9092"
  val groupId = "kafka-example"
  val topics = "topic-parti"

  val props: Properties = {
    val p = new Properties()
    p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    p.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    p.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")

    // optional configs
    // See the Kafka Producer tutorial for descriptions of the following
    p.put(ProducerConfig.ACKS_CONFIG, "all")

    p
  }



  def main(args: Array[String]): Unit = {


    val callback = new Callback
    {
      override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
        if (exception!=null)
          exception.printStackTrace()
        else
          println(" ,Offsets enviados: "+metadata.offset().toString)
      }
    }



    val producer = new KafkaProducer[String, String](props)
    var k=0

    while(true)
    {
      k+=1
      var msg="temp;"+k

      print(msg)
      //producer.send(new ProducerRecord(topics, s"key ${k}", "oh the value! "+k))

      //asincrono con callback
      producer.send(new ProducerRecord(topics,msg), callback)

      Thread.sleep(30000)
    }


    producer.close()

  }




}
