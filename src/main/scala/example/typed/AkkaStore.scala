package example.typed
import akka.Done

import scala.concurrent.Future

trait AkkaStore[K, V] {
  def set(key: K, value: V): Future[Done]
  def list: Future[Map[K, V]]
  def get(key: K): Future[Option[V]]
  def remove(key: K): Future[Done]
}
