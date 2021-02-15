

import java.io.{FileWriter, PrintWriter}

object Escribir_Fichero extends App{


  var i=0
  while(true)
    {
      //modo append
      val pw=new PrintWriter(new FileWriter("/home/angel/Desktop/hello",true))

      //escribo en el fichero
      pw.println(i)


      println(i)
      pw.close()

      //hace espera de 20 seg
      Thread.sleep(20000)
      i+=1

    }

}
