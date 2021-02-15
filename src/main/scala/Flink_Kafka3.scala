

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import java.util.Properties

//DEVUELVE TUPLA
object Flink_Kafka3 {


  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    //propiedades de kafka
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "localhost:9092")
    properties.setProperty("group.id", "test")

    //1. obtiene los datos de kafka
    val fuente_kafka: DataStream[String] = env.addSource(
      new FlinkKafkaConsumer[String]("topic-parti", new SimpleStringSchema(), properties))


    //2.divide por ; y lo mete en array, luego hace una tupla de cada valor, devuelve DataStream[Tuple2]
    val tupla_stream = fuente_kafka.map(value => {
      val arreglo_columnas = value.split(";")
      val tupla_dos= (arreglo_columnas(0), arreglo_columnas(1))
      tupla_dos

    })

    //IMPRIME EL DATO TAL COMO VIENE EN TIEMPO REAL
    //3. imprime la tupla
    tupla_stream.print()


    env.execute()

  }

}
