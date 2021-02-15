

import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessAllWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer
import org.apache.flink.streaming.connectors.elasticsearch7.ElasticsearchSink
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.util.Collector
import org.apache.http.HttpHost
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.{Requests, RestClientBuilder}

import java.util.Properties




///OBTIENE EL PROMEDIO CADA DOS MINUTOS

object Flink_Kafka_Window_Esearch {




  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.enableCheckpointing(30000)

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



    //3. se calcula promedio cada dos minutos

    val promedio_stream=tupla_stream
      .timeWindowAll(Time.minutes(2))
      .process(
        //entra una tuṕla y sale un double
        new ProcessAllWindowFunction[(String,String), Double, TimeWindow] {
        override  def process(context: Context, elements: Iterable[(String,String)], out: Collector[Double]): Unit = {

            var suma = 0.0
            var contar=0.0
            var promedio=0.0

            elements.iterator.foreach(s =>
            {
              //en seguna columna esta el valor
              println(s._2.toDouble)
              suma+= s._2.toDouble
              contar+=1
            })

            promedio=suma/contar

            out.collect(promedio)


          }
        })





    //4. ENVIO A ESEARCH EL PROMEDIO CADA DOS MINUTOS
    val httpHosts = new java.util.ArrayList[HttpHost]

    httpHosts.add(new HttpHost("localhost", 9200, "http"))
    //httpHosts.add(new HttpHost("10.2.3.1", 9200, "http"))

    //lo q entra es un Double,este double viene de promedio_stream
    val esSinkBuilder = new ElasticsearchSink.Builder[Double](
      httpHosts, (element: Double, ctx: RuntimeContext, indexer: RequestIndexer) => {

        val json = new java.util.HashMap[String, String]

        //el double se pasa a string
        println("PROMEDIO: "+element.toString)
        json.put("temperatura", element.toString)


        val rqst: IndexRequest = Requests.indexRequest
          .index("scala-temp")
          .source(json)

        indexer.add(rqst)


      }
    )



    // configuration for the bulk requests; this instructs the sink to emit after every element, otherwise they would be buffered
    esSinkBuilder.setBulkFlushMaxActions(1)


    // finally, build and add the sink to the job's pipeline
    promedio_stream.addSink(esSinkBuilder.build)




    env.execute()

  }



}