
// Scala code for thread creation by implementing the Runnable Interface
class MyThread extends Runnable
{
  override def run()
  {
    // Displaying the thread that is running
    println("Thread " + Thread.currentThread().getName() + " is running.")
  }
}



// Creating object
object MainObject
{
  // Main method
  def main(args: Array[String])
  {
    val cantidad_hilos=2

    for (x <- 1 to cantidad_hilos)
    {
      var th = new Thread(new MyThread())
      th.setName(x.toString())
      th.start()
    }
  }
}