package akkastore.server

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern._
import akka.cluster.ddata.typed.scaladsl.Replicator
import akka.cluster.ddata.typed.scaladsl.Replicator.{Update, UpdateResponse}
import akka.cluster.ddata.{LWWMap, LWWMapKey}
import akkastore.api.{AkkaStore, KVPayload, Ok}
import scala.async.Async._
import scala.concurrent.Future

class AkkaStoreImpl[K, V](dbName: String, runtime: ActorRuntime) extends AkkaStore[K, V] {
  import runtime._

  val DataKey: LWWMapKey[K, V] = LWWMapKey[K, V](dbName)

  override def set(key: K, value: V): Future[Ok] = async {
    val update: ActorRef[UpdateResponse[LWWMap[K, V]]] => Update[LWWMap[K, V]] =
      Replicator.Update(DataKey, LWWMap.empty[K, V], Replicator.WriteLocal)(_ + (key, value))

    val result = await(replicator ? update)
    result match {
      case x: Replicator.UpdateSuccess[_] =>
        println("update success in set")
        Ok
      case x => throw new RuntimeException(s"update failed due to: $x")
    }
  }

  override def list: Future[List[KVPayload[K, V]]] = {
    listInner.map(m => m.map { case (key, value) => KVPayload(key, value) }.toList)
  }

  override def get(key: K): Future[Option[V]] = {
    println("Inside get ...." + key)
    listInner.map(_.get(key))
  }

  private def listInner: Future[Map[K, V]] = async {
    println("Inside innerList....")
    val get    = Replicator.Get(DataKey, Replicator.ReadLocal)
    val result = await(replicator ? get)

    result match {
      case r @ Replicator.GetSuccess(k, v) =>
        val v = r.get(k)
        println("In innerList success...." + v.entries)
        v.entries
      //Handle exceptions GetFailure and Notfound
      case x => throw new RuntimeException(s"Get failed due to: $x")
    }
  }

  override def remove(key: K): Future[Ok] = async {
    val remove: ActorRef[UpdateResponse[LWWMap[K, V]]] => Update[LWWMap[K, V]] =
      Replicator.Update(DataKey, LWWMap.empty[K, V], Replicator.WriteLocal)(_ - key)

    val result = await(replicator ? remove)
    result match {
      case x: Replicator.UpdateSuccess[_] =>
        println("update success in remove")
        Ok
      case x => throw new RuntimeException(s"deletion failed due to: $x")
    }
  }

}
