package example.typed
import akka.Done

import scala.concurrent.Future

trait AkkaDb {
  def set(key: String, value: Int): Future[Done]
  def list: Future[Map[String, Int]]
  def get(key: String): Future[Option[Int]]
  def remove(key: String): Future[Done]
}
