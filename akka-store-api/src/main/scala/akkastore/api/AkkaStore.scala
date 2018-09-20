package akkastore.api

import akka.NotUsed
import akka.stream.scaladsl.Source

import scala.concurrent.Future

abstract class AkkaStore[K, V] {
  def set(key: K, value: V): Future[Ok]
  def list: Future[List[KVPayload[K, V]]]
  def get(key: K): Future[Option[V]]
  def remove(key: K): Future[Ok]
  def watch(key: K): Source[WatchEvent[V], NotUsed]
}

sealed trait Ok
case object Ok extends Ok

case class KVPayload[K, V](key: K, value: V)

sealed trait WatchEvent[+T]
object WatchEvent {
  case class ValueUpdated[V](value: V) extends WatchEvent[V]
  case object KeyRemoved               extends WatchEvent[Nothing]
}
