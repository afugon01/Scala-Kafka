

//ES EL ULTIMO ESTE ES EL Q DEBE USARSE MULTIHILO

import java.util.concurrent.Executors
import scala.collection.JavaConverters._
import org.apache.kafka.clients.consumer.{ KafkaConsumer, OffsetAndMetadata, OffsetCommitCallback}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.WakeupException
import java.util.concurrent.TimeUnit
import java.util
import java.util.{Collections, Map, Properties}


// Scala code for thread creation by implementing the Runnable Interface
class MiHilo (var id:Int) extends Runnable
{
  val topics = "topic-parti"
  this.id=id

  val props: Properties =
  {
    val p = new Properties()
    p.put("group.id", "group-1")
    p.put("bootstrap.servers","localhost:9092")
    //deserializa los datos a consumir String	//deserializa los datos a consumir String
    p.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    p.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    p.put("enable.auto.commit", "false")

    p
  }

  val consumer = new KafkaConsumer[String, String](props)


  //cuerpo del hilo
  override def run()
  {
    try {

      consumer.subscribe(Collections.singletonList(this.topics))
      while(true)
      {
        val records = consumer.poll(1000).asScala

        for (record <- records)
        {
          println("Hilo: " +this.id+" Consumiendo: (key: " + record.key() + ", with value: " + record.value() +
            ") at on partition " + record.partition() + " at offset " + record.offset())
        }
        consumer.commitAsync(new Callback_C)

      }
    } catch//FIN TRY
      {
        case e:WakeupException=> println(e);

      } finally
      {
        consumer.close()
      }
  }//fin del run


  def shutdown(): Unit = {
    consumer.wakeup()
  }


}//fin clase MiHilo



class Callback_C extends OffsetCommitCallback
{
  override def onComplete(offsets: Map[TopicPartition, OffsetAndMetadata], exception: Exception): Unit = {
    // retrying only if no other commit incremented the global counter
    if(exception != null)
    {
      exception.printStackTrace()
    }
  }
}



// Creating object
object Principal
{
  // Main method
  def main(args: Array[String])
  {
    val cantidad_hilos=2

    //un hilo un consumidor
    val executor = Executors.newFixedThreadPool(cantidad_hilos)
    val consumers = new util.ArrayList[MiHilo]


    for (x <- 1 to cantidad_hilos)
    {
      var mh=new MiHilo(x)
      consumers.add(mh)
      executor.submit(mh)
    }



    //entra cuando para el proceso
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {

        import scala.collection.JavaConversions._
        for (consumer:MiHilo<- consumers) {
          consumer.shutdown()
        }
        executor.shutdown()

        try executor.awaitTermination(5000, TimeUnit.MILLISECONDS)
        catch {
          case e: InterruptedException =>
            e.printStackTrace()
        }
      }
    })



  }//fin del main





}//fin objeto Principal
