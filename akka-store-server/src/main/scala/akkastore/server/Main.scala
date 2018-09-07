package akkastore.server

object Main {
  def main(args: Array[String]): Unit = {
    val wiring = new Wiring
    wiring.akkaStoreServer.start()
  }
}
