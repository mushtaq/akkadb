package akkastore.api

import akka.Done
import akka.stream.KillSwitch
import akka.stream.scaladsl.Source

import scala.concurrent.Future

abstract class AkkaStore[K, V] {
  def set(key: K, value: V): Future[Done]
  def list: Future[List[KVPayload[K, V]]]
  def get(key: K): Future[Option[V]]
  def remove(key: K): Future[Done]
  def watch(key: K): Source[WatchEvent[V], KillSwitch]
}

case class KVPayload[K, V](key: K, value: V)

sealed trait WatchEvent[+T]
object WatchEvent {
  case class ValueUpdated[V](value: V) extends WatchEvent[V]
  case object KeyRemoved               extends WatchEvent[Nothing]
}
