package akkastore.api

import akka.NotUsed
import akka.stream.scaladsl.Source
import play.api.libs.json._

import scala.concurrent.Future

abstract class AkkaStore[K, V] {
  def set(key: K, value: V): Future[Ok]
  def list: Future[List[KVPayload[K, V]]]
  def get(key: K): Future[Option[V]]
  def remove(key: K): Future[Ok]
  def watch(key: K): Source[WatchEvent[V], Future[NotUsed]]
  // def watch(key: K): Source[MyWatchEvent[V], Future[NotUsed]]
}

sealed trait Ok
case object Ok extends Ok

case class KVPayload[K, V](key: K, value: V)
case class KPayload[K](key: K)

/* // Used this to test as intermediate case to send only ValueChanged event.
  // Keeping this code as a reference since it used Formatter from play json library
case class MyWatchEvent[V](value: V)
object MyWatchEvent {
  implicit def myWatchEventPayLoadFormat[V: Format]: OFormat[MyWatchEvent[V]] = Json.format[MyWatchEvent[V]]
}
 */

trait WatchEvent[+T]
object WatchEvent {
  case class ValueUpdated[V](value: V) extends WatchEvent[V]

  //   object ValueUpdated {
  //    implicit def valueUpdatedFormat[V: Format]: OFormat[ValueUpdated[V]] = Json.format[ValueUpdated[V]]
  //    implicit def watchEventFormat: OWrites[WatchEvent[JsValue]] = derived.flat.owrites((__ \ "type").write[String])

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
