package akkadb

object Main {
  def main(args: Array[String]): Unit = {
    val wiring = new Wiring
    wiring.akkaDbServer.start()
  }
}
