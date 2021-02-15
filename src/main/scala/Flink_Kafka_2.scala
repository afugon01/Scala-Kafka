

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import java.util.Properties

//DEVUELVE CLASE CON SUS VALORES
object Flink_Kafka_2 {

  case class Columnas(val1:String, val2:String)


  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    //propiedades de kafka
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "localhost:9092")
    properties.setProperty("group.id", "test")

    //1. obtiene los datos de kafka
    val fuente_kafka: DataStream[String] = env.addSource(
      new FlinkKafkaConsumer[String]("topic-parti", new SimpleStringSchema(), properties))


    //2.Se le aplica funcion, se parte el string por ; y se mete a la clase ambos valores, se de vuelve la clase
    val parsedStream = fuente_kafka.map(value => {
      val columns = value.split(";")

      Columnas(columns(0), columns(1))
    })

    //devuelve la clase con valores
    //parsedStream.print()


    //devuelve cada valor por separado
    parsedStream.map(a=>a.val1+"-"+a.val2).print()

    env.execute()

  }


}
