package akkastore.api

import akka.stream.KillSwitch
import akka.stream.scaladsl.Source
import play.api.libs.json._

import scala.concurrent.Future

abstract class AkkaStore[K, V] {
  def set(key: K, value: V): Future[Ok]
  def list: Future[List[KVPayload[K, V]]]
  def get(key: K): Future[Option[V]]
  def remove(key: K): Future[Ok]
  def watch(key: K): Source[WatchEvent[V], KillSwitch]
}

sealed trait Ok
case object Ok extends Ok

case class KVPayload[K, V](key: K, value: V)
case class KPayload[K](key: K)

trait WatchEvent[+T]
object WatchEvent {
  case class ValueUpdated[V](value: V) extends WatchEvent[V]

  //  Since the play json library is not able to give conversion for typed trait, writing conversion manually here.
  case object KeyRemoved extends WatchEvent[Nothing]

  implicit def watchFormat[T: Format]: Format[WatchEvent[T]] = new Format[WatchEvent[T]] {
    override def writes(o: WatchEvent[T]): JsValue = o match {
      case ValueUpdated(a) => Json.toJson(a)
      case KeyRemoved      => JsNull
    }
    override def reads(json: JsValue): JsResult[WatchEvent[T]] = json match {
      case JsNull => JsSuccess(KeyRemoved)
      case _      => implicitly[Format[T]].reads(json).map(ValueUpdated(_))
    }
  }

}
