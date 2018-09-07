package akkastore.server

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern._
import akka.cluster.ddata.typed.scaladsl.Replicator
import akka.cluster.ddata.typed.scaladsl.Replicator.{Update, UpdateResponse}
import akka.cluster.ddata.{LWWMap, LWWMapKey}
import akkastore.api.{AkkaStore, Ok}

import scala.concurrent.Future

class AkkaStoreImpl[K, V](dbName: String, runtime: ActorRuntime) extends AkkaStore[K, V] {
  import runtime._

  val DataKey: LWWMapKey[K, V] = LWWMapKey[K, V](dbName)

  override def set(key: K, value: V): Future[Ok] = {
    val update: ActorRef[UpdateResponse[LWWMap[K, V]]] => Update[LWWMap[K, V]] =
      Replicator.Update(DataKey, LWWMap.empty[K, V], Replicator.WriteLocal)(_ + (key, value))

    (replicator ? update).map {
      case x: Replicator.UpdateSuccess[_] =>
        println("update success in set")
        Ok
      case x => throw new RuntimeException(s"update failed due to: $x")
    }
  }

  override def list: Future[Map[K, V]] = {
    println("Inside list....")
    val get    = Replicator.Get(DataKey, Replicator.ReadLocal)
    val result = replicator ? get

    result.map {
      case r @ Replicator.GetSuccess(k, v) =>
        val value = r.get(k)
        println("In list success...." + value.entries)
        value.entries
      //Handle exceptions GetFailure and Notfound
      case x => throw new RuntimeException(s"Get failed due to: $x")
    }
  }

  override def get(key: K): Future[Option[V]] = {
    println("Inside get ....")
    list.map(_.get(key))
  }

  override def remove(key: K): Future[Ok] = {
    val remove: ActorRef[UpdateResponse[LWWMap[K, V]]] => Update[LWWMap[K, V]] =
      Replicator.Update(DataKey, LWWMap.empty[K, V], Replicator.WriteLocal)(_ - key)

    (replicator ? remove).map {
      case x: Replicator.UpdateSuccess[_] =>
        println("update success in remove")
        Ok
      case x => throw new RuntimeException(s"deletion failed due to: $x")
    }
  }

}
