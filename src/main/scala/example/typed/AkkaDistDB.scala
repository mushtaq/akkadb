package example.typed
import scala.concurrent.Future

trait AkkaDistDB {

  def set(key: String, value: Int)
  def getAll(): Future[Map[String, Int]]
  def get(key: String): Future[Map[String, Int]]
  def remove(key: String)

}
