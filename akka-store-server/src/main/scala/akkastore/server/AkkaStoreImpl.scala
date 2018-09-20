package akkastore.server

import akka.NotUsed
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern._
import akka.cluster.ddata.typed.scaladsl.Replicator
import akka.cluster.ddata.typed.scaladsl.Replicator.{Update, UpdateResponse}
import akka.cluster.ddata.{LWWMap, LWWMapKey, LWWRegister, LWWRegisterKey, ORSetKey}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.typed.scaladsl.ActorSource
import akkastore.api.WatchEvent.{KeyRemoved, ValueUpdated}
import akkastore.api.{AkkaStore, KVPayload, Ok, WatchEvent}

import scala.async.Async._
import scala.concurrent.Future

class AkkaStoreImpl[K, V](dbName: String, runtime: ActorRuntime) extends AkkaStore[K, V] {
  import runtime._

  val DataKey: LWWMapKey[K, V]            = LWWMapKey[K, V](dbName)
  var watchKey: LWWRegisterKey[Option[V]] = _

  override def set(key: K, value: V): Future[Ok] = async {

    //for Key Watching
    val z = new UpdateWatchKey(key, Some(value))

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

  override def watch(key: K): Source[WatchEvent[V], NotUsed] = {

    println("In watch Impl")

    //first try to get key with keyName, if exists means you don't need to add to registry.
    //else add empty value to registry....

    val z: UpdateWatchKey[K, V] = new UpdateWatchKey(key, None) //add key with empty value

    implicit val watchEvent: Source[WatchEvent[V], NotUsed.type] = ActorSource
      .actorRef[Replicator.Changed[LWWRegister[Option[V]]]](
        completionMatcher = PartialFunction.empty,
        failureMatcher = PartialFunction.empty,
        1024,
        OverflowStrategy.dropHead
      )
      .mapMaterializedValue { actorRef =>
        replicator ! Replicator.Subscribe[LWWRegister[Option[V]]](z.watchKey, actorRef)
        println("In materialize .... ")
        NotUsed
      }
      .collect {
        case c @ Replicator.Changed(datakey) =>
          /*if c.get(watchKey).isDefined ⇒*/
          println("Value Changed ... " + datakey)
          ValueUpdated(c.get(datakey).value.get)
        case c @ Replicator.Changed(dataKey) ⇒
          println("Value Removed ... " + dataKey)
          KeyRemoved
        case _ => throw new RuntimeException(s"Exeption in Subscribed collect..")

      }
    watchEvent
  }

  class UpdateWatchKey[K, V](key: K, value: Option[V] = None) {

    val watchKey: LWWRegisterKey[Option[V]] = LWWRegisterKey(key.toString)
    val watchValue                          = Option(value).getOrElse(Option.empty[V])

    async {
      println("In setForWatchKey..." + key + " " + watchValue)

      val update: ActorRef[UpdateResponse[LWWRegister[Option[V]]]] => Update[LWWRegister[Option[V]]] =
        Replicator.Update(watchKey, LWWRegister(Option.empty[V]), Replicator.WriteLocal)(_ => LWWRegister(watchValue))

      val result = await(replicator ? update)
      result match {
        case x: Replicator.UpdateSuccess[_] =>
          println("WatchKey update success..")
          Ok
        case x => throw new RuntimeException(s"update failed due to: $x")
      }
    }
  }
}
