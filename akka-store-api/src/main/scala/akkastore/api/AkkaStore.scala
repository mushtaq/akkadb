package akkastore.api

import play.api.libs.json.JsValue

import scala.concurrent.Future

abstract class AkkaStore[K, V] {
  def set(key: K, value: V): Future[Ok]
  def list: Future[Map[K, V]]
  def get(key: K): Future[Option[V]]
  def remove(key: K): Future[Ok]
}

sealed trait Ok
case object Ok extends Ok

case class KVPayload[K, V](key: K, value: V)
