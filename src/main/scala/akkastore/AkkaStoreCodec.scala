package akkastore

trait AkkaStoreCodec[T] {
  def toString(value: T): String
  def fromString(string: String): T
}

object AkkaStoreCodec {
  object StringCodec extends AkkaStoreCodec[String] {
    override def toString(value: String): String    = value
    override def fromString(string: String): String = string
  }
}
