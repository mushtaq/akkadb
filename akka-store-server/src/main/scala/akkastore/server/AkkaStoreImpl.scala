package akkastore.server

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern._
import akka.cluster.ddata._
import akka.cluster.ddata.typed.scaladsl.Replicator
import akka.cluster.ddata.typed.scaladsl.Replicator.{Update, UpdateResponse}
import akka.stream.{KillSwitch, KillSwitches, OverflowStrategy}
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.typed.scaladsl.ActorSource
import akkastore.api.WatchEvent.{KeyRemoved, ValueUpdated}
import akkastore.api._

import scala.async.Async._
import scala.concurrent.Future

class AkkaStoreImpl[K, V](dbName: String, runtime: ActorRuntime) extends AkkaStore[K, V] {
  import runtime._

  val DataKey: LWWMapKey[K, V] = LWWMapKey[K, V](dbName)

  override def set(key: K, value: V): Future[Ok] = async {

    //for Key Watching
    await(updateWatchKey(key, Some(value)))

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
    //for key watching -- with value as None.
    await(updateWatchKey(key, Option.empty[V]))

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

  override def watch(key: K): Source[WatchEvent[V], KillSwitch] = {

    println("In watch Impl")

    val subscribekey: LWWRegisterKey[Option[V]] = LWWRegisterKey(key.toString)

    implicit val watchEvent: Source[WatchEvent[V], KillSwitch] = ActorSource
      .actorRef[Replicator.Changed[LWWRegister[Option[V]]]](
        completionMatcher = PartialFunction.empty,
        failureMatcher = PartialFunction.empty,
        1024,
        OverflowStrategy.dropHead
      )
      .mapMaterializedValue { actorRef =>
        replicator ! Replicator.Subscribe[LWWRegister[Option[V]]](subscribekey, actorRef)
        println("In materialize .... " + subscribekey)
      }
      .viaMat(KillSwitches.single)(Keep.right)
      /*.map { a =>
        println("*" * 80)
        println(a)
        println("*" * 80)
        a
      }*/
      .collect {
        case c @ Replicator.Changed(datakey) if (c.get(datakey).value.get == None) =>
          println("Key Deleted ... " + datakey)
          KeyRemoved //Some issues with replicator subscibing an event- when value against key is None. Need to look into it.
        case c @ Replicator.Changed(datakey) =>
          println("Value Changed ... " + datakey)
          ValueUpdated(c.get(datakey).value.get)
        case _ => throw new RuntimeException(s"Exeption in Subscribed collect..")
      }
    //watchEvent.to(Sink.foreach(println)).run()
    watchEvent
  }

  def updateWatchKey(key: K, value: Option[V] = None): Future[Ok] = async {

    val watchKey: LWWRegisterKey[Option[V]] = LWWRegisterKey(key.toString)

    println("In UpdateWatchKey..." + key.toString + " " + value)

    val update: ActorRef[UpdateResponse[LWWRegister[Option[V]]]] => Update[LWWRegister[Option[V]]] =
      Replicator.Update(watchKey, LWWRegister(Option.empty[V]), Replicator.WriteLocal)(_ => LWWRegister(value))

    val result = await(replicator ? update)
    result match {
      case x: Replicator.UpdateSuccess[_] =>
        println("WatchKey update success..")
        Ok
      case x => throw new RuntimeException(s"update failed due to: $x")
    }
  }

}
