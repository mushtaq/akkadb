package akkadb

import akka.Done

import scala.concurrent.Future

abstract class AkkaDB[K, V]( keyCodec: AkkaStoreCodec[K], valueCodec: AkkaStoreCodec[V]) {
  def set(key: K, value: V): Future[Done]
  def list: Future[Map[K, V]]
  def get(key: K): Future[Option[V]]
  def remove(key: K): Future[Done]
}
